package com.superplanner.app.domain.planning

import java.time.Duration

/** Structured facts used to estimate realistic planning capacity without judging the user. */
data class CapacityEstimateInput(
    val date: String,
    val availableWindow: Duration,
    val fixedCommitments: Duration = Duration.ZERO,
    val work: Duration = Duration.ZERO,
    val sleepAndRecovery: Duration = Duration.ZERO,
    val logistics: Duration = Duration.ZERO,
    val preparation: Duration = Duration.ZERO,
    val desiredActivities: Duration = Duration.ZERO,
    val safetyMargin: Duration = Duration.ZERO,
    val historicalPlanned: Duration? = null,
    val historicalActual: Duration? = null,
) {
    init {
        require(!availableWindow.isNegative) { "availableWindow cannot be negative" }
        listOf(
            fixedCommitments, work, sleepAndRecovery, logistics, preparation, desiredActivities, safetyMargin,
        ).forEach { require(!it.isNegative) { "capacity durations cannot be negative" } }
        require(historicalPlanned == null || !historicalPlanned.isNegative) { "historicalPlanned cannot be negative" }
        require(historicalActual == null || !historicalActual.isNegative) { "historicalActual cannot be negative" }
    }
}

enum class CapacityLoad {
    SUSTAINABLE,
    TIGHT,
    OVER_CAPACITY,
}

data class DailyCapacityAssessment(
    val date: String,
    val grossWindow: Duration,
    val protectedTime: Duration,
    val desiredLoad: Duration,
    val schedulableCapacity: Duration,
    val remainingCapacity: Duration,
    val load: CapacityLoad,
    val reasons: List<String>,
)

data class WeeklyCapacityAssessment(
    val days: List<DailyCapacityAssessment>,
    val totalCapacity: Duration,
    val totalDesiredLoad: Duration,
    val totalRemainingCapacity: Duration,
    val load: CapacityLoad,
    val reasons: List<String>,
)

/**
 * Capacity is a budget, not all free calendar time. When history is insufficient the estimate
 * stays conservative; observed planned-vs-actual duration can reduce the usable budget.
 */
class WeeklyCapacityEstimator(
    private val utilizationLimit: Double = 0.80,
) {
    init {
        require(utilizationLimit in 0.0..1.0) { "utilizationLimit must be between 0 and 1" }
    }

    fun estimate(input: CapacityEstimateInput): DailyCapacityAssessment {
        val protected = protectedTime(input)
        val free = input.availableWindow.minus(protected).coerceAtLeastZero()
        val historyFactor = historicalFactor(input)
        val capacity = Duration.ofMillis((free.toMillis() * utilizationLimit * historyFactor).toLong())
        val remaining = capacity.minus(input.desiredActivities).coerceAtLeastZero()
        val load = classifyLoad(loadRatio(capacity, input.desiredActivities))

        return DailyCapacityAssessment(
            date = input.date,
            grossWindow = input.availableWindow,
            protectedTime = protected,
            desiredLoad = input.desiredActivities,
            schedulableCapacity = capacity,
            remainingCapacity = remaining,
            load = load,
            reasons = reasonsFor(input, historyFactor, load),
        )
    }

    fun estimateWeek(inputs: List<CapacityEstimateInput>): WeeklyCapacityAssessment {
        require(inputs.isNotEmpty()) { "at least one day is required" }
        val days = inputs.map(::estimate)
        val totalCapacity = days.fold(Duration.ZERO) { acc, day -> acc.plus(day.schedulableCapacity) }
        val totalDesired = days.fold(Duration.ZERO) { acc, day -> acc.plus(day.desiredLoad) }
        val remaining = totalCapacity.minus(totalDesired).coerceAtLeastZero()
        val load = classifyLoad(loadRatio(totalCapacity, totalDesired))
        val reasons = days.flatMap { day -> day.reasons.map { "${day.date}: $it" } }
            .distinct()
            .take(8)

        return WeeklyCapacityAssessment(days, totalCapacity, totalDesired, remaining, load, reasons)
    }

    private fun protectedTime(input: CapacityEstimateInput): Duration = listOf(
        input.fixedCommitments,
        input.work,
        input.sleepAndRecovery,
        input.logistics,
        input.preparation,
        input.safetyMargin,
    ).fold(Duration.ZERO, Duration::plus)

    private fun loadRatio(capacity: Duration, desired: Duration): Double =
        if (capacity.isZero) {
            if (desired.isZero) 0.0 else 2.0
        } else {
            desired.toMillis().toDouble() / capacity.toMillis().toDouble()
        }

    private fun classifyLoad(ratio: Double): CapacityLoad = when {
        ratio <= 0.80 -> CapacityLoad.SUSTAINABLE
        ratio <= 1.0 -> CapacityLoad.TIGHT
        else -> CapacityLoad.OVER_CAPACITY
    }

    private fun reasonsFor(
        input: CapacityEstimateInput,
        historyFactor: Double,
        load: CapacityLoad,
    ): List<String> = buildList {
        if (!input.fixedCommitments.isZero) add("compromissos fixos consomem capacidade")
        if (!input.work.isZero) add("trabalho é tempo protegido")
        if (!input.sleepAndRecovery.isZero) add("sono e recuperação são tempo legítimo")
        if (!input.logistics.isZero) add("deslocamentos foram descontados")
        if (!input.preparation.isZero) add("preparação foi descontada")
        if (historyFactor < 1.0) add("histórico realizado sugere uma margem mais conservadora")
        if (load == CapacityLoad.OVER_CAPACITY) add("a carga desejada excede a capacidade estimada")
    }

    private fun historicalFactor(input: CapacityEstimateInput): Double {
        val planned = input.historicalPlanned ?: return 0.85
        val actual = input.historicalActual ?: return 0.85
        if (planned.isZero) return 0.85
        val ratio = actual.toMillis().toDouble() / planned.toMillis().toDouble()
        return ratio.coerceIn(0.70, 1.0)
    }
}

private fun Duration.coerceAtLeastZero(): Duration = if (isNegative) Duration.ZERO else this
