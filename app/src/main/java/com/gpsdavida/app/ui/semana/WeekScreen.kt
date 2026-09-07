package com.gpsdavida.app.ui.semana

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.gpsdavida.app.R
import com.gpsdavida.app.domain.model.WeeklyDaySummary
import com.gpsdavida.app.ui.theme.GpsDaVidaColors
import com.gpsdavida.app.ui.theme.SuperPlannerCard
import com.gpsdavida.app.ui.theme.SuperPlannerMetadata
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun WeekScreen(
    onOpenDay: (String) -> Unit,
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val startDate by viewModel.selectedStartDate.collectAsStateWithLifecycle()
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR"))

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
                Text("Semana", style = MaterialTheme.typography.headlineSmall, color = GpsDaVidaColors.Ink)
                Text(
                    "${state.startDate.format(formatter)} — ${state.endDate.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GpsDaVidaColors.InkSoft,
                )
            }
            Row {
                IconButton(onClick = viewModel::previousWeek) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior")
                }
                IconButton(onClick = viewModel::nextWeek) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próxima semana")
                }
            }
        }

        if (!state.startDate.isEqual(startDate)) {
            TextButton(onClick = viewModel::currentWeek) { Text("Voltar para esta semana") }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                        color = GpsDaVidaColors.Ink,
                    )
                    Text(
                        day.date.format(DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR"))),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GpsDaVidaColors.InkSoft,
                    )
                }
                TextButton(onClick = { onOpenDay(day.date.toString()) }) {
                    Text("Abrir dia")
                }
            }

            SuperPlannerMetadata(
                items = listOf(
                    "${day.plannedCount} atividades" to GpsDaVidaColors.InkSoft,
                    "${day.plannedDuration.toMinutes()} min" to GpsDaVidaColors.InkSoft,
                    "${(day.completionRatio * 100).toInt()}%" to GpsDaVidaColors.Terracotta,
                ),
            )

            if (day.activities.isEmpty()) {
                Text(
                    stringResource(R.string.week_empty_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GpsDaVidaColors.InkSoft,
                )
            } else {
                day.activities.take(4).forEach { activity ->
                    Text(
                        "• ${activity.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GpsDaVidaColors.Ink,
                    )
                }
                if (day.activities.size > 4) {
                    Text(
                        "+ ${day.activities.size - 4} atividades",
                        style = MaterialTheme.typography.bodySmall,
                        color = GpsDaVidaColors.InkSoft,
                    )
                }
            }
        }
    }
}
