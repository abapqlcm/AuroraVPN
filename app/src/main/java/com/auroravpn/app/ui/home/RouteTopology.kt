package com.auroravpn.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraTypography
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow

/**
 * Device → transport → endpoint → internet, drawn from the state the engine
 * publishes rather than from the reference picture.
 *
 * The endpoint hop is only drawn when one is known: a topology that names a
 * city nobody dialled is worse than one that admits it is still choosing.
 */
@Composable
fun RouteTopology(
    transport: String,
    endpoint: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    val hops = buildList {
        add("Device")
        add(transport.ifBlank { "—" }.uppercase())
        if (endpoint.isNotBlank()) add(endpoint)
        add("Internet")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        hops.forEachIndexed { index, hop ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Node(
                    active = connected && index in 1 until hops.size - 1,
                    first = index == 0,
                    last = index == hops.lastIndex,
                )
                Text(
                    text = hop,
                    style = if (index == 0 || index == hops.lastIndex) {
                        AuroraTypography.MetricLabel
                    } else {
                        AuroraTypography.Endpoint
                    },
                    color = if (index == 0 || index == hops.lastIndex) {
                        AuroraColors.TextMuted
                    } else {
                        AuroraColors.TextPrimary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (index != hops.lastIndex) {
                Link(
                    modifier = Modifier.padding(start = 11.dp),
                    active = connected && index in 1 until hops.size - 1,
                )
            }
        }
    }
}

@Composable
private fun Node(active: Boolean, first: Boolean, last: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .nodeBackground(active, first, last),
    )
}

private fun Modifier.nodeBackground(active: Boolean, first: Boolean, last: Boolean): Modifier =
    background(
        if (active) AuroraColors.AccentMint
        else if (first || last) AuroraColors.TextMuted
        else AuroraColors.GlassBorder,
    )

@Composable
private fun Link(modifier: Modifier = Modifier, active: Boolean) {
    Box(
        modifier = modifier
            .size(width = 2.dp, height = 12.dp)
            .linkBackground(active),
    )
}

private fun Modifier.linkBackground(active: Boolean): Modifier =
    background(
        if (active) AuroraColors.AccentMint.copy(alpha = 0.45f)
        else AuroraColors.GlassBorder,
    )
