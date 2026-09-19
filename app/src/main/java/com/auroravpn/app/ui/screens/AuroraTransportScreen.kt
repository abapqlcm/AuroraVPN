package com.auroravpn.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraChip
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraRadioButton
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.whitedns.whiteaesther.data.ScanStrategy
import com.whitedns.whiteaesther.data.TunnelProtocol

/**
 * Transport: which protocol the tunnel speaks.
 *
 * Only the protocols the engine actually knows are offered. Each is a real
 * [TunnelProtocol] with a wire name the engine understands, and choosing one
 * writes it straight to settings -- no intermediate selection state to keep in
 * sync, and no transport advertised that the engine cannot build.
 *
 * AUTO is not a transport; it is the engine resolving one, and the label says
 * what it does rather than pretending to be a peer of the others.
 */
@Composable
fun AuroraTransportScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Transport", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("Protocol")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TunnelProtocol.entries.forEach { protocol ->
                    TransportRow(
                        protocol = protocol,
                        selected = settings.transport == protocol,
                        onSelect = { viewModel.save(settings.copy(transport = protocol)) },
                    )
                }
            }
        }

        AuroraSectionHeader("Scan strategy")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ScanStrategy.entries.forEach { strategy ->
                    AuroraChip(
                        text = stringResource(strategy.label),
                        selected = settings.scanStrategy == strategy,
                        onClick = { viewModel.save(settings.copy(scanStrategy = strategy)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = "How the engine searches for a working endpoint when the transport is automatic.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun TransportRow(
    protocol: TunnelProtocol,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val label = stringResource(protocol.label)
    val description = when (protocol) {
        TunnelProtocol.AUTO -> "Work out what this network allows, and remember what worked."
        TunnelProtocol.H3 -> "MASQUE over HTTP/3, QUIC. The fastest when UDP gets through."
        TunnelProtocol.H2 -> "MASQUE over HTTP/2, TCP. The fallback when QUIC is blocked."
        TunnelProtocol.WIREGUARD -> "WireGuard inside MASQUE."
        TunnelProtocol.WARP_IN_WARP -> "Two MASQUE hops; the inner handshake rides through the outer."
        else -> ""
    }

    AuroraGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AuroraDimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuroraRadioButton(selected = selected, onClick = onSelect)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = AuroraTypography.CardTitle,
                    color = AuroraColors.TextPrimary,
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = AuroraTypography.BodySmall,
                        color = AuroraColors.TextMuted,
                    )
                }
            }
        }
    }
}
