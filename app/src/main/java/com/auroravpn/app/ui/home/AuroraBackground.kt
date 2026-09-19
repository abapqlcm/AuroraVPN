package com.auroravpn.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.auroravpn.app.ui.design.AuroraColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The midnight field the whole application sits on.
 *
 * Two radial glows and a set of faint diagonal lines. Nothing here moves and
 * nothing here allocates: the whole thing is drawn once per size change.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // The base coat.
        drawRect(color = AuroraColors.Background)

        // A cyan glow centred on the upper third, where the orbit sits.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraColors.AccentMint.copy(alpha = 0.07f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.5f, size.height * 0.28f),
                radius = size.minDimension * 0.75f,
            ),
        )

        // A blue glow lower left, to keep the bottom from going dead black.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraColors.Blue.copy(alpha = 0.06f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.15f, size.height * 0.8f),
                radius = size.minDimension * 0.6f,
            ),
        )

        // Faint diagonal technical lines toward the upper right.
        val lineColor = AuroraColors.Translucency.BorderWeak
        val dotted = PathEffect.dashPathEffect(floatArrayOf(1f, 18f), 0f)
        val span = size.width + size.height
        var distance = size.width * 0.45f
        while (distance < span) {
            val start = Offset(size.width, distance - size.width)
            val end = Offset(distance, 0f)
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = 1f,
                pathEffect = dotted,
            )
            distance += 84f
        }
    }
}

/**
 * A smooth waveform of the traffic the tunnel is actually carrying.
 *
 * [history] is a bounded series of samples in [0, 1] the ViewModel holds; this
 * draws it. An empty list is an inactive connection, and the line stays flat
 * rather than pretending motion exists.
 */
@Composable
fun TrafficWaveform(
    history: List<Float>,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    secondHistory: List<Float> = emptyList(),
    filled: Boolean = false,
) {
    Canvas(modifier = modifier) {
        if (history.isEmpty()) {
            // A quiet baseline, so an empty card still reads as a graph.
            drawLine(
                color = AuroraColors.Translucency.BorderWeak,
                start = Offset(0f, size.height * 0.7f),
                end = Offset(size.width, size.height * 0.7f),
                strokeWidth = 1f,
            )
            return@Canvas
        }

        // The primary trace, cyan, drawn once as a glow and once as a line.
        drawSeries(
            history = history,
            color = if (active) AuroraColors.BrightMint else AuroraColors.TextMuted.copy(alpha = 0.4f),
            glow = AuroraColors.AccentMint.copy(alpha = if (active) 0.22f else 0.06f),
            filled = filled,
        )
        // The secondary trace, blue, underneath. Drawn after so a crossing
        // reads as two channels rather than as a broken line.
        if (secondHistory.isNotEmpty()) {
            drawSeries(
                history = secondHistory,
                color = if (active) AuroraColors.BlueSecondary else AuroraColors.TextMuted.copy(alpha = 0.3f),
                glow = AuroraColors.Blue.copy(alpha = if (active) 0.16f else 0.04f),
                filled = false,
            )
        }
    }
}

/**
 * One series: a smooth curve through the samples, with an optional glow
 * underlay and an optional translucent fill to the baseline.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeries(
    history: List<Float>,
    color: Color,
    glow: Color,
    filled: Boolean,
) {
    val stepX = if (history.size > 1) size.width / (history.size - 1) else 0f
    // Catmull-Rom to Bézier: a curve through every sample rather than a
    // polyline, which is what makes the trace read as traffic.
    val points = history.mapIndexed { index, value ->
        Offset(
            x = index * stepX,
            y = size.height * (1f - value.coerceIn(0f, 1f)) * 0.85f + size.height * 0.075f,
        )
    }

    // The glow underlay: the same path, wider and translucent.
    drawPathThrough(points = points, color = glow, width = 5f)
    // The fill: the area under the curve, fading toward the baseline.
    if (filled) {
        val fill = androidx.compose.ui.graphics.Path()
        fill.moveTo(points.first().x, size.height)
        points.forEach { fill.lineTo(it.x, it.y) }
        fill.lineTo(points.last().x, size.height)
        fill.close()
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
            ),
        )
    }
    drawPathThrough(points = points, color = color, width = 1.8f)
}

/**
 * Draws a smooth curve through [points] using cubic segments derived from the
 * neighbours of each pair, so the line passes through every sample.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathThrough(
    points: List<Offset>,
    color: Color,
    width: Float,
) {
    val path = androidx.compose.ui.graphics.Path()
    if (points.isEmpty()) return
    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) {
        path.lineTo(points.first().x, points.first().y)
    } else {
        for (i in 0 until points.size - 1) {
            val previous = points.getOrElse(i - 1) { points[i] }
            val current = points[i]
            val next = points[i + 1]
            val afterNext = points.getOrElse(i + 2) { next }

            // Control points a quarter of the way to the neighbours, which is
            // what keeps the curve from overshooting a spike.
            val control1 = Offset(
                x = current.x + (next.x - previous.x) / 6f,
                y = current.y + (next.y - previous.y) / 6f,
            )
            val control2 = Offset(
                x = next.x - (afterNext.x - current.x) / 6f,
                y = next.y - (afterNext.y - current.y) / 6f,
            )
            path.cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
        }
    }
    drawPath(path = path, color = color, style = Stroke(width = width))
}