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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.auroravpn.app.ui.design.AuroraStatusPill
import com.auroravpn.app.ui.design.AuroraTypography
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.AuroraTelemetry
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.service.TrafficSample

/**
 * The Home screen: the orbital globe, and the live readings arranged under it.
 *
 * The composition follows the reference: a top bar, a globe large enough to be
 * the dominant element, status directly beneath it, a four-column metric row,
 * then the two glass cards. Every value is collected from a WAM StateFlow;
 * nothing has a default that could be mistaken for a reading.
 */
@Composable
fun NetworkOrbitHomeScreen(
    viewModel: AuroraViewModel,
    telemetry: AuroraTelemetry,
    onConnect: (AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val traffic by viewModel.traffic.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val sessionElapsed by viewModel.sessionClock.collectAsStateWithLifecycle()
    val activeEndpoint by viewModel.activeEndpoint.collectAsStateWithLifecycle()
    val downHistory by telemetry.downHistory.collectAsStateWithLifecycle()
    val upHistory by telemetry.upHistory.collectAsStateWithLifecycle()

    val secure = connection is AuroraConnectionState.Connected

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuroraDimensions.screenMarginLarge)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                // The bottom bar is drawn over this content; the inset plus its
                // height is what keeps the last card from sliding under it.
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                        AuroraDimensions.bottomBarClearance,
                ),
            verticalArrangement = Arrangement.spacedBy(AuroraDimensions.homeGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuroraTopBar(state = connection, onSettings = onSettings)

            // The globe is tappable: the same real operation the button would
            // invoke, and no other.
            //
            // The clip is the fix for the square flash: a Box with an aspect
            // ratio is a rectangle, and an unclipped ripple on it paints a
            // square halo over the screen. Clipped to the circle first, the
            // indication stays inside the globe, where the tap happened.
            NetworkOrbit(
                secure = secure,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .clip(CircleShape)
                    .semantics {
                        contentDescription = if (secure) "Connected. Tap to disconnect." else "Disconnected. Tap to connect."
                    }
                    .clickable {
                        if (secure) onDisconnect() else onConnect(settings)
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
                connected = secure,
            )

            RouteProfileCard(
                transport = settings.transport.wireName,
                endpoint = activeEndpoint,
                connected = secure,
            )

            LiveTrafficCard(
                downHistory = downHistory,
                upHistory = upHistory,
                traffic = traffic,
                connected = secure,
                telemetry = telemetry,
            )

            Spacer(modifier = Modifier.height(4.dp))
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
                Text(text = "AURORA", style = AuroraTypography.Brand)
                Text(text = "NETWORK ORBIT", style = AuroraTypography.BrandSubtitle)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuroraStatusPill(label = pillLabel(state), dotColor = pillDot(state))
            Box(
                modifier = Modifier
                    .size(AuroraDimensions.touchTarget)
                    .clip(CircleShape)
                    .clickable(onClick = onSettings)
                    .semantics { contentDescription = "Settings" },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "⚙", style = AuroraTypography.Endpoint, color = AuroraColors.TextSecondary)
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

private fun pillDot(state: AuroraConnectionState): Color = when (state) {
    is AuroraConnectionState.Connected -> AuroraColors.Secure
    is AuroraConnectionState.Idle -> AuroraColors.TextSecondary
    is AuroraConnectionState.Failed -> AuroraColors.Error
    is AuroraConnectionState.Preparing, is AuroraConnectionState.Connecting,
    is AuroraConnectionState.Stopping -> AuroraColors.Warning
}

// ------------------------------------------------------- connection status --

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
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        when (connection) {
            is AuroraConnectionState.Connected -> {
                Text("SECURE", style = AuroraTypography.StatusLarge, color = AuroraColors.Secure)
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
                Text("DISCONNECTING", style = AuroraTypography.StatusLarge, color = AuroraColors.Warning)
                Text(
                    text = connection.message.ifBlank { "Ending the session." },
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                )
            }
            is AuroraConnectionState.Failed -> {
                Text("CONNECTION ERROR", style = AuroraTypography.StatusLarge, color = AuroraColors.Error)
                Text(
                    text = connection.message.ifBlank { "The engine reported a failure." },
                    style = AuroraTypography.Endpoint,
                    color = AuroraColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            is AuroraConnectionState.Idle -> {
                Text("DISCONNECTED", style = AuroraTypography.StatusLarge, color = AuroraColors.TextPrimary)
                Text("No active connection", style = AuroraTypography.Endpoint, color = AuroraColors.TextMuted)
            }
        }
    }
}

/**
 * Connecting states tell the user what is happening without naming the engine.
 * "Establishing secure tunnel…" is a truthful description of the work; the
 * internal engine name belongs on the diagnostics screen.
 */
@Composable
private fun ConnectingText(message: String) {
    Text("CONNECTING", style = AuroraTypography.StatusLarge, color = AuroraColors.Warning)
    Text(
        text = message.ifBlank { "Establishing secure tunnel…" },
        style = AuroraTypography.Endpoint,
        color = AuroraColors.TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun lineFor(endpoint: String, transport: String): String = when {
    endpoint.isBlank() -> transport.uppercase()
    transport.isBlank() -> endpoint
    else -> "$endpoint • ${transport.uppercase()}"
}

// ------------------------------------------------------------------ metrics --

@Composable
private fun MetricsRow(
    connection: AuroraConnectionState,
    traffic: TrafficSample,
    sessionBytes: Long,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The engine publishes RTT only inside scan results, not for a live
        // peer, so there is no latency to show here. Em-dash is the honest
        // "unavailable", not a placeholder that looks like a reading.
        MetricColumn(label = "Latency", value = "—", modifier = Modifier.weight(1f))
        MetricDivider()
        MetricColumn(
            label = "Download",
            modifier = Modifier.weight(1f),
            value = if (connected && traffic.supported) {
                AuroraTelemetry.formatRate(traffic.downloadPerSecond)
            } else "—",
        )
        MetricDivider()
        MetricColumn(
            label = "Upload",
            modifier = Modifier.weight(1f),
            value = if (connected && traffic.supported) {
                AuroraTelemetry.formatRate(traffic.uploadPerSecond)
            } else "—",
        )
        MetricDivider()
        MetricColumn(
            label = "Session",
            modifier = Modifier.weight(1f),
            value = if (connected) AuroraTelemetry.formatBytes(sessionBytes) else "—",
        )
    }
}

@Composable
private fun MetricColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = AuroraTypography.MetricValue, color = AuroraColors.TextPrimary)
        Text(text = label, style = AuroraTypography.MetricLabel, color = AuroraColors.TextMuted)
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 28.dp)
            .background(AuroraColors.Translucency.Divider),
    )
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
                Text("AUTO", style = AuroraTypography.Button, color = AuroraColors.AccentMint)
                Text("Route Profile", style = AuroraTypography.MetricLabel, color = AuroraColors.TextMuted)
            }
            RouteTopology(transport = transport, endpoint = endpoint, connected = connected)
        }
    }
}

// --------------------------------------------------------- live traffic ----

@Composable
private fun LiveTrafficCard(
    downHistory: List<Float>,
    upHistory: List<Float>,
    traffic: TrafficSample,
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(AuroraColors.GlassSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        TrafficWaveform(
                            history = downHistory.takeLast(24),
                            secondHistory = upHistory.takeLast(24),
                            modifier = Modifier.padding(horizontal = 4.dp).height(16.dp),
                            active = connected,
                        )
                    }
                    Text("Live Traffic", style = AuroraTypography.CardTitle)
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

            // The full-width waveform: the card's reason for existing.
            TrafficWaveform(
                history = downHistory,
                secondHistory = upHistory,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                active = connected,
                filled = true,
            )

            if (!connected) {
                Text("No session is running.", style = AuroraTypography.BodySmall, color = AuroraColors.TextMuted)
            } else if (!traffic.supported) {
                Text(
                    "This device does not report per-app traffic counters.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
            }
        }
    }
}
