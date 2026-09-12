package com.superplanner.app.ui.next

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.superplanner.app.R
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerMetadata
import com.superplanner.app.ui.theme.SuperPlannerPrimaryButton
import com.superplanner.app.ui.theme.SuperPlannerSecondaryActions
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class NextActionUiModel(
    val title: String,
    val durationMinutes: Long? = null,
    val scheduledTime: LocalTime? = null,
    val priorityLabel: String? = null,
    val contextLabel: String? = null,
    val reasonLabels: List<String> = emptyList(),
    val state: NextActionState = NextActionState.Ready,
)

enum class NextActionState { Ready, InProgress, Completed, Empty }

@Composable
fun NextActionCard(
    model: NextActionUiModel,
    onStart: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onComplete: () -> Unit = {},
    onSwap: () -> Unit = {},
    oneTapComplete: Boolean = false,
    modifier: Modifier = Modifier,
) {
    SuperPlannerCard(modifier = modifier.fillMaxWidth()) {
        when (model.state) {
            NextActionState.Empty -> EmptyContent()
            NextActionState.Completed -> CompletedContent(model.title)
            NextActionState.Ready, NextActionState.InProgress -> ReadyContent(model, onStart, onSnooze, onComplete, onSwap, oneTapComplete)
        }
    }
}

@Composable
private fun ReadyContent(
    model: NextActionUiModel,
    onStart: () -> Unit,
    onSnooze: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    oneTapComplete: Boolean,
) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NextLabel()
        Text(model.title, style = MaterialTheme.typography.headlineMedium, color = SuperPlannerColors.Ink)
        MetadataRow(model)
        if (model.reasonLabels.isNotEmpty()) {
            Text(
                text = model.reasonLabels.take(3).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = SuperPlannerColors.InkSoft,
            )
        }
        Spacer(modifier = Modifier.size(1.dp))
        SuperPlannerPrimaryButton(
            text = stringResource(if (oneTapComplete || model.state == NextActionState.InProgress) R.string.next_action_complete else R.string.next_action_start),
            onClick = if (oneTapComplete || model.state == NextActionState.InProgress) onComplete else onStart,
        )
        SuperPlannerSecondaryActions(
            primaryText = stringResource(R.string.next_action_swap),
            onPrimary = onSwap,
            secondaryText = stringResource(R.string.next_action_snooze),
            onSecondary = onSnooze,
        )
    }
}

@Composable
private fun MetadataRow(model: NextActionUiModel) {
    val items = buildList {
        model.scheduledTime?.let { add(it.format(DateTimeFormatter.ofPattern("HH:mm")) to SuperPlannerColors.TerracottaDark) }
        model.durationMinutes?.let { add(stringResource(R.string.next_action_duration, it) to SuperPlannerColors.Warning) }
        model.priorityLabel?.let { add(it to SuperPlannerColors.Rose) }
        model.contextLabel?.let { add(it to SuperPlannerColors.Sage) }
    }
    SuperPlannerMetadata(items)
}

@Composable
private fun NextLabel() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("♡", style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.Terracotta)
        Text(stringResource(R.string.next_action_label), style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.TerracottaDark)
    }
}

@Composable
private fun EmptyContent() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = SuperPlannerColors.Sage, modifier = Modifier.size(28.dp))
        Text(stringResource(R.string.next_action_empty_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.next_action_empty_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompletedContent(title: String) {
    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = SuperPlannerColors.Success, modifier = Modifier.size(44.dp))
        Column {
            Text(stringResource(R.string.next_action_completed_label), style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.Success)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
