package com.superplanner.app.ui.semana

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.WeeklyDaySummary
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerMetadata
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PtBr = Locale("pt", "BR")
private val WeekRangeFormatter = DateTimeFormatter.ofPattern("d MMM", PtBr)
private val DayFormatter = DateTimeFormatter.ofPattern("EEEE", PtBr)

@Composable
fun WeekScreen(
    onOpenDay: (String) -> Unit,
    onOrganizeWeek: () -> Unit = {},
    viewModel: WeekViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SuperPlannerColors.Canvas)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("Semana", style = MaterialTheme.typography.headlineSmall, color = SuperPlannerColors.Ink, fontWeight = FontWeight.SemiBold)
                Text("${state.startDate.format(WeekRangeFormatter)} — ${state.endDate.format(WeekRangeFormatter)}", style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.InkSoft)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AgendaNavigationButton("Semana anterior", viewModel::previousWeek) { Icon(Icons.Filled.ArrowBack, null) }
                AgendaNavigationButton("Próxima semana", viewModel::nextWeek) { Icon(Icons.Filled.ArrowForward, null) }
            }
        }

        Button(onClick = onOrganizeWeek, modifier = Modifier.fillMaxWidth()) {
            Text("✨ Organize sua semana")
        }

        if (state.startDate != currentWeekStart) {
            TextButton(onClick = viewModel::currentWeek) { Text("Voltar para esta semana", color = SuperPlannerColors.Terracotta) }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(state.days, key = { it.date.toString() }) { day -> WeeklyDayCard(day = day, onOpenDay = onOpenDay) }
        }
    }
}

@Composable
private fun AgendaNavigationButton(contentDescription: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = SuperPlannerColors.Surface, contentColor = SuperPlannerColors.InkSoft) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) { icon() }
    }
}

@Composable
private fun WeeklyDayCard(day: WeeklyDaySummary, onOpenDay: (String) -> Unit) {
    val isToday = day.date == LocalDate.now()
    val dayName = day.date.format(DayFormatter).replaceFirstChar { it.uppercase(PtBr) }
    SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    DateBadge(day.date.dayOfMonth, isToday)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(dayName, style = MaterialTheme.typography.titleLarge, color = SuperPlannerColors.Ink)
                            if (isToday) Text("Hoje", style = MaterialTheme.typography.labelSmall, color = SuperPlannerColors.Terracotta)
                        }
                        Text(day.date.format(WeekRangeFormatter), style = MaterialTheme.typography.bodySmall, color = SuperPlannerColors.InkSoft)
                    }
                }
                TextButton(onClick = { onOpenDay(day.date.toString()) }) { Text("Abrir dia", color = SuperPlannerColors.Terracotta) }
            }
            SuperPlannerMetadata(items = listOf("${day.plannedCount} atividades" to SuperPlannerColors.InkSoft, "${day.plannedDuration.toMinutes()} min" to SuperPlannerColors.InkSoft, "${(day.completionRatio * 100).toInt()}% concluído" to SuperPlannerColors.Terracotta))
            Spacer(modifier = Modifier.size(1.dp))
            if (day.activities.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), color = SuperPlannerColors.SurfaceWarm, shape = RoundedCornerShape(18.dp)) {
                    Text(stringResource(R.string.week_empty_day), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.InkSoft)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    day.activities.take(4).forEachIndexed { index, activity ->
                        WeeklyTaskCard(activity.title, when (index % 3) { 0 -> SuperPlannerColors.Terracotta; 1 -> SuperPlannerColors.Sage; else -> SuperPlannerColors.BlueGray })
                    }
                    if (day.activities.size > 4) Text("+ ${day.activities.size - 4} atividades", modifier = Modifier.padding(start = 12.dp, top = 2.dp), style = MaterialTheme.typography.labelMedium, color = SuperPlannerColors.InkSoft)
                }
            }
        }
    }
}

@Composable
private fun DateBadge(day: Int, isToday: Boolean) {
    Surface(shape = RoundedCornerShape(16.dp), color = if (isToday) SuperPlannerColors.TerracottaSoft else SuperPlannerColors.SurfaceWarm, contentColor = if (isToday) SuperPlannerColors.TerracottaDark else SuperPlannerColors.Ink) {
        Text(day.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp))
    }
}

@Composable
private fun WeeklyTaskCard(title: String, accent: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = SuperPlannerColors.SurfaceWarm, shape = RoundedCornerShape(18.dp)) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(modifier = Modifier.size(width = 4.dp, height = 30.dp).background(accent, RoundedCornerShape(50)))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = SuperPlannerColors.Ink)
                Text("Atividade planejada", style = MaterialTheme.typography.labelSmall, color = SuperPlannerColors.InkSoft)
            }
        }
    }
}
