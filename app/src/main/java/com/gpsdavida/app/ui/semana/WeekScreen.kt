package com.superplanner.app.ui.semana

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.WeeklyDaySummary
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerMetadata
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeekScreen(
    onOpenDay: (String) -> Unit,
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR"))
    val currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Semana", style = MaterialTheme.typography.headlineSmall, color = SuperPlannerColors.Ink)
                Text(
                    "${state.startDate.format(formatter)} — ${state.endDate.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }
            Row {
                IconButton(onClick = viewModel::previousWeek) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Semana anterior")
                }
                IconButton(onClick = viewModel::nextWeek) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Próxima semana")
                }
            }
        }

        if (state.startDate != currentWeekStart) {
            TextButton(onClick = viewModel::currentWeek) { Text("Voltar para esta semana") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.days, key = { it.date.toString() }) { day ->
                WeeklyDayCard(day = day, onOpenDay = onOpenDay)
            }
        }
    }
}

@Composable
private fun WeeklyDayCard(
    day: WeeklyDaySummary,
    onOpenDay: (String) -> Unit,
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("pt", "BR"))
    SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        day.date.format(dayFormatter).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = SuperPlannerColors.Ink,
                    )
                    Text(
                        day.date.format(DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR"))),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuperPlannerColors.InkSoft,
                    )
                }
                TextButton(onClick = { onOpenDay(day.date.toString()) }) {
                    Text("Abrir dia")
                }
            }

            SuperPlannerMetadata(
                items = listOf(
                    "${day.plannedCount} atividades" to SuperPlannerColors.InkSoft,
                    "${day.plannedDuration.toMinutes()} min" to SuperPlannerColors.InkSoft,
                    "${(day.completionRatio * 100).toInt()}%" to SuperPlannerColors.Terracotta,
                ),
            )

            if (day.activities.isEmpty()) {
                Text(
                    stringResource(R.string.week_empty_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            } else {
                day.activities.take(4).forEach { activity ->
                    Text(
                        "• ${activity.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuperPlannerColors.Ink,
                    )
                }
                if (day.activities.size > 4) {
                    Text(
                        "+ ${day.activities.size - 4} atividades",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuperPlannerColors.InkSoft,
                    )
                }
            }
        }
    }
}
