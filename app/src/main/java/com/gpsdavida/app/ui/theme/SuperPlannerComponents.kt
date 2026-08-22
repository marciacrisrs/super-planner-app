package com.gpsdavida.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            Text(title, style = MaterialTheme.typography.titleLarge, color = GpsDaVidaColors.Ink)
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = GpsDaVidaColors.InkSoft)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
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
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GpsDaVidaColors.InkSoft),
        ) { Text(secondaryText, style = MaterialTheme.typography.labelMedium) }
        Button(
            onClick = onPrimary,
            modifier = Modifier.weight(1.35f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GpsDaVidaColors.Terracotta,
                contentColor = GpsDaVidaColors.Surface,
            ),
        ) { Text(primaryText, style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
fun SuperPlannerMetadata(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (text, color) ->
            Text(text, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

enum class SuperPlannerTimelineState { COMPLETED, CURRENT, UPCOMING }

data class SuperPlannerTimelineItem(
    val time: String,
    val title: String,
    val supportingText: String? = null,
    val state: SuperPlannerTimelineState = SuperPlannerTimelineState.UPCOMING,
)

@Composable
fun SuperPlannerTimeline(
    items: List<SuperPlannerTimelineItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val accent = when (item.state) {
                SuperPlannerTimelineState.CURRENT -> GpsDaVidaColors.Terracotta
                SuperPlannerTimelineState.COMPLETED -> GpsDaVidaColors.Sage
                SuperPlannerTimelineState.UPCOMING -> GpsDaVidaColors.BlueGray
            }
            val alpha = if (item.state == SuperPlannerTimelineState.COMPLETED) 0.62f else 1f
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    item.time,
                    modifier = Modifier.width(52.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = GpsDaVidaColors.InkSoft.copy(alpha = alpha),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
                    Spacer(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(if (item.state == SuperPlannerTimelineState.CURRENT) 10.dp else 8.dp)
                            .background(accent, CircleShape),
                    )
                    if (index < items.lastIndex) {
                        Spacer(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(1.dp)
                                .height(48.dp)
                                .background(GpsDaVidaColors.Outline),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, bottom = if (index == items.lastIndex) 0.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        item.title,
                        style = if (item.state == SuperPlannerTimelineState.CURRENT) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        color = GpsDaVidaColors.Ink.copy(alpha = alpha),
                    )
                    item.supportingText?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = GpsDaVidaColors.InkSoft.copy(alpha = alpha))
                    }
                }
            }
        }
    }
}
