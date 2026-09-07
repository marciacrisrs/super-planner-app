package com.gpsdavida.app.ui.horizontes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.domain.usecase.ObserveWeeklyPlanning
import java.time.LocalDate

@Composable
fun WeeklyReviewScreen(
    viewModel: WeeklyReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Revisão semanal", style = MaterialTheme.typography.headlineSmall) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("Planejado", "${state.plannedMinutes} min")
                    Metric("Realizado", "${state.actualMinutes} min")
                    Metric("Concluído", "${state.completedCount}")
                }
            }
        }
        item { Text("Atenção", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
        state.conflicts.forEach { conflict ->
            item { Card(Modifier.fillMaxWidth()) { Text("Conflito em ${conflict}", Modifier.padding(16.dp)) } }
        }
        item { Text("Próxima semana", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
        state.days.forEach { day ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(day.date.toString(), style = MaterialTheme.typography.titleMedium)
                        Text("${day.plannedCount} atividades · ${day.plannedDuration.toMinutes()} min · ${day.completedCount} concluídas")
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class WeeklyReviewViewModel @javax.inject.Inject constructor(
    observe: ObserveWeeklyPlanning,
    clock: java.time.Clock,
) : androidx.lifecycle.ViewModel() {
    data class State(
        val plannedMinutes: Long = 0,
        val actualMinutes: Long = 0,
        val completedCount: Int = 0,
        val conflicts: List<String> = emptyList(),
        val days: List<com.gpsdavida.app.domain.model.WeeklyDaySummary> = emptyList(),
    )

    val state: kotlinx.coroutines.flow.StateFlow<State> = observe(
        LocalDate.now(clock).with(java.time.DayOfWeek.MONDAY),
    ).map { planning ->
        State(
            plannedMinutes = planning.plannedDuration.toMinutes(),
            actualMinutes = planning.actualDuration.toMinutes(),
            completedCount = planning.days.sumOf { it.completedCount },
            conflicts = planning.days.filter { it.conflictCount > 0 }.map { it.date.toString() },
            days = planning.days,
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), State())
}
