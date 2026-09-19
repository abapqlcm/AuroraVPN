package com.auroravpn.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraShapes
import com.auroravpn.app.ui.design.AuroraTypography

/**
 * The four primary destinations, as a dark glass bar with a top divider.
 *
 * Icons are drawn marks rather than a Material Icons dependency whose version
 * does not track Compose's; the active tab is the one that is mint, and the
 * indicator under it is the only thing that moves.
 */
@Composable
fun AuroraBottomBar(
    destinations: List<AuroraTab>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AuroraColors.Translucency.GlassStrong),
    ) {
        // The hairline that separates the bar from the content above.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AuroraColors.Translucency.BorderWeak),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                AuroraTabButton(
                    destination = destination,
                    selected = currentRoute == destination.route,
                    onSelect = { onSelect(destination.route) },
                )
            }
        }
    }
}

@Composable
private fun AuroraTabButton(
    destination: AuroraTab,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val tint = if (selected) AuroraColors.AccentMint else AuroraColors.TextMuted
    Column(
        modifier = Modifier
            .clip(AuroraShapes.Pill)
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = "Navigate to ${destination.label}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // The indicator, present only on the active tab.
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 2.dp)
                .clip(CircleShape)
                .background(if (selected) AuroraColors.AccentMint else androidx.compose.ui.graphics.Color.Transparent),
        )
        TabIcon(kind = destination.iconKind, tint = tint)
        Text(
            text = destination.label,
            style = AuroraTypography.Navigation,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A small drawn mark per tab. Kept as an enum so a tab cannot ask for an icon
 * that has no implementation.
 */
enum class TabIconKind { Home, Routes, Activity, Settings }

@Composable
private fun TabIcon(kind: TabIconKind, tint: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(20.dp),
    ) {
        val w = size.width
        val h = size.height
        when (kind) {
            TabIconKind.Home -> {
                // A ring with a centre dot: the orbit, in miniature.
                drawCircle(color = tint, radius = w * 0.32f, center = androidx.compose.ui.geometry.Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f))
                drawCircle(color = tint, radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w / 2, h / 2))
            }
            TabIconKind.Routes -> {
                // Two linked nodes.
                val a = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.7f)
                val b = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.3f)
                drawLine(color = tint, start = a, end = b, strokeWidth = 1.6f)
                drawCircle(color = tint, radius = w * 0.10f, center = a)
                drawCircle(color = tint, radius = w * 0.10f, center = b)
            }
            TabIconKind.Activity -> {
                // A pulse line.
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.6f)
                    lineTo(w * 0.3f, h * 0.6f)
                    lineTo(w * 0.42f, h * 0.25f)
                    lineTo(w * 0.56f, h * 0.85f)
                    lineTo(w * 0.7f, h * 0.5f)
                    lineTo(w, h * 0.5f)
                }
                drawPath(path = path, color = tint, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f))
            }
            TabIconKind.Settings -> {
                // A gear simplified to a ring and spokes.
                val center = androidx.compose.ui.geometry.Offset(w / 2, h / 2)
                drawCircle(color = tint, radius = w * 0.26f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f))
                for (angle in 0 until 360 step 60) {
                    val radians = Math.toRadians(angle.toDouble())
                    val from = androidx.compose.ui.geometry.Offset(
                        x = center.x + (Math.cos(radians) * w * 0.26f).toFloat(),
                        y = center.y + (Math.sin(radians) * w * 0.26f).toFloat(),
                    )
                    val to = androidx.compose.ui.geometry.Offset(
                        x = center.x + (Math.cos(radians) * w * 0.4f).toFloat(),
                        y = center.y + (Math.sin(radians) * w * 0.4f).toFloat(),
                    )
                    drawLine(color = tint, start = from, end = to, strokeWidth = 1.6f)
                }
            }
        }
    }
}
