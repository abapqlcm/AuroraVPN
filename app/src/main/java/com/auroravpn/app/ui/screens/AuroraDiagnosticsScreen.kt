package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.AddressPair
import com.whitedns.whiteaesther.service.EngineStage

/**
 * Live diagnostics: what the engine, the carrier and the network are doing right now.
 *
 * Everything is a reading the backend published. Where a value is not known yet, the
 * screen says so rather than substituting a plausible one.
 */
@Composable
fun AuroraDiagnosticsScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val status by viewModel.engineStatus.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val version by viewModel.engineVersion.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Diagnostics", style = MaterialTheme.typography.headlineSmall)

        DiagnosticsCard(title = "Engine") {
            DiagnosticsRow("Stage", status.stage.name)
            DiagnosticsRow("Mode", status.mode?.wireName ?: "not chosen")
            DiagnosticsRow("Message", status.message.ifBlank { "—" })
            DiagnosticsRow("Endpoint", status.peer ?: "—")
            DiagnosticsRow("Version", version ?: "native core not loaded")
        }

        DiagnosticsCard(title = "Carrier path") {
            if (status.path.isEmpty()) {
                DiagnosticsRow("Path", "single carrier, no hops to report")
            } else {
                status.path.forEach { hop ->
                    DiagnosticsRow(hop.carrier.wireName, hop.stage.name)
                }
            }
            if (status.attempts.isNotEmpty()) {
                Text(
                    text = "Automatic has tried",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                status.attempts.forEach { attempt ->
                    DiagnosticsRow(attempt.carrier.wireName, attempt.stage.name)
                }
            }
            status.searchStartedAtMillis?.let { started ->
                val seconds = (System.currentTimeMillis() - started) / 1000
                DiagnosticsRow("Searching for", "${seconds}s")
            }
        }

        DiagnosticsCard(title = "Network") {
            DiagnosticsRow("Carrier", settings.carrier.wireName)
            DiagnosticsRow("Second carrier", settings.secondCarrier?.wireName ?: "none")
            DiagnosticsRow("Transport", settings.transport.wireName)
            DiagnosticsRow("Dual stack", settings.dualStack.toString())
            DiagnosticsRow("Custom DNS", settings.dnsServers.ifBlank { "engine default" })
            DiagnosticsRow("Kill switch", killSwitchLabel(settings))
        }

        DiagnosticsCard(title = "Addresses") {
            DiagnosticsRow("Through the tunnel", addresses.tunnel ?: "not connected")
            DiagnosticsRow("Without a tunnel", addresses.real ?: "not measured")
        }

        if (status.stage == EngineStage.ERROR) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Last error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = status.message.ifBlank { "The engine reported an error." },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun DiagnosticsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Same discipline as the session rows: the label keeps its natural
        // width, the value takes what is left and is not squeezed into a
        // vertical strip. Diagnostics values are the widest strings in the app
        // -- messages, addresses, hop names -- so this is where they wrapped.
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 16.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun killSwitchLabel(settings: com.whitedns.whiteaesther.data.AppSettings): String = when {
    settings.strictKillSwitch -> "strict (blocks between sessions)"
    settings.killSwitch -> "on (blocks on failure)"
    else -> "off"
}
