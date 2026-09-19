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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * The orbital globe: the visual centre of the product.
 *
 * Drawn as a stack of translucent layers, back to front:
 *
 *   ambient glow → atmosphere haze → outer dotted rings → orbital ellipses →
 *   crosshair → glass sphere → sphere grid → specular core → orbital nodes.
 *
 * Each layer is a few path operations and a radial brush. Nothing is
 * re-allocated inside the draw: geometry is computed once in [OrbitGeometry]
 * and the two things that actually move share one slow animation, so a frame
 * costs microseconds and the battery never notices it.
 *
 * [secure] is the only parameter that changes what is drawn, and it arrives
 * from the connection state — never from a default here.
 */
@Composable
fun NetworkOrbit(
    secure: Boolean,
    modifier: Modifier = Modifier,
) {
    // One slow rotation shared by every orbiting element, so the whole
    // construction moves as a single system rather than several unrelated ones.
    val orbit by rememberInfiniteTransition(label = "orbit").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 46_000, easing = LinearEasing),
        ),
        label = "orbit-value",
    )

    // The core breathes on its own, much slower, out of phase with the orbit.
    val pulse by rememberInfiniteTransition(label = "core-pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "core-pulse-value",
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.62f)
            .aspectRatio(1f)
            .semantics {
                contentDescription = if (secure) "Network orbit, connection secure" else "Network orbit, no connection"
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val geometry = OrbitGeometry(size.minDimension)

            drawAmbientGlow(geometry, secure)
            drawAtmosphere(geometry)
            drawDottedRings(geometry)
            drawOrbitEllipses(geometry, orbit)
            drawCrosshair(geometry)
            drawGlassSphere(geometry)
            drawSphereGrid(geometry)
            drawSpecularCore(geometry, pulse, secure)
            drawOrbitalNodes(geometry, orbit, secure)
        }
    }
}

/**
 * The size-derived geometry, kept here so no draw call re-derives it and no
 * object beyond this is allocated per frame.
 */
private class OrbitGeometry(val size: Float) {
    val center = Offset(size / 2f, size / 2f)
    val radius = size / 2f

    /** The glass sphere, slightly inside the outer ring system. */
    val sphereRadius = radius * 0.72f

    /** Where the light comes from; the sphere is lit from the upper left. */
    val lightSource = Offset(center.x - radius * 0.28f, center.y - radius * 0.34f)
}

// ------------------------------------------------------------------ layers --

/**
 * A wide, soft radial wash behind everything. This is what makes the sphere
 * read as sitting in a lit space rather than floating on a flat sheet.
 */
private fun DrawScope.drawAmbientGlow(g: OrbitGeometry, secure: Boolean) {
    val core = if (secure) AuroraColors.BrightMint else AuroraColors.BlueSecondary
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                core.copy(alpha = 0.26f),
                core.copy(alpha = 0.10f),
                Color.Transparent,
            ),
            center = g.center,
            radius = g.radius * 1.45f,
        ),
        center = g.center,
        radius = g.radius * 1.45f,
    )
}

/**
 * The atmospheric haze: a rim of light slightly larger than the sphere, which
 * is the glass refraction the eye reads as depth.
 */
private fun DrawScope.drawAtmosphere(g: OrbitGeometry) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                AuroraColors.Blue.copy(alpha = 0.14f),
                AuroraColors.AccentMint.copy(alpha = 0.05f),
                Color.Transparent,
            ),
            center = g.center,
            radius = g.sphereRadius * 1.22f,
        ),
        center = g.center,
        radius = g.sphereRadius * 1.22f,
    )
}

/**
 * Two faint dotted rings outside the sphere, the widest geometry on screen.
 * Dotted, so they read as a technical overlay rather than as a container.
 */
private fun DrawScope.drawDottedRings(g: OrbitGeometry) {
    val dotted = PathEffect.dashPathEffect(floatArrayOf(2f, 9f), 0f)
    for (factor in floatArrayOf(0.88f, 1f)) {
        drawCircle(
            color = AuroraColors.Translucency.BorderWeak,
            radius = g.radius * factor,
            center = g.center,
            style = Stroke(width = 1.dp.toPx(), pathEffect = dotted),
        )
    }
}

/**
 * Three tilted orbital ellipses at different inclinations, each carrying its
 * own node. The foreshortening (minor axis far below the major) is what makes
 * a ring read as encircling a sphere instead of as a halo behind one.
 */
private fun DrawScope.drawOrbitEllipses(g: OrbitGeometry, angle: Float) {
    drawTiltedOrbit(g, semiMajor = 0.98f, ratio = 0.30f, tilt = -24f, angle = angle,
        color = AuroraColors.BlueSecondary, alpha = 0.34f)
    drawTiltedOrbit(g, semiMajor = 0.90f, ratio = 0.42f, tilt = 18f, angle = angle * 0.72f + 130f,
        color = AuroraColors.AccentMint, alpha = 0.30f)
    drawTiltedOrbit(g, semiMajor = 1.06f, ratio = 0.22f, tilt = 62f, angle = angle * 1.3f + 40f,
        color = AuroraColors.Blue, alpha = 0.22f)
}

private fun DrawScope.drawTiltedOrbit(
    g: OrbitGeometry,
    semiMajor: Float,
    ratio: Float,
    tilt: Float,
    angle: Float,
    color: Color,
    alpha: Float,
) {
    val a = g.radius * semiMajor
    val b = a * ratio
    rotate(degrees = tilt, pivot = g.center) {
        // The ring itself.
        drawOval(
            color = color.copy(alpha = alpha),
            topLeft = Offset(g.center.x - a, g.center.y - b),
            size = androidx.compose.ui.geometry.Size(a * 2, b * 2),
            style = Stroke(width = 1.1.dp.toPx()),
        )
        // A travelling glow on the ring: the leading node's arc, brighter than
        // the rest of the track, so motion is visible even at slow speed.
        val head = (angle % 360f + 360f) % 360f
        drawArc(
            color = color.copy(alpha = alpha * 2.1f),
            startAngle = head - 28f,
            sweepAngle = 28f,
            useCenter = false,
            topLeft = Offset(g.center.x - a, g.center.y - b),
            size = androidx.compose.ui.geometry.Size(a * 2, b * 2),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * The vertical and horizontal crosshair, fading from the core outward.
 */
private fun DrawScope.drawCrosshair(g: OrbitGeometry) {
    val reach = g.radius * 0.94f
    for (angle in floatArrayOf(0f, 90f, 180f, 270f)) {
        val radians = angle * PI.toFloat() / 180f
        val end = Offset(
            x = g.center.x + cos(radians) * reach,
            y = g.center.y + sin(radians) * reach,
        )
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    AuroraColors.BrightMint.copy(alpha = 0.38f),
                    Color.Transparent,
                ),
                start = g.center,
                end = end,
            ),
            start = g.center,
            end = end,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/**
 * The glass sphere. Two overlapping radial gradients, offset from one another:
 * a cool blue body shaded toward the lower right, and a cyan wash where the
 * light actually is. Overlapped, the two read as one lit, curved surface.
 */
private fun DrawScope.drawGlassSphere(g: OrbitGeometry) {
    // The body: dark glass, shaded away from the light source.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF102338),
                Color(0xFF0B1420),
                Color(0xFF070C13),
            ),
            center = Offset(
                g.center.x + g.sphereRadius * 0.22f,
                g.center.y + g.sphereRadius * 0.28f,
            ),
            radius = g.sphereRadius * 1.15f,
        ),
        center = g.center,
        radius = g.sphereRadius,
    )
    // The cyan illumination, on the lit side.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AuroraColors.AccentMint.copy(alpha = 0.30f),
                AuroraColors.Blue.copy(alpha = 0.10f),
                Color.Transparent,
            ),
            center = g.lightSource,
            radius = g.sphereRadius * 0.95f,
        ),
        center = g.center,
        radius = g.sphereRadius,
    )
    // The dark rim that makes it glass rather than a flat disc.
    drawCircle(
        color = AuroraColors.Translucency.BorderStrong,
        center = g.center,
        radius = g.sphereRadius,
        style = Stroke(width = 1.dp.toPx()),
    )
}

/**
 * Latitude and longitude across the surface. Latitude lines flatten toward the
 * poles; longitude lines narrow toward the edges. Both are ellipses, which is
 * what the two families look like projected onto a sphere.
 */
private fun DrawScope.drawSphereGrid(g: OrbitGeometry) {
    val r = g.sphereRadius
    // Latitude: the parallels, flattening toward the poles.
    for (i in 1..4) {
        val factor = i / 5f
        drawOval(
            color = AuroraColors.Blue.copy(alpha = 0.16f),
            topLeft = Offset(g.center.x - r, g.center.y - r * factor),
            size = androidx.compose.ui.geometry.Size(r * 2, r * factor * 2),
            style = Stroke(width = 0.8.dp.toPx()),
        )
    }
    // Longitude: the meridians, narrowing toward the edges.
    for (i in 1..4) {
        val factor = i / 5f
        drawOval(
            color = AuroraColors.AccentMint.copy(alpha = 0.12f),
            topLeft = Offset(g.center.x - r * factor, g.center.y - r),
            size = androidx.compose.ui.geometry.Size(r * factor * 2, r * 2),
            style = Stroke(width = 0.8.dp.toPx()),
        )
    }
}

/**
 * The bright core and its specular highlight, breathing with [pulse].
 */
private fun DrawScope.drawSpecularCore(g: OrbitGeometry, pulse: Float, secure: Boolean) {
    val coreRadius = g.sphereRadius * (0.15f + 0.035f * pulse)
    val coreColor = if (secure) AuroraColors.BrightMint else AuroraColors.BlueSecondary

    // The wide, soft bloom.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = 0.75f),
                coreColor.copy(alpha = 0.18f),
                Color.Transparent,
            ),
            center = g.center,
            radius = coreRadius * 2.6f,
        ),
        center = g.center,
        radius = coreRadius * 2.6f,
    )
    // The hot centre itself.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                coreColor.copy(alpha = 0.85f),
            ),
            center = Offset(g.center.x - coreRadius * 0.2f, g.center.y - coreRadius * 0.25f),
            radius = coreRadius,
        ),
        center = g.center,
        radius = coreRadius,
    )
    // The specular pinprick, offset toward the light.
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        center = Offset(g.center.x - coreRadius * 0.22f, g.center.y - coreRadius * 0.28f),
        radius = coreRadius * 0.42f,
    )
}

/**
 * Nodes riding the orbital ellipses, drawn last so they sit over the sphere.
 * Each is placed on its own tilted ellipse; those at the back of the ring are
 * dimmed and shrunk, which is the depth cue.
 */
private fun DrawScope.drawOrbitalNodes(g: OrbitGeometry, angle: Float, secure: Boolean) {
    nodeOnOrbit(g, semiMajor = 0.98f, ratio = 0.30f, tilt = -24f, angle = angle,
        color = AuroraColors.BrightMint, secure = secure)
    nodeOnOrbit(g, semiMajor = 0.90f, ratio = 0.42f, tilt = 18f, angle = angle * 0.72f + 130f + 60f,
        color = AuroraColors.BlueSecondary, secure = secure)
    nodeOnOrbit(g, semiMajor = 1.06f, ratio = 0.22f, tilt = 62f, angle = angle * 1.3f + 40f + 200f,
        color = AuroraColors.AccentMint, secure = secure)
}

private fun DrawScope.nodeOnOrbit(
    g: OrbitGeometry,
    semiMajor: Float,
    ratio: Float,
    tilt: Float,
    angle: Float,
    color: Color,
    secure: Boolean,
) {
    val a = g.radius * semiMajor
    val b = a * ratio
    val radians = angle * PI.toFloat() / 180f
    val local = Offset(a * cos(radians), b * sin(radians))
    // Rotate the ellipse position into its tilted plane.
    val t = tilt * PI.toFloat() / 180f
    val point = Offset(
        x = g.center.x + local.x * cos(t) - local.y * sin(t),
        y = g.center.y + local.x * sin(t) + local.y * cos(t),
    )
    val depth = (sin(radians) + 1f) / 2f // 0 at the back, 1 at the front.
    val nodeRadius = (1.4f + 1.8f * depth).dp.toPx()
    val alpha = (0.30f + 0.70f * depth) * (if (secure) 1f else 0.7f)

    drawNode(point, color, alpha, nodeRadius)
}

private fun DrawScope.drawNode(point: Offset, color: Color, alpha: Float, radius: Float) {
    // The halo first, so the bright centre draws over it.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f * alpha), Color.Transparent),
            center = point,
            radius = radius * 3.6f,
        ),
        center = point,
        radius = radius * 3.6f,
    )
    drawCircle(
        color = color.copy(alpha = alpha),
        center = point,
        radius = radius,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f * alpha),
        center = point,
        radius = radius * 0.45f,
    )
}

/**
 * A smooth closed path through the node positions, held as a reusable shape by
 * callers that need to glow the whole orbit at once. Not used by the globe
 * itself, which draws rings as ellipses; exported for the waveform card and any
 * other surface that wants the orbit silhouette.
 */
internal fun orbitPath(center: Offset, radius: Float, tilt: Float): Path = Path().apply {
    val a = radius
    val b = radius * 0.42f
    val t = tilt * PI.toFloat() / 180f
    for (i in 0..360 step 6) {
        val r = i * PI.toFloat() / 180f
        val lx = a * cos(r)
        val ly = b * sin(r)
        val x = center.x + lx * cos(t) - ly * sin(t)
        val y = center.y + lx * sin(t) + ly * cos(t)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
