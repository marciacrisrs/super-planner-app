package com.gpsdavida.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Reusable visual primitives for the Super Planner editorial language. */
@Composable
fun SuperPlannerCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GpsDaVidaColors.SurfaceWarm),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content,
    )
}

@Composable
fun SuperPlannerSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GpsDaVidaColors.Ink,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GpsDaVidaColors.InkSoft,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun SuperPlannerProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = GpsDaVidaColors.InkSoft,
            )
        }
        LinearProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(safeProgress, 0f..1f)
                    label?.let { contentDescription = it }
                },
            color = GpsDaVidaColors.Terracotta,
            trackColor = GpsDaVidaColors.RoseSoft,
        )
    }
}

/** The visual state of an item in the planner timeline. */
enum class SuperPlannerTimelineState {
    COMPLETED,
    CURRENT,
    UPCOMING,
}

data class SuperPlannerTimelineItem(
    val time: String,
    val title: String,
    val supportingText: String? = null,
    val state: SuperPlannerTimelineState = SuperPlannerTimelineState.UPCOMING,
)

/**
 * A low-density vertical timeline for day and week views.
 * The current item receives the strongest visual anchor; completed items recede.
 */
@Composable
fun SuperPlannerTimeline(
    items: List<SuperPlannerTimelineItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items.forEachIndexed { index, item ->
            SuperPlannerTimelineRow(
                item = item,
                isLast = index == items.lastIndex,
            )
        }
    }
}

@Composable
private fun SuperPlannerTimelineRow(
    item: SuperPlannerTimelineItem,
    isLast: Boolean,
) {
    val accent = when (item.state) {
        SuperPlannerTimelineState.CURRENT -> GpsDaVidaColors.Terracotta
        SuperPlannerTimelineState.COMPLETED -> GpsDaVidaColors.Sage
        SuperPlannerTimelineState.UPCOMING -> GpsDaVidaColors.BlueGray
    }
    val textAlpha = when (item.state) {
        SuperPlannerTimelineState.COMPLETED -> 0.62f
        else -> 1f
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = item.time,
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.labelMedium,
            color = GpsDaVidaColors.InkSoft.copy(alpha = textAlpha),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp),
        ) {
            Spacer(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(if (item.state == SuperPlannerTimelineState.CURRENT) 10.dp else 8.dp)
                    .background(accent, CircleShape),
            )
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(1.dp)
                        .height(48.dp)
                        .background(GpsDaVidaColors.Outline.copy(alpha = 0.8f)),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = if (isLast) 0.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = if (item.state == SuperPlannerTimelineState.CURRENT) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = GpsDaVidaColors.Ink.copy(alpha = textAlpha),
            )
            item.supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = GpsDaVidaColors.InkSoft.copy(alpha = textAlpha),
                )
            }
        }
    }
}

@Composable
fun SuperPlannerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GpsDaVidaColors.Terracotta,
            contentColor = GpsDaVidaColors.Surface,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SuperPlannerSecondaryActions(
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GpsDaVidaColors.InkSoft,
            ),
        ) {
            Text(secondaryText, style = MaterialTheme.typography.labelMedium)
        }
        Button(
            onClick = onPrimary,
            modifier = Modifier.weight(1.35f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GpsDaVidaColors.Terracotta,
                contentColor = GpsDaVidaColors.Surface,
            ),
        ) {
            Text(primaryText, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SuperPlannerMetadata(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { (text, color) ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}
