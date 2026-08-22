package com.gpsdavida.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/** Editorial home composition: one anchor, one overview, and a calm view of what follows. */
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = greeting,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = GpsDaVidaColors.Ink,
            )
            Text(
                text = state.currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = GpsDaVidaColors.InkSoft,
            )
        }

        SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                SuperPlannerSectionHeader(
                    title = "Agora",
                    supportingText = state.currentTime.format(timeFormatter),
                )
                NextActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    model = NextActionUiModel(
                        title = state.title,
                        durationMinutes = state.durationMinutes,
                        scheduledTime = state.scheduledTime,
                        priorityLabel = state.priority?.let { stringResource(it.labelRes()) },
                        state = state.state,
                    ),
                    oneTapComplete = true,
                    onSnooze = viewModel::deferCurrent,
                    onComplete = viewModel::completeCurrent,
                    onSwap = viewModel::skipCurrent,
                )
            }
        }

        val timeline = buildList {
            state.nextUpcoming?.let {
                add(
                    SuperPlannerTimelineItem(
                        time = it.scheduledTime.format(timeFormatter),
                        title = it.title,
                        supportingText = "${it.durationMinutes} min",
                        state = SuperPlannerTimelineState.UPCOMING,
                    ),
                )
            }
            state.laterUpcoming.forEach {
                add(
                    SuperPlannerTimelineItem(
                        time = it.scheduledTime.format(timeFormatter),
                        title = it.title,
                        supportingText = "${it.durationMinutes} min",
                        state = SuperPlannerTimelineState.UPCOMING,
                    ),
                )
            }
        }

        if (timeline.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SuperPlannerSectionHeader(
                    title = "Depois",
                    supportingText = "O que vem a seguir",
                )
                SuperPlannerTimeline(items = timeline)
            }
        }
    }
}
