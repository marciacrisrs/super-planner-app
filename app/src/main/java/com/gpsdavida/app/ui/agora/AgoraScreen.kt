package com.superplanner.app.ui.agora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.RouteFeedbackReason
import com.superplanner.app.ui.next.NextActionCard
import com.superplanner.app.ui.next.NextActionUiModel
import com.superplanner.app.ui.tasks.labelRes
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.widget.AgoraWidgetProvider
import com.superplanner.app.ui.widget.AgoraWidgetSnapshot
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun AgoraScreen(
    viewModel: AgoraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentActivity = state.currentActivity
    var showCapacityDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackGivenFor by remember { mutableStateOf<String?>(null) }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale("pt", "BR"))

    LaunchedEffect(currentActivity?.id) {
        feedbackGivenFor = null
    }

    LaunchedEffect(state.title, state.scheduledTime, state.durationMinutes, currentActivity, state.state, state.lowCapacity) {
        AgoraWidgetSnapshot.write(
            context = context,
            snapshot = AgoraWidgetSnapshot(
                title = state.title,
                scheduledTime = state.scheduledTime?.format(timeFmt).orEmpty(),
                durationMinutes = state.durationMinutes?.toInt() ?: 0,
                isEmpty = currentActivity == null,
            ),
        )
        AgoraWidgetProvider.updateAll(context)
    }

    if (showCapacityDialog) {
        AlertDialog(
            onDismissRequest = { showCapacityDialog = false },
            title = { Text(stringResource(R.string.low_capacity_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        if (state.lowCapacity) R.string.low_capacity_dialog_active_body else R.string.low_capacity_dialog_body,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setLowCapacity(!state.lowCapacity)
                    showCapacityDialog = false
                }) {
                    Text(stringResource(if (state.lowCapacity) R.string.low_capacity_restore else R.string.low_capacity_enable))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapacityDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showFeedbackDialog && currentActivity != null) {
        val feedbackActivityId = currentActivity.id.value
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text(stringResource(R.string.agora_feedback_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackOption(stringResource(R.string.agora_feedback_helpful), RouteFeedbackReason.HELPFUL, viewModel) {
                        showFeedbackDialog = false
                        feedbackGivenFor = feedbackActivityId
                    }
                    FeedbackOption(stringResource(R.string.agora_feedback_other), RouteFeedbackReason.WANTED_OTHER, viewModel) {
                        showFeedbackDialog = false
                        feedbackGivenFor = feedbackActivityId
                    }
                    FeedbackOption(stringResource(R.string.agora_feedback_duration), RouteFeedbackReason.DURATION_WRONG, viewModel) {
                        showFeedbackDialog = false
                        feedbackGivenFor = feedbackActivityId
                    }
                    FeedbackOption(stringResource(R.string.agora_feedback_time), RouteFeedbackReason.TIME_WRONG, viewModel) {
                        showFeedbackDialog = false
                        feedbackGivenFor = feedbackActivityId
                    }
                    FeedbackOption(stringResource(R.string.agora_feedback_wrong), RouteFeedbackReason.PLANNER_WRONG, viewModel) {
                        showFeedbackDialog = false
                        feedbackGivenFor = feedbackActivityId
                    }
                }
            },
            confirmButton = {},
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(R.string.nav_agora), style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.TerracottaDark)
            Text(text = state.currentTime.format(timeFmt), style = MaterialTheme.typography.displaySmall, color = SuperPlannerColors.Ink)
            Text(text = state.currentDate.format(dateFmt), style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.InkSoft)
        }

        CapacityContextCard(
            remainingMinutes = state.capacityRemainingMinutes,
            nextWindowMinutes = state.nextWindowMinutes,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showCapacityDialog = true },
        ) {
            Text(stringResource(if (state.lowCapacity) R.string.low_capacity_active_button else R.string.low_capacity_button))
        }

        if (state.lowCapacity) {
            Text(
                text = stringResource(
                    R.string.low_capacity_summary,
                    state.lowCapacitySummary.preserved,
                    state.lowCapacitySummary.moved,
                    state.lowCapacitySummary.deferred,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = SuperPlannerColors.InkSoft,
            )
        }

        NextActionCard(
            modifier = Modifier.fillMaxWidth(),
            model = NextActionUiModel(
                title = state.title,
                durationMinutes = state.durationMinutes,
                scheduledTime = state.scheduledTime,
                priorityLabel = state.priority?.let { stringResource(it.labelRes()) },
                explanation = state.explanation,
                state = state.state,
            ),
            oneTapComplete = false,
            onStart = viewModel::startCurrent,
            onSnooze = viewModel::deferCurrent,
            onComplete = viewModel::completeCurrent,
            onSwap = viewModel::skipCurrent,
        )

        if (currentActivity != null && feedbackGivenFor != currentActivity.id.value) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showFeedbackDialog = true },
            ) {
                Text(stringResource(R.string.agora_feedback_title))
            }
        } else if (feedbackGivenFor == currentActivity?.id?.value) {
            Text(
                text = stringResource(R.string.agora_feedback_recorded),
                style = MaterialTheme.typography.bodySmall,
                color = SuperPlannerColors.InkSoft,
            )
        }

        if (state.nextUpcoming != null || state.laterUpcoming.isNotEmpty()) {
            AgoraUpcomingSection(next = state.nextUpcoming, later = state.laterUpcoming)
        }
    }
}

@Composable
private fun FeedbackOption(
    label: String,
    reason: RouteFeedbackReason,
    viewModel: AgoraViewModel,
    onRecorded: () -> Unit,
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            viewModel.recordCurrentFeedback(reason)
            onRecorded()
        },
    ) {
        Text(label)
    }
}
