package com.gpsdavida.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Small editorial accents for Super Planner surfaces.
 * Decoration is intentionally quiet: it should support hierarchy, never compete with content.
 */
@Composable
fun SuperPlannerEditorialAccent(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(72.dp)) {
        val stroke = 1.5.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.72f)
            cubicTo(
                size.width * 0.30f, size.height * 0.16f,
                size.width * 0.70f, size.height * 0.90f,
                size.width * 0.92f, size.height * 0.28f,
            )
        }
        drawPath(
            path = path,
            color = GpsDaVidaColors.Terracotta.copy(alpha = 0.22f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = GpsDaVidaColors.Sage.copy(alpha = 0.28f),
            radius = size.minDimension * 0.06f,
            center = Offset(size.width * 0.78f, size.height * 0.22f),
        )
    }
}

/** A subtle organic corner treatment for large planner surfaces. */
@Composable
fun SuperPlannerOrganicAccent(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rect = Rect(
                left = size.width * 0.18f,
                top = size.height * 0.10f,
                right = size.width * 0.92f,
                bottom = size.height * 0.86f,
            )
            drawArc(
                color = GpsDaVidaColors.RoseSoft.copy(alpha = 0.65f),
                startAngle = 200f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
