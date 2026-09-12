package com.gpsdavida.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.R
import com.gpsdavida.app.ui.agora.AgoraViewModel
import com.gpsdavida.app.ui.next.NextActionCard
import com.gpsdavida.app.ui.next.NextActionUiModel
import com.gpsdavida.app.ui.tasks.labelRes
import com.gpsdavida.app.ui.theme.GpsDaVidaColors
import com.gpsdavida.app.ui.theme.SuperPlannerCard
import com.gpsdavida.app.ui.theme.SuperPlannerSectionHeader
import com.gpsdavida.app.ui.theme.SuperPlannerTimeline
import com.gpsdavida.app.ui.theme.SuperPlannerTimelineItem
import com.gpsdavida.app.ui.theme.SuperPlannerTimelineState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Calm editorial home: greeting, daily anchor, then a restrained look ahead. */
@Composable
fun HomeScreen(
    viewModel: AgoraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale("pt", "BR"))
    val greeting = when (state.currentTime.hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GpsDaVidaColors.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "$greeting, Márcia! ☀️",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GpsDaVidaColors.Ink,
                )
                Text(
                    state.currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = GpsDaVidaColors.InkSoft,
                )
            }
            Text("♡", style = MaterialTheme.typography.headlineMedium, color = GpsDaVidaColors.Terracotta)
        }

        SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Seu dia", style = MaterialTheme.typography.titleLarge, color = GpsDaVidaColors.Ink)
                    Text(
                        "Um passo de cada vez. O Super Planner organiza o próximo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GpsDaVidaColors.InkSoft,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(GpsDaVidaColors.RoseSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✦", color = GpsDaVidaColors.Terracotta, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SuperPlannerSectionHeader(title = "Agora", supportingText = state.currentTime.format(timeFormatter))
            NextActionCard(
                modifier = Modifier.fillMaxWidth(),
                model = NextActionUiModel(
                    title = state.title,
                    durationMinutes = state.durationMinutes,
                    scheduledTime = state.scheduledTime,
                    priorityLabel = state.priority?.let { stringResource(it.labelRes()) },
                    reasonLabels = state.reasons.map { reason ->
                        when (reason) {
                            com.gpsdavida.app.domain.model.NextActionReason.CURRENTLY_ACTIVE -> stringResource(R.string.reason_currently_active)
                            com.gpsdavida.app.domain.model.NextActionReason.DUE_NOW -> stringResource(R.string.reason_due_now)
                            com.gpsdavida.app.domain.model.NextActionReason.HIGHER_PRIORITY -> stringResource(R.string.reason_higher_priority)
                            com.gpsdavida.app.domain.model.NextActionReason.FIXED_COMMITMENT -> stringResource(R.string.reason_fixed)
                            com.gpsdavida.app.domain.model.NextActionReason.AVAILABLE_IN_WINDOW -> stringResource(R.string.reason_available)
                            com.gpsdavida.app.domain.model.NextActionReason.ENERGY_MATCH -> stringResource(R.string.reason_energy)
                            com.gpsdavida.app.domain.model.NextActionReason.CONTEXT_MATCH -> stringResource(R.string.reason_context)
                            com.gpsdavida.app.domain.model.NextActionReason.DEPENDENCIES_SATISFIED -> stringResource(R.string.reason_dependencies)
                            com.gpsdavida.app.domain.model.NextActionReason.FLEXIBLE_SLOT -> stringResource(R.string.reason_flexible)
                            com.gpsdavida.app.domain.model.NextActionReason.TRAVEL_FITS -> stringResource(R.string.reason_travel)
                        }
                    },
                    state = state.state,
                ),
                oneTapComplete = true,
                onSnooze = viewModel::deferCurrent,
                onComplete = viewModel::completeCurrent,
                onSwap = viewModel::skipCurrent,
            )
        }

        val timeline = buildList {
            state.nextUpcoming?.let {
                add(SuperPlannerTimelineItem(it.scheduledTime.format(timeFormatter), it.title, "${it.durationMinutes} min", SuperPlannerTimelineState.UPCOMING))
            }
            state.laterUpcoming.forEach {
                add(SuperPlannerTimelineItem(it.scheduledTime.format(timeFormatter), it.title, "${it.durationMinutes} min", SuperPlannerTimelineState.UPCOMING))
            }
        }

        if (timeline.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SuperPlannerSectionHeader(title = "Depois", supportingText = "O que vem a seguir")
                SuperPlannerTimeline(items = timeline)
            }
        }
    }
}
