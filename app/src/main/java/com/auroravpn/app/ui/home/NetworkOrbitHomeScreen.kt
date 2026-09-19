package com.auroravpn.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraConnectionState
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraLogo
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraStatusPill
import com.auroravpn.app.ui.design.AuroraTypography
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraMetric
import com.auroravpn.app.ui.design.AuroraMetricDivider
import com.auroravpn.app.ui.AuroraTelemetry
import com.whitedns.whiteaesther.service.EngineStage

/**
 * The Home screen: the Network Orbit, and the live readings arranged under it.
 *
 * Every value here is collected from a WAM StateFlow. Nothing has a default
 * that could be mistaken for a reading, and no state is held locally: rotate
 * the phone and the same flows are re-read.
 */
@Composable
fun NetworkOrbitHomeScreen(
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onConnect: (com.whitedns.whiteaesther.data.AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val traffic by viewModel.traffic.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val sessionElapsed by viewModel.sessionClock.collectAsStateWithLifecycle()
    val activeEndpoint by viewModel.activeEndpoint.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val downHistory by telemetry.downHistory.collectAsStateWithLifecycle()

    val secure = connection is AuroraConnectionState.Connected

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AuroraDimensions.screenMarginLarge,
                    vertical = 0.dp,
                )
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + AuroraDimensions.sectionGap),
            verticalArrangement = Arrangement.spacedBy(AuroraDimensions.cardGapLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuroraTopBar(
                state = connection,
                onSettings = onSettings,
            )

            // The globe is tappable: the same real operation the button would
            // invoke, and no other.
            NetworkOrbit(
                secure = secure,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .semantics { contentDescription = if (secure) "Connected. Tap to disconnect." else "Disconnected. Tap to connect." }
                    .clickable(enabled = true) {
                        if (secure) {
                            onDisconnect()
                        } else {
                            onConnect(settings)
                        }
                    },
            )

            ConnectionStatus(
                connection = connection,
                endpoint = activeEndpoint,
                transport = settings.transport.wireName,
            )

            MetricsRow(
                connection = connection,
                traffic = traffic,
                sessionBytes = traffic.received + traffic.sent,
                sessionElapsedMillis = sessionElapsed,
            )

            RouteProfileCard(
                transport = settings.transport.wireName,
                endpoint = activeEndpoint,
                connected = secure,
            )

            LiveTrafficCard(
                history = downHistory,
                traffic = traffic,
                connected = secure,
                telemetry = telemetry,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ------------------------------------------------------------------ top bar --

@Composable
private fun AuroraTopBar(
    state: AuroraConnectionState,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuroraLogo()
            Column {
                Text(
                    text = "AURORA",
                    style = AuroraTypography.Brand,
                )
                Text(
                    text = "NETWORK ORBIT",
                    style = AuroraTypography.BrandSubtitle,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuroraStatusPill(
                label = pillLabel(state),
                dotColor = pillDot(state),
            )
            Box(
                modifier = Modifier
                    .size(AuroraDimensions.touchTarget)
                    .clip(CircleShape)
                    .clickable(onClick = onSettings)
                    .semantics { contentDescription = "Settings" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⚙",
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                )
            }
        }
    }
}

private fun pillLabel(state: AuroraConnectionState): String = when (state) {
    is AuroraConnectionState.Idle -> "VPN Ready"
    is AuroraConnectionState.Preparing -> "Preparing"
    is AuroraConnectionState.Connecting -> "Connecting"
    is AuroraConnectionState.Connected -> "Connected"
    is AuroraConnectionState.Stopping -> "Disconnecting"
    is AuroraConnectionState.Failed -> "Error"
}

private fun pillDot(state: AuroraConnectionState): androidx.compose.ui.graphics.Color = when (state) {
    is AuroraConnectionState.Connected -> AuroraColors.Secure
    is AuroraConnectionState.Idle -> AuroraColors.TextSecondary
    is AuroraConnectionState.Failed -> AuroraColors.Error
    is AuroraConnectionState.Preparing, is AuroraConnectionState.Connecting,
    is AuroraConnectionState.Stopping -> AuroraColors.Warning
}

// ------------------------------------------------------- connection status --

@Composable
private fun ConnectingText(message: String) {
    Text(
        text = "CONNECTING",
        style = AuroraTypography.StatusLarge,
        color = AuroraColors.Warning,
    )
    Text(
        text = message.ifBlank { "Establishing secure connection…" },
        style = AuroraTypography.Endpoint,
        color = AuroraColors.TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ConnectionStatus(
    connection: AuroraConnectionState,
    endpoint: String,
    transport: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (connection) {
            is AuroraConnectionState.Connected -> {
                Text(
                    text = "SECURE",
                    style = AuroraTypography.StatusLarge,
                    color = AuroraColors.Secure,
                )
                Text(
                    text = lineFor(endpoint, transport),
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            is AuroraConnectionState.Connecting -> ConnectingText(connection.message)
            is AuroraConnectionState.Preparing -> ConnectingText(connection.message)
            is AuroraConnectionState.Stopping -> {
                Text(
                    text = "DISCONNECTING",
                    style = AuroraTypography.StatusLarge,
                    color = AuroraColors.Warning,
                )
                Text(
                    text = connection.message.ifBlank { "Ending the session." },
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                )
            }
            is AuroraConnectionState.Failed -> {
                Text(
                    text = "CONNECTION ERROR",
                    style = AuroraTypography.StatusLarge,
                    color = AuroraColors.Error,
                )
                Text(
                    text = connection.message.ifBlank { "The engine reported a failure." },
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            is AuroraConnectionState.Idle -> {
                Text(
                    text = "DISCONNECTED",
                    style = AuroraTypography.StatusLarge,
                    color = AuroraColors.TextPrimary,
                )
                Text(
                    text = "No active connection",
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextMuted,
                )
            }
        }
    }
}

private fun Modifier.trafficMarkBackground(): Modifier =
    background(AuroraColors.GlassSurface)

private fun lineFor(endpoint: String, transport: String): String = when {
    endpoint.isBlank() -> transport.uppercase()
    transport.isBlank() -> endpoint
    else -> "$endpoint • ${transport.uppercase()}"
}

// ------------------------------------------------------------------ metrics --

@Composable
private fun MetricsRow(
    connection: AuroraConnectionState,
    traffic: com.whitedns.whiteaesther.service.TrafficSample,
    sessionBytes: Long,
    sessionElapsedMillis: Long,
    modifier: Modifier = Modifier,
) {
    val connected = connection is AuroraConnectionState.Connected

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuroraMetric(
            label = "Latency",
            // The engine publishes RTT only for a live peer; nothing else is
            // shown, because nothing else is measured.
            value = if (connected && traffic.supported) "—" else "—",
        )
        AuroraMetricDivider()
        AuroraMetric(
            label = "Download",
            value = if (connected && traffic.supported) {
                AuroraTelemetry.formatRate(traffic.downloadPerSecond)
            } else "—",
        )
        AuroraMetricDivider()
        AuroraMetric(
            label = "Upload",
            value = if (connected && traffic.supported) {
                AuroraTelemetry.formatRate(traffic.uploadPerSecond)
            } else "—",
        )
        AuroraMetricDivider()
        AuroraMetric(
            label = "Session",
            value = if (connected) AuroraTelemetry.formatBytes(sessionBytes) else "—",
        )
    }
}

// ---------------------------------------------------------- route profile --

@Composable
private fun RouteProfileCard(
    transport: String,
    endpoint: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    AuroraGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AuroraDimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AUTO",
                    style = AuroraTypography.Button,
                    color = AuroraColors.AccentMint,
                )
                Text(
                    text = "Route Profile",
                    style = AuroraTypography.MetricLabel,
                    color = AuroraColors.TextMuted,
                )
            }
            RouteTopology(
                transport = transport,
                endpoint = endpoint,
                connected = connected,
            )
        }
    }
}

// --------------------------------------------------------- live traffic ----

@Composable
private fun LiveTrafficCard(
    history: List<Float>,
    traffic: com.whitedns.whiteaesther.service.TrafficSample,
    connected: Boolean,
    telemetry: AuroraTelemetry,
    modifier: Modifier = Modifier,
) {
    AuroraGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AuroraDimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // The waveform mark in its own dark square.
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .trafficMarkBackground(),
                        contentAlignment = Alignment.Center,
                    ) {
                        TrafficWaveform(
                            history = history.takeLast(24),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(16.dp),
                            active = connected,
                        )
                    }
                    Text(
                        text = "Live Traffic",
                        style = AuroraTypography.CardTitle,
                    )
                }
                Text(
                    text = if (connected) "→" else "",
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextMuted,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "↓ ${if (connected && traffic.supported) telemetry.rateString(traffic.downloadPerSecond) else "—"}",
                    style = AuroraTypography.MetricValue,
                    color = AuroraColors.BrightMint,
                )
                Text(
                    text = "↑ ${if (connected && traffic.supported) telemetry.rateString(traffic.uploadPerSecond) else "—"}",
                    style = AuroraTypography.MetricValue,
                    color = AuroraColors.BlueSecondary,
                )
            }

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
}
