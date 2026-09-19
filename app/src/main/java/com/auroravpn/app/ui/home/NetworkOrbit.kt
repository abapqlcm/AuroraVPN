package com.auroravpn.app.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.auroravpn.app.ui.design.AuroraColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The orbital globe that is the visual centre of the application.
 *
 * A 2.5D Canvas composition rather than a 3D engine: nine layers drawn back to
 * front, each a handful of path operations, and one slow animation driving the
 * two that move. On a frame this costs microseconds; a real scene graph would
 * cost megabytes and a GPU context the battery pays for.
 *
 * The [secure] parameter is the only thing that changes what is drawn, and it
 * comes from the connection state -- not from a default, and not from here.
 */
@Composable
fun NetworkOrbit(
    secure: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val corePulse by rememberInfiniteTransition(label = "core-pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "core-pulse-value",
    )

    // One orbit speed, shared by every ring, so the whole thing moves as one
    // slow system instead of several unrelated ones.
    val orbit by rememberInfiniteTransition(label = "orbit").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40_000, easing = LinearEasing),
        ),
        label = "orbit-value",
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f)
            .semantics {
                contentDescription = if (secure) "Network orbit, connection secure" else "Network orbit, no connection"
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. The ambient glow the sphere sits in.
            drawAmbientGlow(center, radius, secure)

            // 2. Faint dotted rings, the widest geometry on screen.
            drawDottedRings(center, radius)

            // 3. The crosshair, fading toward its ends.
            drawCrosshair(center, radius)

            // 4-5. Two tilted elliptical orbits carrying nodes.
            drawOrbitWithNodes(center, radius, orbit, tilt = -22f, ringColor = AuroraColors.BlueSecondary)
            drawOrbitWithNodes(center, radius, orbit * 0.7f + 120f, tilt = 28f, ringColor = AuroraColors.AccentMint)

            // 6. The glass sphere itself, shaded spherically.
            drawGlassSphere(center, radius)

            // 7. Latitude and longitude curves across the sphere.
            drawSphereGrid(center, radius)

            // 8. The bright core, breathing.
            drawCore(center, radius, corePulse, secure)

            // 9. A rim highlight on the upper left, where the light is.
            drawRimLight(center, radius)
        }
    }
}

// ---------------------------------------------------------------- layers ----

private fun DrawScope.drawAmbientGlow(center: Offset, radius: Float, secure: Boolean) {
    val core = if (secure) AuroraColors.AccentMint else AuroraColors.Blue
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                core.copy(alpha = 0.22f),
                core.copy(alpha = 0.07f),
                Color.Transparent,
            ),
            center = center,
            radius = radius * 1.5f,
        ),
        center = center,
        radius = radius * 1.5f,
    )
}

private fun DrawScope.drawDottedRings(center: Offset, radius: Float) {
    val dotted = PathEffect.dashPathEffect(floatArrayOf(2f, 10f), 0f)
    for (factor in floatArrayOf(0.68f, 0.84f)) {
        drawCircle(
            color = AuroraColors.Translucency.BorderWeak,
            radius = radius * factor,
            center = center,
            style = Stroke(width = 1.dp.toPx(), pathEffect = dotted),
        )
    }
}

private fun DrawScope.drawCrosshair(center: Offset, radius: Float) {
    val reach = radius * 0.92f
    // Brightest at the core, gone at the ends: a gradient per arm.
    for (angle in floatArrayOf(0f, 90f, 180f, 270f)) {
        val radians = angle * PI.toFloat() / 180f
        val end = Offset(
            x = center.x + cos(radians) * reach,
            y = center.y + sin(radians) * reach,
        )
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    AuroraColors.BrightMint.copy(alpha = 0.45f),
                    Color.Transparent,
                ),
                start = center,
                end = end,
            ),
            start = center,
            end = end,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawOrbitWithNodes(
    center: Offset,
    radius: Float,
    angle: Float,
    tilt: Float,
    ringColor: Color,
) {
    val a = radius * 0.9f // Semi-major axis: wider than the sphere.
    val b = radius * 0.34f // Semi-minor: the tilt, foreshortened.

    rotate(degrees = tilt, pivot = center) {
        drawOval(
            color = ringColor.copy(alpha = 0.35f),
            topLeft = Offset(center.x - a, center.y - b),
            size = androidx.compose.ui.geometry.Size(a * 2, b * 2),
            style = Stroke(width = 1.2.dp.toPx()),
        )

        // Three nodes riding this ring. Placed on the ellipse and carried by
        // the shared angle, so they pass behind and in front of the sphere.
        for (i in 0 until 3) {
            val t = angle + i * 120f
            val radians = t * PI.toFloat() / 180f
            val point = Offset(
                x = center.x + a * cos(radians),
                y = center.y + b * sin(radians),
            )
            val behind = sin(radians) < 0f
            val alpha = if (behind) 0.35f else 1f
            drawNode(point, ringColor, alpha = alpha, radius = if (behind) 2.dp.toPx() else 3.dp.toPx())
        }
    }
}

private fun DrawScope.drawNode(
    point: Offset,
    color: Color,
    alpha: Float,
    radius: Float,
) {
    // The halo first, so the bright centre draws over it.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.5f * alpha), Color.Transparent),
            center = point,
            radius = radius * 3.5f,
        ),
        center = point,
        radius = radius * 3.5f,
    )
    drawCircle(
        color = color.copy(alpha = alpha),
        center = point,
        radius = radius,
    )
}

private fun DrawScope.drawGlassSphere(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF0E1A28),
                Color(0xFF0B111A),
            ),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.3f),
            radius = radius * 1.1f,
        ),
        center = center,
        radius = radius * 0.78f,
    )
    // The dark outer rim that reads as glass rather than as a flat disc.
    drawCircle(
        color = AuroraColors.Translucency.BorderStrong,
        center = center,
        radius = radius * 0.78f,
        style = Stroke(width = 1.dp.toPx()),
    )
}

private fun DrawScope.drawSphereGrid(center: Offset, radius: Float) {
    val r = radius * 0.78f
    // Latitude: ellipses that flatten toward the poles.
    for (i in 1..3) {
        val factor = i / 4f
        drawOval(
            color = AuroraColors.Blue.copy(alpha = 0.14f),
            topLeft = Offset(center.x - r, center.y - r * factor),
            size = androidx.compose.ui.geometry.Size(r * 2, r * factor * 2),
            style = Stroke(width = 0.8.dp.toPx()),
        )
    }
    // Longitude: ellipses that narrow toward the edges, the meridians.
    for (i in 1..3) {
        val factor = i / 4f
        drawOval(
            color = AuroraColors.AccentMint.copy(alpha = 0.10f),
            topLeft = Offset(center.x - r * factor, center.y - r),
            size = androidx.compose.ui.geometry.Size(r * factor * 2, r * 2),
            style = Stroke(width = 0.8.dp.toPx()),
        )
    }
}

private fun DrawScope.drawCore(center: Offset, radius: Float, pulse: Float, secure: Boolean) {
    val coreRadius = radius * (0.14f + 0.03f * pulse)
    val coreColor = if (secure) AuroraColors.BrightMint else AuroraColors.BlueSecondary

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                coreColor.copy(alpha = 0.8f),
                coreColor.copy(alpha = 0.0f),
            ),
            center = center,
            radius = coreRadius * 2.4f,
        ),
        center = center,
        radius = coreRadius * 2.4f,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        center = center,
        radius = coreRadius * 0.5f,
    )
}

private fun DrawScope.drawRimLight(center: Offset, radius: Float) {
    drawArc(
        color = AuroraColors.BrightMint.copy(alpha = 0.30f),
        startAngle = 130f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.78f, center.y - radius * 0.78f),
        size = androidx.compose.ui.geometry.Size(radius * 1.56f, radius * 1.56f),
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
    )
}
