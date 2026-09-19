package com.auroravpn.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraButton
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.whitedns.whiteaesther.core.ChainNode

@Composable
fun AuroraChainScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val chain by viewModel.chainState.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Chain", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("Status")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "Available: ${if (chain.available) "Yes" else "No"}", style = AuroraTypography.MetricLabel)
                Text(text = "Selected: ${chain.selected ?: "Auto"}", style = AuroraTypography.MetricLabel)
                chain.testProgress?.let { (done, total) ->
                    Text(text = "Testing $done/$total", style = AuroraTypography.MetricLabel)
                }
                chain.error?.let { text ->
                    Text(text = text, style = AuroraTypography.BodySmall, color = AuroraColors.Error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraButton(
                        text = if (chain.busy) "Testing\u2026" else "Refresh nodes",
                        onClick = { viewModel.refreshChainNodes(settings) },
                        enabled = !chain.busy,
                    )
                    AuroraButton(
                        text = "Cancel",
                        onClick = { viewModel.cancelChainTests() },
                        enabled = chain.busy,
                    )
                }
            }
        }

        AuroraSectionHeader("Nodes (${chain.nodes.size})")
        if (chain.nodes.isEmpty()) {
            AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (chain.available) "No nodes returned." else "Chain is not running.",
                    style = AuroraTypography.Body,
                    color = AuroraColors.TextMuted,
                    modifier = Modifier.padding(AuroraDimensions.cardPadding),
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chain.nodes, key = { it.name + it.kind }) { node ->
                    NodeRow(
                        node = node,
                        selected = chain.selected == node.name,
                        onSelect = { viewModel.selectChainNode(settings, node.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeRow(node: ChainNode, selected: Boolean, onSelect: () -> Unit) {
    AuroraGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
    ) {
        Column(modifier = Modifier.padding(AuroraDimensions.cardPadding)) {
            Text(text = node.name, style = AuroraTypography.CardTitle)
            Text(text = node.kind, style = AuroraTypography.Endpoint, color = AuroraColors.TextMuted)
            node.delay?.let { delay ->
                Text(text = "${delay}ms", style = AuroraTypography.MetricLabel, color = AuroraColors.TextMuted)
            }
        }
    }
}
