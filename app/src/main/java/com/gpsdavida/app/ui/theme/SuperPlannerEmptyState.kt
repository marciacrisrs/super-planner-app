package com.superplanner.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Small, consistent icon vocabulary for planner concepts, drawn locally to avoid icon-pack coupling. */
enum class SuperPlannerIcon {
    CALENDAR,
    EVENT,
    COMPLETED,
    GOAL,
    IDEA,
}

@Composable
fun SuperPlannerIcon(
    icon: SuperPlannerIcon,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    ) {
        val stroke = size.minDimension * 0.09f
        val color = SuperPlannerColors.Terracotta
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.34f

        when (icon) {
            SuperPlannerIcon.CALENDAR -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.24f),
                    size = Size(size.width * 0.64f, size.height * 0.58f),
                    cornerRadius = CornerRadius(stroke * 1.5f),
                    style = Stroke(stroke),
                )
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.40f), Offset(size.width * 0.82f, size.height * 0.40f), stroke)
                drawLine(color, Offset(size.width * 0.35f, size.height * 0.16f), Offset(size.width * 0.35f, size.height * 0.32f), stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.65f, size.height * 0.16f), Offset(size.width * 0.65f, size.height * 0.32f), stroke, cap = StrokeCap.Round)
            }
            SuperPlannerIcon.EVENT -> {
                drawCircle(color, radius, center, style = Stroke(stroke))
                drawCircle(color, radius * 0.22f, center)
            }
            SuperPlannerIcon.COMPLETED -> {
                drawCircle(color, radius, center, style = Stroke(stroke))
                val path = Path().apply {
                    moveTo(size.width * 0.31f, size.height * 0.51f)
                    lineTo(size.width * 0.45f, size.height * 0.64f)
                    lineTo(size.width * 0.70f, size.height * 0.38f)
                }
                drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            SuperPlannerIcon.GOAL -> {
                drawCircle(color, radius, center, style = Stroke(stroke))
                drawCircle(color, radius * 0.55f, center, style = Stroke(stroke))
                drawCircle(color, radius * 0.16f, center)
            }
            SuperPlannerIcon.IDEA -> {
                drawCircle(color, radius * 0.72f, Offset(center.x, center.y * 0.92f), style = Stroke(stroke))
                drawLine(color, Offset(size.width * 0.40f, size.height * 0.76f), Offset(size.width * 0.60f, size.height * 0.76f), stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.43f, size.height * 0.85f), Offset(size.width * 0.57f, size.height * 0.85f), stroke, cap = StrokeCap.Round)
            }
        }
    }
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
                .background(SuperPlannerColors.RoseSoft, CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuperPlannerIcon(
                icon = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = SuperPlannerColors.Ink,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = SuperPlannerColors.InkSoft,
        )
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
