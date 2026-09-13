package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.DailyReview
import com.superplanner.app.domain.model.ReviewSuggestion
import com.superplanner.app.domain.model.WeeklyReview
import com.superplanner.app.domain.model.WeeklyPlanning
import java.time.LocalDate
import javax.inject.Inject

class BuildReviews @Inject constructor() {
    fun daily(planning: WeeklyPlanning, date: LocalDate): DailyReview {
        val day = planning.days.firstOrNull { it.date == date }
        if (day == null) return DailyReview(date, null, 0, 0, emptyList(), emptyList(), emptyList())

        val priority = day.activities.minByOrNull { it.instance.priority.weight }?.title
        val unfinished = (day.plannedCount - day.completedCount).coerceAtLeast(0)
        val changed = buildList {
            if (day.conflictCount > 0) add("Há ${day.conflictCount} conflito(s) na rota de hoje.")
            if (unfinished > 0) add("$unfinished atividade(s) planejada(s) ainda não aconteceu(aram).")
        }
        val toReorganize = if (day.conflictCount > 0 || unfinished > 0) {
            day.activities.takeLast(unfinished.coerceAtMost(day.activities.size)).map { it.title }
        } else emptyList()

        val suggestions = buildList {
            if (unfinished > 0) add(
                ReviewSuggestion(
                    id = "daily-reorganize",
                    title = "Reorganizar o que ficou para trás",
                    explanation = "Preserve a prioridade principal e não tente compensar tudo de uma vez.",
                    actionLabel = "Reorganizar rota",
                ),
            )
            if (day.conflictCount > 0) add(
                ReviewSuggestion(
                    id = "daily-conflict",
                    title = "Rever conflitos",
                    explanation = "A rota tem compromissos concorrentes que precisam de uma decisão explícita.",
                    actionLabel = "Rever conflitos",
                ),
            )
        }
        return DailyReview(date, priority, day.plannedCount, day.completedCount, changed, toReorganize, suggestions)
    }

    fun weekly(planning: WeeklyPlanning): WeeklyReview {
        val advanced = planning.days.filter { it.completedCount > 0 }
            .flatMap { it.activities.filter { activity -> activity.instance.priority.weight <= 1 }.map { it.title } }
            .distinct()
            .take(5)
        val repeated = planning.days
            .filter { it.plannedCount > it.completedCount }
            .flatMap { it.activities.takeLast((it.plannedCount - it.completedCount).coerceAtLeast(0)) }
            .groupingBy { it.title }
            .eachCount()
            .filterValues { it >= 2 }
            .keys
            .take(5)
        val nextChanges = buildList {
            if (repeated.isNotEmpty()) add("Reduza ou redistribua atividades que estão ficando para trás repetidamente.")
            if (planning.days.any { it.conflictCount > 0 }) add("Reserve espaço para resolver conflitos antes de adicionar novas tarefas.")
        }
        val neglected = if (repeated.isNotEmpty()) listOf("Há atividades recorrentes que estão perdendo espaço na semana.") else emptyList()
        val suggestions = buildList {
            if (repeated.isNotEmpty()) add(
                ReviewSuggestion(
                    id = "weekly-repeat",
                    title = "Ajustar atividades recorrentes",
                    explanation = "Algumas atividades ficaram para trás em mais de um dia. Vale reduzir, mover ou redefinir frequência.",
                    actionLabel = "Revisar recorrências",
                ),
            )
            if (planning.days.any { it.conflictCount > 0 }) add(
                ReviewSuggestion(
                    id = "weekly-conflicts",
                    title = "Proteger espaço para compromissos",
                    explanation = "Conflitos repetidos indicam que a capacidade planejada está apertada.",
                    actionLabel = "Ajustar capacidade",
                ),
            )
        }
        return WeeklyReview(advanced, repeated, nextChanges, neglected, suggestions)
    }
}
