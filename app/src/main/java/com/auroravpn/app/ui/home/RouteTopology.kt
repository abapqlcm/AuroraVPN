package com.auroravpn.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraTypography

/**
 * Device → Transport → Endpoint → Internet, as one horizontal line.
 *
 * The reference's topology is a row, not a column: four nodes, each in its own
 * glass container, joined by a line that carries the glow of an established
 * session. Nothing here invents a hop — the endpoint hop is only drawn when the
 * engine has actually published one, and the transport label comes from the
 * setting the user picked.
 */
@Composable
fun RouteTopology(
    transport: String,
    endpoint: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    // The endpoint hop is only part of the row when one is known. A topology
    // that names a city nobody dialled is worse than one that admits it has
    // not chosen yet.
    val hops = buildList {
        add(TopologyNode.Device)
        add(TopologyNode.Transport(transport.ifBlank { "AUTO" }.uppercase()))
        if (endpoint.isNotBlank()) add(TopologyNode.Endpoint(endpoint))
        add(TopologyNode.Internet)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        hops.forEachIndexed { index, hop ->
            TopologyItem(
                hop = hop,
                active = connected && index in 1 until hops.size - 1,
                modifier = Modifier.weight(1f),
            )
            if (index != hops.lastIndex) {
                TopologyLink(
                    active = connected,
                    modifier = Modifier.weight(0.55f),
                )
            }
        }
    }
}

/** The kinds of node a route can hold. */
private sealed interface TopologyNode {
    val label: String

    data object Device : TopologyNode {
        override val label = "Device"
    }

    data class Transport(override val label: String) : TopologyNode
    data class Endpoint(override val label: String) : TopologyNode
    data object Internet : TopologyNode {
        override val label = "Internet"
    }
}

@Composable
private fun TopologyItem(
    hop: TopologyNode,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // The circular glass container the icon sits in.
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(nodeContainer(active)),
            contentAlignment = Alignment.Center,
        ) {
            NodeMark(hop = hop, active = active)
        }
        Text(
            text = hop.label,
            style = AuroraTypography.MetricLabel,
            color = if (active) AuroraColors.TextPrimary else AuroraColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun nodeContainer(active: Boolean): Brush = if (active) {
    Brush.radialGradient(
        colors = listOf(
            AuroraColors.AccentMint.copy(alpha = 0.30f),
            AuroraColors.GlassSurface,
        ),
    )
} else {
    Brush.radialGradient(
        colors = listOf(
            AuroraColors.GlassSurface,
            AuroraColors.BackgroundSecondary,
        ),
    )
}

/**
 * A drawn mark per node: a device as a rounded square, transport as a pulse,
 * the endpoint as a location pin, the internet as a globe.
 */
@Composable
private fun NodeMark(hop: TopologyNode, active: Boolean) {
    val tint = if (active) AuroraColors.BrightMint else AuroraColors.TextMuted
    androidx.compose.foundation.Canvas(modifier = Modifier.size(17.dp)) {
        val w = size.width
        val h = size.height
        when (hop) {
            TopologyNode.Device -> {
                // A rounded rectangle: the handset.
                drawRoundRect(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.16f),
                    size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.68f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.10f, w * 0.10f),
                    style = Stroke(width = 1.5f),
                )
            }
            is TopologyNode.Transport -> {
                // A pulse line: the tunnel.
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.55f)
                    lineTo(w * 0.30f, h * 0.55f)
                    lineTo(w * 0.42f, h * 0.22f)
                    lineTo(w * 0.58f, h * 0.86f)
                    lineTo(w * 0.70f, h * 0.50f)
                    lineTo(w, h * 0.50f)
                }
                drawPath(path = path, color = tint, style = Stroke(width = 1.5f))
            }
            is TopologyNode.Endpoint -> {
                // A location pin.
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, h * 0.86f)
                    cubicTo(
                        w * 0.14f, h * 0.48f,
                        w * 0.26f, h * 0.14f,
                        w * 0.5f, h * 0.14f,
                    )
                    cubicTo(
                        w * 0.74f, h * 0.14f,
                        w * 0.86f, h * 0.48f,
                        w * 0.5f, h * 0.86f,
                    )
                    close()
                }
                drawPath(path = path, color = tint, style = Stroke(width = 1.5f))
                drawCircle(color = tint, radius = w * 0.075f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.36f))
            }
            TopologyNode.Internet -> {
                // A globe: a circle with a meridian and an equator.
                drawCircle(
                    color = tint,
                    radius = w * 0.34f,
                    center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f),
                    style = Stroke(width = 1.5f),
                )
                drawOval(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.16f),
                    size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.68f),
                    style = Stroke(width = 1.5f),
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.84f, h * 0.5f),
                    strokeWidth = 1.5f,
                )
            }
        }
    }
}

/**
 * The line between two nodes. When a session is up, the line carries a moving
 * glow; when it is not, it is a static hairline that reads as "not yet".
 */
@Composable
private fun TopologyLink(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(linkBrush(active)),
    )
}

private fun linkBrush(active: Boolean): Brush = if (active) {
    Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            AuroraColors.AccentMint.copy(alpha = 0.75f),
            AuroraColors.BrightMint,
            AuroraColors.AccentMint.copy(alpha = 0.75f),
            Color.Transparent,
        ),
    )
} else {
    Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            AuroraColors.GlassBorder,
            Color.Transparent,
        ),
    )
}
