package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraConnectionState
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.AddressPair
import java.util.concurrent.TimeUnit

/**
 * The home screen: connection control and the live readouts the engine publishes.
 *
 * Every number on this screen is read from a WAM StateFlow. None is stored locally, and
 * none has a default that could be mistaken for a real reading: an empty peer means the
 * engine has not chosen one, a zero byte count means the session has not carried
 * anything, and a blank duration means there is no session.
 */
@Composable
fun AuroraHomeScreen(
    viewModel: AuroraViewModel,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val traffic by viewModel.traffic.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val version by viewModel.engineVersion.collectAsStateWithLifecycle()
    val sessionElapsed by viewModel.sessionClock.collectAsStateWithLifecycle()
    val activeEndpoint by viewModel.activeEndpoint.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "AuroraVPN", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Raw functional build — Network Orbit comes later",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ConnectionCard(
            connection = connection,
            transport = settings.transport.wireName,
            activeEndpoint = activeEndpoint,
            onConnect = { onConnect(settings) },
            onDisconnect = onDisconnect,
            canToggle = viewModel.canToggleConnection,
        )

        TrafficCard(sample = traffic, connected = connection is AuroraConnectionState.Connected)

        AddressCard(
            addresses = addresses,
            connected = connection is AuroraConnectionState.Connected,
        )

        SessionCard(
            connection = connection,
            version = version,
            mode = settings.mode.wireName,
            sessionElapsedMillis = sessionElapsed,
        )
    }
}

@Composable
private fun ConnectionCard(
    connection: AuroraConnectionState,
    transport: String,
    activeEndpoint: String,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    canToggle: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Connection status" },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stageLabel(connection), style = MaterialTheme.typography.titleMedium)
            Text(
                text = connectionMessage(connection, activeEndpoint).ifBlank { "No details yet." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (connection is AuroraConnectionState.Connected) {
                Text(
                    text = "Transport: $transport",
                    style = MaterialTheme.typography.bodyMedium,
                )
                // The engine publishes the peer it dialled. When it has not
                // chosen one the field is simply absent, rather than a label
                // claiming a value exists that nothing reported.
                if (activeEndpoint.isNotBlank()) {
                    Text(
                        text = "Endpoint: $activeEndpoint",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (connection is AuroraConnectionState.Connected || connection is AuroraConnectionState.Preparing || connection is AuroraConnectionState.Connecting || connection is AuroraConnectionState.Stopping) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = !canToggle,
                        modifier = Modifier.semantics { contentDescription = "Disconnect VPN" },
                    ) { Text("Disconnect") }
                } else {
                    Button(
                        onClick = onConnect,
                        enabled = canToggle,
                        modifier = Modifier.semantics { contentDescription = "Connect VPN" },
                    ) { Text("Connect") }
                }
            }
        }
    }
}

@Composable
private fun TrafficCard(
    sample: com.whitedns.whiteaesther.service.TrafficSample,
    connected: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Traffic", style = MaterialTheme.typography.titleMedium)
            if (!connected) {
                Text(
                    text = "No session is running.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (!sample.supported) {
                Text(
                    text = "This device does not report per-app traffic counters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Received", style = MaterialTheme.typography.bodyMedium)
                    Text(formatBytes(sample.received), style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Sent", style = MaterialTheme.typography.bodyMedium)
                    Text(formatBytes(sample.sent), style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Down/s", style = MaterialTheme.typography.bodyMedium)
                    Text(formatBytes(sample.downloadPerSecond), style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Up/s", style = MaterialTheme.typography.bodyMedium)
                    Text(formatBytes(sample.uploadPerSecond), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun AddressCard(addresses: AddressPair, connected: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Addresses", style = MaterialTheme.typography.titleMedium)
            AddressRow(label = "Seen by websites", value = addresses.tunnel, note = if (!connected) "Not connected" else null)
            AddressRow(label = "Without a tunnel", value = addresses.real, note = "Read while idle")
        }
    }
}

@Composable
private fun AddressRow(label: String, value: String?, note: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value?.ifBlank { "—" } ?: note ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isNullOrBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun SessionCard(
    connection: AuroraConnectionState,
    version: String?,
    mode: String,
    sessionElapsedMillis: Long,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Session", style = MaterialTheme.typography.titleMedium)
            SessionRow(label = "Duration", value = durationOf(connection, sessionElapsedMillis))
            SessionRow(label = "Engine mode", value = mode.uppercase())
            SessionRow(label = "Engine version", value = version ?: "Native core not loaded")
        }
    }
}

@Composable
private fun SessionRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Label reserved at its natural width so a long value -- an engine
        // version string, a mode name -- cannot crowd it into wrapping.
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 16.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun stageLabel(state: AuroraConnectionState): String = when (state) {
    is AuroraConnectionState.Idle -> "Disconnected"
    is AuroraConnectionState.Preparing -> "Preparing"
    is AuroraConnectionState.Connecting -> "Connecting"
    is AuroraConnectionState.Connected -> "Connected"
    is AuroraConnectionState.Stopping -> "Stopping"
    is AuroraConnectionState.Failed -> "Connection failed"
}

private fun connectionMessage(state: AuroraConnectionState, activeEndpoint: String): String = when (state) {
    is AuroraConnectionState.Idle -> "Press Connect to start a session."
    is AuroraConnectionState.Preparing -> state.message
    is AuroraConnectionState.Connecting -> state.message
    is AuroraConnectionState.Connected ->
        // The peer is the address the engine dialled. Absent means the engine has
        // not published one for this session -- reported as such, never filled in.
        if (activeEndpoint.isBlank()) "The tunnel is up." else "The tunnel is up. Endpoint $activeEndpoint."
    is AuroraConnectionState.Stopping -> state.message.ifBlank { "Ending the session." }
    is AuroraConnectionState.Failed -> state.message.ifBlank { "The engine reported a failure." }
}

private fun durationOf(state: AuroraConnectionState, elapsedMillis: Long): String {
    // Only a Connected state carries a session, and the clock flow emits zero for
    // anything else. A session that never started reads "—" rather than a duration
    // that would imply one did.
    if (state !is AuroraConnectionState.Connected || elapsedMillis < 0) return "—"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024.0 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return if (unit == 0) "${bytes} B" else String.format(java.util.Locale.ROOT, "%.1f %s", value, units[unit])
}
