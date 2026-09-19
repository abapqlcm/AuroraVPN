package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraConnectionState
import com.auroravpn.app.ui.AuroraTelemetry
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraMetric
import com.auroravpn.app.ui.design.AuroraMetricDivider
import com.auroravpn.app.ui.design.AuroraNavRow
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.auroravpn.app.ui.home.TrafficWaveform

/**
 * Activity: what this session has done, while it is doing it.
 *
 * The waveform is the telemetry window, which holds only samples the engine
 * actually published. Disconnected, it is flat and the totals read zero,
 * because nothing has been counted.
 */
@Composable
fun AuroraActivityScreen(
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onLogs: () -> Unit,
    onDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val traffic by viewModel.traffic.collectAsStateWithLifecycle()
    val elapsed by viewModel.sessionClock.collectAsStateWithLifecycle()
    val endpoint by viewModel.activeEndpoint.collectAsStateWithLifecycle()
    val down by telemetry.downHistory.collectAsStateWithLifecycle()
    val up by telemetry.upHistory.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()

    val connected = connection is AuroraConnectionState.Connected
    val downTotal = if (connected) AuroraTelemetry.formatBytes(traffic.received) else "—"
    val upTotal = if (connected) AuroraTelemetry.formatBytes(traffic.sent) else "—"
    val total = if (connected) AuroraTelemetry.formatBytes(traffic.received + traffic.sent) else "—"

    AuroraDetailScaffold(
        title = "Activity",
        onBack = { },
        modifier = modifier,
    ) {
        AuroraSectionHeader("Session")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    AuroraMetric(label = "Duration", value = formatDuration(elapsed))
                    AuroraMetricDivider()
                    AuroraMetric(label = "Endpoint", value = endpoint.ifBlank { "—" })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    AuroraMetric(label = "Download", value = downTotal)
                    AuroraMetricDivider()
                    AuroraMetric(label = "Upload", value = upTotal)
                    AuroraMetricDivider()
                    AuroraMetric(label = "Total", value = total)
                }
            }
        }

        AuroraSectionHeader("Live Traffic")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "↓ ${if (connected) telemetry.rateString(traffic.downloadPerSecond) else "—"}",
                        style = AuroraTypography.MetricValue,
                        color = AuroraColors.BrightMint,
                    )
                    Text(
                        text = "↑ ${if (connected) telemetry.rateString(traffic.uploadPerSecond) else "—"}",
                        style = AuroraTypography.MetricValue,
                        color = AuroraColors.BlueSecondary,
                    )
                }
                TrafficWaveform(
                    history = down,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    active = connected,
                )
                TrafficWaveform(
                    history = up,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    active = connected,
                )
                if (!connected) {
                    Text(
                        text = "No session is running.",
                        style = AuroraTypography.BodySmall,
                        color = AuroraColors.TextMuted,
                    )
                } else if (!traffic.supported) {
                    Text(
                        text = "This device does not report per-app traffic counters.",
                        style = AuroraTypography.BodySmall,
                        color = AuroraColors.TextMuted,
                    )
                }
            }
        }

        AuroraSectionHeader("Exit Address")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AddressLine(
                    label = "Tunnel exit",
                    value = addresses.tunnel?.ifBlank { null } ?: "Not yet observed",
                )
                AddressLine(
                    label = "Device address",
                    value = addresses.real?.ifBlank { null } ?: "Not yet observed",
                )
            }
        }

        AuroraSectionHeader("More")
        AuroraNavRow(
            title = "Logs",
            subtitle = "Severity, timestamps and the engine's own record",
            onClick = onLogs,
        )
        AuroraNavRow(
            title = "Diagnostics",
            subtitle = "VPN state, DNS, network identity and engine status",
            onClick = onDiagnostics,
        )
    }
}

@Composable
private fun AddressLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AuroraTypography.MetricLabel,
            color = AuroraColors.TextMuted,
        )
        Text(
            text = value,
            style = AuroraTypography.Endpoint,
            color = AuroraColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Milliseconds to a duration the way a clock shows one. */
private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "—"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(java.util.Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
