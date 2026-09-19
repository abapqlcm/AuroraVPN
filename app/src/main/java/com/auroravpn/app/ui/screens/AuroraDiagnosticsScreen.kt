package com.auroravpn.app.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography

@Composable
fun AuroraDiagnosticsScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val engineStatus by viewModel.engineStatus.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Diagnostics", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("Top")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiagnosticLine("State", connection.toString())
                DiagnosticLine("Engine", engineStatus.stage.name)
            }
        }

        AuroraSectionHeader("Network")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiagnosticLine("Tunnel exit", addresses.tunnel ?: "Not observed")
                DiagnosticLine("Device address", addresses.real ?: "Not observed")
                DiagnosticLine("DNS", settings.dnsServers.ifBlank { "Engine default" })
                DiagnosticLine("Stack", if (settings.dualStack) "IPv4 + IPv6" else "IPv4")
            }
        }

        AuroraSectionHeader("Engine")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiagnosticLine("Version", viewModel.engineVersion.value ?: "-")
                DiagnosticLine("Carrier", settings.carrier.wireName)
                DiagnosticLine("Transport", settings.transport.wireName)
            }
        }
    }
}

@Composable
private fun DiagnosticLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AuroraTypography.MetricLabel,
            color = AuroraColors.TextMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = AuroraTypography.Endpoint,
            color = AuroraColors.TextPrimary,
            maxLines = 2,
        )
    }
}
