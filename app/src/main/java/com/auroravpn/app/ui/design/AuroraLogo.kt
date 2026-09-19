package com.auroravpn.app.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The compact circular mark, drawn rather than shipped as a bitmap.
 *
 * A ring, a dark core and a glow -- the same vocabulary the Network Orbit
 * speaks, at the size a top bar can hold.
 */
@Composable
fun AuroraLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp,
) {
    Canvas(
        modifier = modifier.size(size),
    ) {
        val radius = this.size.minDimension / 2f
        val center = Offset(radius, radius)

        // The glow behind the ring.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraColors.AccentMint.copy(alpha = 0.25f),
                    Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            center = center,
            radius = radius,
        )

        // The ring, cyan to blue around its travel.
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    AuroraColors.AccentMint,
                    AuroraColors.Blue,
                    AuroraColors.AccentMint,
                ),
                center = center,
            ),
            center = center,
            radius = radius * 0.72f,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // A dark, slightly recessed centre.
        drawCircle(
            color = AuroraColors.BackgroundSecondary,
            center = center,
            radius = radius * 0.5f,
        )

        // The bright core dot.
        drawCircle(
            color = AuroraColors.BrightMint,
            center = center,
            radius = radius * 0.2f,
        )
    }
}
