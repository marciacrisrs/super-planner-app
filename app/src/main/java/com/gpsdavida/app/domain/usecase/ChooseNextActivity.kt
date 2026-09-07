package com.gpsdavida.app.domain.usecase

import com.gpsdavida.app.domain.model.ActivityInstance
import com.gpsdavida.app.domain.model.ActivityStatus
import com.gpsdavida.app.domain.model.Availability
import com.gpsdavida.app.domain.model.AvailabilityKind
import com.gpsdavida.app.domain.model.Dependency
import com.gpsdavida.app.domain.model.Energy
import com.gpsdavida.app.domain.model.ExecutionContext
import com.gpsdavida.app.domain.model.Flexibility
import com.gpsdavida.app.domain.model.NextActionContext
import com.gpsdavida.app.domain.model.NextActionDecision
import com.gpsdavida.app.domain.model.NextActionReason
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject

/**
 * Chooses the best executable activity without knowing where it came from.
 */
class ChooseNextActivity @Inject constructor() {
    operator fun invoke(
        activities: List<ActivityInstance>,
        now: Instant,
    ): ActivityInstance? = invoke(
        activities = activities,
        context = NextActionContext(now = now),
    ).recommended

    operator fun invoke(
        activities: List<ActivityInstance>,
        context: NextActionContext,
    ): NextActionDecision {
        val baseExecutable = activities
            .asSequence()
            .filter { it.status == ActivityStatus.PENDING }
            .filter { isAvailable(it, context) }
            .filter { matchesContext(it, context.currentContext) }
            .filter { dependenciesSatisfied(it, activities, context.dependencies) }
            .toList()

        val current = baseExecutable
            .filter { it.planned.start <= context.now && context.now < it.planned.end }
            .minWithOrNull(currentComparator)

        val executable = baseExecutable
            .filter { travelAndBufferFitBeforeStart(it, current, context) }

        val next = executable
            .asSequence()
            .filter { it.id != current?.id }
            .sortedWith(nextComparator(context))
            .firstOrNull()

        return NextActionDecision(
            current = current,
            next = next,
            travelDurationToNext = travelDurationTo(next, current, context),
            currentReasons = current?.let { reasonsFor(it, context, isCurrent = true) }.orEmpty(),
            nextReasons = next?.let { reasonsFor(it, context, isCurrent = false) }.orEmpty(),
        )
    }

    private fun reasonsFor(
        activity: ActivityInstance,
        context: NextActionContext,
        isCurrent: Boolean,
    ): List<NextActionReason> = buildList {
        if (isCurrent) add(NextActionReason.CURRENTLY_ACTIVE)
        if (activity.planned.start <= context.now) add(NextActionReason.DUE_NOW)
        if (activity.flexibility == Flexibility.FIXED) add(NextActionReason.FIXED_COMMITMENT)
        if (isAvailable(activity, context)) add(NextActionReason.AVAILABLE_IN_WINDOW)
        if (activity.contexts.isEmpty() || context.currentContext == null || context.currentContext in activity.contexts) {
            add(NextActionReason.CONTEXT_MATCH)
        }
        if (activity.energy == null || context.currentEnergy == null || activity.energy == context.currentEnergy) {
            add(NextActionReason.ENERGY_MATCH)
        }
        if (dependenciesSatisfied(activity, listOf(activity), emptyList())) {
            add(NextActionReason.DEPENDENCIES_SATISFIED)
        }
        if (activity.flexibility != Flexibility.FIXED) add(NextActionReason.FLEXIBLE_SLOT)
        if (travelDurationTo(activity, null, context).isZero() || travelAndBufferFitBeforeStart(activity, null, context)) {
            add(NextActionReason.TRAVEL_FITS)
        }
    }

    private fun matchesContext(
        activity: ActivityInstance,
        currentContext: ExecutionContext?,
    ): Boolean {
        if (currentContext == null || activity.contexts.isEmpty()) return true
        return currentContext in activity.contexts
    }

    private fun isAvailable(
        activity: ActivityInstance,
        context: NextActionContext,
    ): Boolean {
        if (context.availability.isEmpty()) return true

        val start = activity.planned.start.atZone(context.zoneId)
        val end = activity.planned.end.atZone(context.zoneId)
        if (start.toLocalDate() != end.toLocalDate()) return false

        val dayRules = context.availability.filter { it.dayOfWeek == start.dayOfWeek }
        if (dayRules.isEmpty()) return true

        val blocked = dayRules
            .filter { it.kind == AvailabilityKind.BLOCKED }
            .any { rangesOverlap(start.toLocalTime(), end.toLocalTime(), it.window.start, it.window.end) }
        if (blocked) return false

        val freeWindows = dayRules
            .filter { it.kind == AvailabilityKind.FREE }
            .map { it.window }

        return freeWindows.isEmpty() || freeWindows.any {
            it.start <= start.toLocalTime() && end.toLocalTime() <= it.end
        }
    }

    private fun dependenciesSatisfied(
        activity: ActivityInstance,
        activities: List<ActivityInstance>,
        dependencies: List<Dependency>,
    ): Boolean = dependencies
        .filter { it.successor == activity.source }
        .all { dependency ->
            activities.any {
                it.source == dependency.predecessor && it.status == ActivityStatus.DONE
            }
        }

    private fun travelAndBufferFitBeforeStart(
        activity: ActivityInstance,
        current: ActivityInstance?,
        context: NextActionContext,
    ): Boolean {
        if (activity.id == current?.id || activity.planned.start <= context.now) return true

        val departure = current?.planned?.end ?: context.now
        val travel = travelDurationTo(activity, current, context)
        val buffer = current?.bufferAfter ?: context.defaultBuffer
        return departure.plus(travel).plus(buffer) <= activity.planned.start
    }

    private fun travelDurationTo(
        target: ActivityInstance?,
        current: ActivityInstance?,
        context: NextActionContext,
    ): Duration {
        val targetLocation = target?.location?.id ?: return Duration.ZERO
        val origin = current?.location?.id ?: context.currentLocation ?: return Duration.ZERO
        if (origin == targetLocation) return Duration.ZERO

        return context.travelTimes
            .firstOrNull { it.from == origin && it.to == targetLocation }
            ?.duration
            ?: Duration.ZERO
    }

    private fun rangesOverlap(
        start: LocalTime,
        end: LocalTime,
        otherStart: LocalTime,
        otherEnd: LocalTime,
    ): Boolean = start < otherEnd && end > otherStart

    private val currentComparator = compareBy<ActivityInstance> { it.priority.weight }
        .thenBy { it.planned.start }

    private fun nextComparator(context: NextActionContext) =
        compareBy<ActivityInstance> { urgency(it, context.now) }
            .thenBy { it.priority.weight }
            .thenBy { energyPenalty(it.energy, context.currentEnergy) }
            .thenBy { it.flexibility != Flexibility.FIXED }
            .thenBy { it.planned.start }

    private fun urgency(activity: ActivityInstance, now: Instant): Int =
        if (activity.planned.start <= now) 0 else 1

    private fun energyPenalty(activityEnergy: Energy?, currentEnergy: Energy?): Int {
        if (currentEnergy == null || activityEnergy == null) return 0

        val distance = mapOf(
            Energy.LOW to 0,
            Energy.MEDIUM to 1,
            Energy.HIGH to 2,
        )
        return kotlin.math.abs(distance.getValue(activityEnergy) - distance.getValue(currentEnergy))
    }
}
