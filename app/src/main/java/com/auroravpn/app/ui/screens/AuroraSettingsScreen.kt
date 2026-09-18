package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel

/**
 * Every setting the engine reads.
 *
 * A control here is only here because the field exists in
 * [com.whitedns.whiteaesther.data.AppSettings] and the engine consumes it, and each one
 * writes through [AuroraViewModel.save] to the same DataStore WAM's own screens use.
 */
@Composable
fun AuroraSettingsScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val update by viewModel.update.collectAsStateWithLifecycle()
    val version by viewModel.engineVersion.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)

        update?.let { available ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "An update is available", style = MaterialTheme.typography.titleMedium)
                    Text(text = available.toString(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        SwitchCard(
            title = "Show advanced settings",
            subtitle = "The fields below this are the ones the engine reads directly.",
            checked = settings.showAdvanced,
            onCheckedChange = { viewModel.save(settings.copy(showAdvanced = it)) },
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "DNS", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = settings.dnsServers,
                    onValueChange = { viewModel.save(settings.copy(dnsServers = it)) },
                    label = { Text("Custom DNS servers") },
                    supportingText = { Text("Comma separated. Blank uses the engine's resolvers.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SwitchRow(
                    title = "IPv6 / dual stack",
                    subtitle = "Carry both address families. Turn off on networks that " +
                        "assign a broken IPv6 range.",
                    checked = settings.dualStack,
                    onCheckedChange = { viewModel.save(settings.copy(dualStack = it)) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Kill switch", style = MaterialTheme.typography.titleMedium)
                SwitchRow(
                    title = "Block on failure",
                    subtitle = "Drop traffic the tunnel was supposed to take, so a " +
                        "failure cannot leak it.",
                    checked = settings.killSwitch,
                    onCheckedChange = { viewModel.save(settings.copy(killSwitch = it)) },
                )
                SwitchRow(
                    title = "Strict",
                    subtitle = "Keep blocking between sessions too. Only lift by hand.",
                    checked = settings.strictKillSwitch,
                    onCheckedChange = { viewModel.save(settings.copy(strictKillSwitch = it)) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Proxy", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = settings.upstreamProxy,
                    onValueChange = { viewModel.save(settings.copy(upstreamProxy = it)) },
                    label = { Text("Upstream proxy") },
                    supportingText = { Text("socks5://host:port or http://host:port. Blank is none.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = settings.proxyPort.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { viewModel.save(settings.copy(proxyPort = it)) }
                    },
                    label = { Text("Local proxy port") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SwitchRow(
                    title = "Share the proxy on the LAN",
                    subtitle = "Let other devices on this network use the tunnel. " +
                        "Give it a username and password first.",
                    checked = settings.lanSharing,
                    onCheckedChange = { viewModel.save(settings.copy(lanSharing = it)) },
                )
                if (settings.lanSharing) {
                    OutlinedTextField(
                        value = settings.lanUsername,
                        onValueChange = { viewModel.save(settings.copy(lanUsername = it)) },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.lanPassword,
                        onValueChange = { viewModel.save(settings.copy(lanPassword = it)) },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Behaviour", style = MaterialTheme.typography.titleMedium)
                SwitchRow(
                    title = "Automatic carrier selection",
                    subtitle = "Let Automatic race the carriers this network might allow, " +
                        "rather than using only the one chosen.",
                    checked = settings.automaticCarrier,
                    onCheckedChange = { viewModel.save(settings.copy(automaticCarrier = it)) },
                )
                SwitchCard(
                    title = "Clean-endpoint validation",
                    subtitle = "Probe a candidate endpoint through the data plane before " +
                        "using it, so a censored result cannot be mistaken for a working one.",
                    checked = settings.validationEnabled,
                    onCheckedChange = { viewModel.save(settings.copy(validationEnabled = it)) },
                )
            }
        }

        if (settings.showAdvanced) {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "TLS", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = settings.tlsGroups,
                        onValueChange = { viewModel.save(settings.copy(tlsGroups = it)) },
                        label = { Text("TLS groups") },
                        supportingText = { Text("Restrict the curves the handshake offers.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.wgKeepalive.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { viewModel.save(settings.copy(wgKeepalive = it)) }
                        },
                        label = { Text("WireGuard keepalive (s)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.engineLogLevel,
                        onValueChange = { viewModel.save(settings.copy(engineLogLevel = it)) },
                        label = { Text("Engine log level") },
                        supportingText = { Text("Blank is the build default.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Text(
            text = "Engine ${version ?: "not loaded"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
