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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.domain.model.WeeklyDaySummary
import com.gpsdavida.app.domain.usecase.ObserveWeeklyPlanning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun WeeklyReviewScreen(viewModel: WeeklyReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        item { Text("Atenção", style = MaterialTheme.typography.titleLarge) }
        state.conflicts.forEach { conflict -> item { Card(Modifier.fillMaxWidth()) { Text("Conflito em $conflict", Modifier.padding(16.dp)) } } }
        item { Text("Resumo da semana", style = MaterialTheme.typography.titleLarge) }
        state.days.forEach { day -> item { ReviewDay(day) } }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleMedium); Text(label, style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun ReviewDay(day: WeeklyDaySummary) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(day.date.toString(), style = MaterialTheme.typography.titleMedium); Text("${day.plannedCount} atividades · ${day.plannedDuration.toMinutes()} min · ${day.completedCount} concluídas") } }
}

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    observe: ObserveWeeklyPlanning,
    clock: java.time.Clock,
) : ViewModel() {
    data class State(val plannedMinutes: Long = 0, val actualMinutes: Long = 0, val completedCount: Int = 0, val conflicts: List<String> = emptyList(), val days: List<WeeklyDaySummary> = emptyList())
    val state: StateFlow<State> = observe(LocalDate.now(clock).with(java.time.DayOfWeek.MONDAY)).map { planning -> State(planning.plannedDuration.toMinutes(), planning.actualDuration.toMinutes(), planning.days.sumOf { it.completedCount }, planning.days.filter { it.conflictCount > 0 }.map { it.date.toString() }, planning.days) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())
}
