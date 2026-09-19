package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraNavRow
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.auroravpn.app.ui.home.RouteTopology
import com.whitedns.whiteaesther.data.AppSettings

/**
 * Routes: the current path, the profile that chose it, and the memory that
 * carries a working route from one network to the next.
 *
 * Every value is read from settings or engine state. The topology names the
 * transport settings actually hold, not the one the reference picture showed.
 */
@Composable
fun AuroraRoutesScreen(
    viewModel: AuroraViewModel,
    onEndpoints: () -> Unit,
    onTransport: () -> Unit,
    onIdentity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val activeEndpoint by viewModel.activeEndpoint.collectAsStateWithLifecycle()
    val scannerState by viewModel.endpointScannerState.collectAsStateWithLifecycle()

    val connected = connection is com.auroravpn.app.ui.AuroraConnectionState.Connected
    val carrierName = stringResource(settings.carrier.label)
    val routeProfile = if (settings.transport.isAutomatic) "AUTO" else settings.transport.wireName.uppercase()

    AuroraDetailScaffold(
        title = "Routes",
        onBack = { },
        modifier = modifier,
    ) {
        AuroraSectionHeader("Current Route")
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
                        text = routeProfile,
                        style = AuroraTypography.Button,
                        color = AuroraColors.AccentMint,
                    )
                    Text(
                        text = carrierName,
                        style = AuroraTypography.MetricLabel,
                        color = AuroraColors.TextMuted,
                    )
                }
                RouteTopology(
                    transport = settings.transport.wireName,
                    endpoint = activeEndpoint,
                    connected = connected,
                )
            }
        }

        AuroraSectionHeader("Endpoint")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingLine(
                    label = "Mode",
                    value = stringResource(settings.endpointMode.label),
                )
                SettingLine(
                    label = "Endpoint",
                    value = activeEndpoint.ifBlank { "Selected automatically" },
                )
                SettingLine(
                    label = "Known routes",
                    value = scannerState.results.size.toString(),
                )
                scannerState.error?.let { error ->
                    Text(
                        text = error,
                        style = AuroraTypography.BodySmall,
                        color = AuroraColors.Error,
                    )
                }
            }
        }

        AuroraSectionHeader("Route Memory")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingLine(
                    label = "Automatic transport",
                    value = if (settings.transport.isAutomatic) "On" else "Off",
                )
                SettingLine(
                    label = "Carrier fallback",
                    value = if (settings.automaticCarrier) "On" else "Off",
                )
                SettingLine(
                    label = "Dual stack",
                    value = if (settings.dualStack) "IPv4 + IPv6" else "IPv4",
                )
            }
        }

        AuroraSectionHeader("More")
        AuroraNavRow(
            title = "Endpoints",
            subtitle = "Scan, test and choose where the tunnel ends",
            onClick = onEndpoints,
        )
        AuroraNavRow(
            title = "Transport",
            subtitle = "MASQUE H3/H2, WireGuard, WIW and MIM",
            onClick = onTransport,
        )
        AuroraNavRow(
            title = "Identity & Provisioning",
            subtitle = "Export, import and inspect the device identity",
            onClick = onIdentity,
        )
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
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
