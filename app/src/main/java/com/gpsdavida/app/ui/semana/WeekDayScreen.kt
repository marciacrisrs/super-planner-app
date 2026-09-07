package com.gpsdavida.app.ui.semana

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.domain.model.WeeklyDaySummary
import com.gpsdavida.app.ui.theme.GpsDaVidaColors
import com.gpsdavida.app.ui.theme.SuperPlannerCard
import com.gpsdavida.app.ui.theme.SuperPlannerMetadata
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeekDayScreen(
    date: LocalDate,
    viewModel: WeekDayViewModel = hiltViewModel(),
) {
    val state by viewModel.observeDay(date).collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineSmall,
            color = GpsDaVidaColors.Ink,
        )
        state?.let { summary ->
            DaySummary(summary)
        } ?: Text("Carregando…", color = GpsDaVidaColors.InkSoft)
    }
}

@Composable
private fun DaySummary(summary: WeeklyDaySummary) {
    SuperPlannerCard(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                SuperPlannerMetadata(
                    items = listOf(
                        "${summary.plannedCount} atividades" to GpsDaVidaColors.InkSoft,
                        "${summary.plannedDuration.toMinutes()} min" to GpsDaVidaColors.InkSoft,
                        "${(summary.completionRatio * 100).toInt()}%" to GpsDaVidaColors.Terracotta,
                    ),
                )
            }
            items(summary.activities) { activity ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(activity.title, style = MaterialTheme.typography.bodyLarge, color = GpsDaVidaColors.Ink)
                    Text(
                        "${activity.instance.planned.start.atZone(java.time.ZoneId.systemDefault()).toLocalTime()} · ${activity.instance.plannedDuration.toMinutes()} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = GpsDaVidaColors.InkSoft,
                    )
                }
            }
            if (summary.activities.isEmpty()) {
                item {
                    Text("Nada planejado para este dia.", color = GpsDaVidaColors.InkSoft)
                }
            }
        }
    }
}
