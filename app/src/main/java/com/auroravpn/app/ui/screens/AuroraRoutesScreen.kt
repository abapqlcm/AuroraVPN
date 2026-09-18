package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.data.EngineMode
import com.whitedns.whiteaesther.data.SplitTunnelMode

/**
 * Routing rules and which apps the interface takes.
 *
 * Every control writes straight through to [AuroraViewModel.save], so a change is
 * persisted the moment it is made. The engine reads the new rules on its next connect;
 * the screen never restarts the tunnel itself.
 */
@Composable
fun AuroraRoutesScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val status by viewModel.engineStatus.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Routes", style = MaterialTheme.typography.headlineSmall)

        if (status.stage == com.whitedns.whiteaesther.service.EngineStage.CONNECTED) {
            Text(
                text = "Routing changes apply on the next connect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RuleCard(
            title = "Block these",
            hint = "One per line. Domains and IPs the tunnel drops.",
            value = settings.routeBlock,
            onValueChange = { viewModel.save(settings.copy(routeBlock = it)) },
        )
        RuleCard(
            title = "Let these through",
            hint = "One per line. Domains and IPs that bypass the tunnel.",
            value = settings.routeDirect,
            onValueChange = { viewModel.save(settings.copy(routeDirect = it)) },
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Split tunnelling", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Which apps the interface takes. Changes apply on the next connect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SplitTunnelMode.entries.forEach { mode ->
                    val selected = settings.splitTunnel.mode == mode
                    AuroraSelectableRow(
                        title = stringResource(mode.label),
                        selected = selected,
                        onSelect = {
                            viewModel.save(settings.copy(splitTunnel = settings.splitTunnel.copy(mode = mode)))
                        },
                    )
                }
                if (settings.splitTunnel.mode != SplitTunnelMode.ALL) {
                    Text(
                        text = "${settings.splitTunnel.packages.size} apps selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "App selection is on the WAM app-list screen and is " +
                            "preserved unchanged. This build does not remove it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Engine mode", style = MaterialTheme.typography.titleMedium)
                EngineMode.entries.forEach { mode ->
                    AuroraSelectableRow(
                        title = mode.wireName.uppercase(),
                        selected = settings.mode == mode,
                        onSelect = { viewModel.save(settings.copy(mode = mode)) },
                    )
                }
                if (settings.mode == EngineMode.PROXY) {
                    Text(
                        text = "Proxy port ${settings.proxyPort}" +
                            if (settings.lanSharing) " — shared on the LAN" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SwitchCard(
            title = "Route sniffing",
            subtitle = "Learn which destinations the tunnel should take.",
            checked = settings.routeSniff,
            onCheckedChange = { viewModel.save(settings.copy(routeSniff = it)) },
        )
    }
}

@Composable
fun RuleCard(
    title: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(title) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


