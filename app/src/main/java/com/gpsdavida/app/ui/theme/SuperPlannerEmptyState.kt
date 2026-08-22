package com.gpsdavida.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Small, consistent icon vocabulary for planner concepts. */
enum class SuperPlannerIcon {
    CALENDAR,
    EVENT,
    COMPLETED,
    GOAL,
    IDEA,
}

private fun SuperPlannerIcon.imageVector(): ImageVector = when (this) {
    SuperPlannerIcon.CALENDAR -> Icons.Outlined.CalendarMonth
    SuperPlannerIcon.EVENT -> Icons.Outlined.EventAvailable
    SuperPlannerIcon.COMPLETED -> Icons.Outlined.CheckCircle
    SuperPlannerIcon.GOAL -> Icons.Outlined.Flag
    SuperPlannerIcon.IDEA -> Icons.Outlined.Lightbulb
}

@Composable
fun SuperPlannerIcon(
    icon: SuperPlannerIcon,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon.imageVector(),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = GpsDaVidaColors.Terracotta,
    )
}

/** Editorial empty state: useful guidance without turning the screen into a dashboard. */
@Composable
fun SuperPlannerEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: SuperPlannerIcon = SuperPlannerIcon.CALENDAR,
    iconContentDescription: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .size(56.dp)
                .background(GpsDaVidaColors.RoseSoft, CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuperPlannerIcon(
                icon = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier
                    .size(26.dp)
                    .semantics { contentDescription = iconContentDescription },
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = GpsDaVidaColors.Ink,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = GpsDaVidaColors.InkSoft,
        )
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
