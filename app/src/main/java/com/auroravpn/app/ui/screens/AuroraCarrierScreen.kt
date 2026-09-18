package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.data.Carrier
import com.whitedns.whiteaesther.data.TorBridge

/**
 * Carriers, Tor bridges and the mihomo chain — the ways out that are not the engine's
 * own tunnel.
 *
 * Every control writes to the same [AppSettings] the service reads, and every node and
 * delay shown is what [com.whitedns.whiteaesther.MainViewModel.chainState] reported.
 */
@Composable
fun AuroraCarrierScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val chain by viewModel.chainState.collectAsStateWithLifecycle()
    val regions by viewModel.psiphonRegions.collectAsStateWithLifecycle()
    val bridgesFetching by viewModel.bridgesFetching.collectAsStateWithLifecycle()
    val bridgesMessage by viewModel.bridgesMessage.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Carrier & Chain", style = MaterialTheme.typography.headlineSmall)

        ChoiceCard(
            title = "Carrier",
            subtitle = "What carries the tunnel. Anything other than Aether arrives as a " +
                "local SOCKS5 port that mihomo routes the interface through.",
            entries = Carrier.entries,
            selected = settings.carrier,
            labelOf = { chosen -> stringResource(chosen.label) },
            onSelect = { chosen -> viewModel.save(settings.copy(carrier = chosen)) },
        )

        if (settings.carrier == Carrier.PSIPHON) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Psiphon region", style = MaterialTheme.typography.titleMedium)
                    if (regions.isEmpty()) {
                        Text(
                            text = "No regions have been listed yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                        ) {
                            items(regions) { region ->
                                AuroraSelectableRow(
                                    title = region,
                                    selected = settings.psiphonRegion == region,
                                    onSelect = { viewModel.save(settings.copy(psiphonRegion = region)) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (settings.carrier == Carrier.TOR) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Tor bridges", style = MaterialTheme.typography.titleMedium)
                    TorBridge.entries.forEach { bridge ->
                        AuroraSelectableRow(
                            title = stringResource(bridge.label),
                            selected = settings.torBridge == bridge,
                            onSelect = { viewModel.save(settings.copy(torBridge = bridge)) },
                        )
                    }
                    if (settings.torBridge == TorBridge.CUSTOM) {
                        OutlinedTextField(
                            value = settings.torBridges,
                            onValueChange = { viewModel.save(settings.copy(torBridges = it)) },
                            label = { Text("Bridge lines") },
                            supportingText = { Text("One per line.") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { viewModel.fetchBridges(settings, viewModel.detectedCountry()) },
                            enabled = !bridgesFetching,
                        ) { Text("Fetch bridges") }
                        if (bridgesFetching) {
                            Text(
                                text = "Fetching…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        bridgesMessage?.let { message ->
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (message.isError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Second carrier", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Chain two carriers, so the second dials through the first. " +
                        "Leave it off for a single hop.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AuroraSelectableRow(
                    title = "None",
                    selected = settings.secondCarrier == null,
                    onSelect = { viewModel.save(settings.copy(secondCarrier = null)) },
                )
                Carrier.entries.forEach { carrier ->
                    AuroraSelectableRow(
                        title = stringResource(carrier.label),
                        selected = settings.secondCarrier == carrier,
                        onSelect = { viewModel.save(settings.copy(secondCarrier = carrier)) },
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Chain", style = MaterialTheme.typography.titleMedium)
                if (!chain.available) {
                    Text(
                        text = "This build does not have the chain library, so the " +
                            "carriers that need it cannot run.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    SwitchRow(
                        title = "Use the chain",
                        subtitle = "Route the interface through a mihomo node.",
                        checked = settings.chain.enabled,
                        onCheckedChange = { enabled ->
                            viewModel.save(settings.copy(chain = settings.chain.copy(enabled = enabled)))
                        },
                    )
                    if (settings.chain.enabled) {
                        SwitchRow(
                            title = "Dial through the tunnel",
                            subtitle = "Hide the node's address and SNI from the local " +
                                "network. Turn off if the tunnel itself cannot connect.",
                            checked = settings.chain.throughTunnel,
                            onCheckedChange = { through ->
                                viewModel.save(
                                    settings.copy(chain = settings.chain.copy(throughTunnel = through)),
                                )
                            },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.refreshChainNodes(settings) },
                                enabled = !chain.busy,
                            ) { Text("Refresh nodes") }
                            OutlinedButton(
                                onClick = { viewModel.testChainNodes() },
                                enabled = !chain.busy && chain.nodes.isNotEmpty(),
                                modifier = Modifier.semantics { contentDescription = "Test chain nodes" },
                            ) { Text("Test all") }
                            OutlinedButton(
                                onClick = { viewModel.cancelChainTests() },
                                enabled = chain.busy,
                            ) { Text("Cancel") }
                        }
                        chain.testProgress?.let { (done, total) ->
                            Text(
                                text = "Testing $done of $total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        chain.error?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        if (chain.nodes.isEmpty()) {
                            Text(
                                text = "No nodes. Add a subscription or paste node URIs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp),
                            ) {
                                items(chain.nodes, key = { it.name }) { node ->
                                    ChainNodeRow(
                                        name = node.name,
                                        kind = node.kind,
                                        delay = node.delay,
                                        supported = node.supported,
                                        isSelected = chain.selected == node.name,
                                        onSelect = { viewModel.selectChainNode(settings, node.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainNodeRow(
    name: String,
    kind: String,
    delay: Int?,
    supported: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Chain node $name" },
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.padding(end = 16.dp)) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = kind,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!supported) {
                    Text(
                        text = "This engine cannot authenticate with this node",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (isSelected) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = when {
                    delay == null -> "failed"
                    delay <= 0 -> "—"
                    else -> "${delay}ms"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
