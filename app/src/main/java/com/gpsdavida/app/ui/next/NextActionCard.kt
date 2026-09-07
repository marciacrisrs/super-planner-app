package com.gpsdavida.app.ui.next

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gpsdavida.app.R
import com.gpsdavida.app.ui.theme.GpsDaVidaColors
import com.gpsdavida.app.ui.theme.SuperPlannerCard
import com.gpsdavida.app.ui.theme.SuperPlannerMetadata
import com.gpsdavida.app.ui.theme.SuperPlannerPrimaryButton
import com.gpsdavida.app.ui.theme.SuperPlannerSecondaryActions
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
    Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        NextLabel()
        Text(model.title, style = MaterialTheme.typography.headlineMedium, color = GpsDaVidaColors.Ink)
        MetadataRow(model)
        if (model.reasonLabels.isNotEmpty()) {
            Text(
                text = model.reasonLabels.take(3).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = GpsDaVidaColors.InkSoft,
            )
        }
        Spacer(modifier = Modifier.size(2.dp))
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
        model.scheduledTime?.let { add(it.format(DateTimeFormatter.ofPattern("HH:mm")) to GpsDaVidaColors.TerracottaDark) }
        model.durationMinutes?.let { add(stringResource(R.string.next_action_duration, it) to GpsDaVidaColors.Warning) }
        model.priorityLabel?.let { add(it to GpsDaVidaColors.Rose) }
        model.contextLabel?.let { add(it to GpsDaVidaColors.Sage) }
    }
    SuperPlannerMetadata(items)
}

@Composable
private fun NextLabel() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = GpsDaVidaColors.Terracotta, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.next_action_label), style = MaterialTheme.typography.labelLarge, color = GpsDaVidaColors.TerracottaDark)
    }
}

@Composable
private fun EmptyContent() {
    Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = GpsDaVidaColors.Sage, modifier = Modifier.size(28.dp))
        Text(stringResource(R.string.next_action_empty_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.next_action_empty_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompletedContent(title: String) {
    Row(modifier = Modifier.padding(28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = GpsDaVidaColors.Success, modifier = Modifier.size(44.dp).clip(CircleShape))
        Column {
            Text(stringResource(R.string.next_action_completed_label), style = MaterialTheme.typography.labelLarge, color = GpsDaVidaColors.Success)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
