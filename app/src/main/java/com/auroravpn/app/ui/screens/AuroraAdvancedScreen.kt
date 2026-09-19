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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraSwitch
import com.auroravpn.app.ui.design.AuroraTextField
import com.auroravpn.app.ui.design.AuroraTypography

/**
 * Advanced: the settings that configure the tunnel itself rather than how it
 * looks.
 *
 * Every switch writes to the real field it names. None of them is a placeholder
 * kept for symmetry: if a capability is not here, it is on another screen, and
 * nothing here is removed from the engine.
 */
@Composable
fun AuroraAdvancedScreen(viewModel: AuroraViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Advanced", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("DNS")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuroraTextField(
                    value = settings.dnsServers,
                    onValueChange = { viewModel.save(settings.copy(dnsServers = it)) },
                    label = "Custom DNS servers",
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Comma-separated. Leave empty to use the engine's defaults.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
            }
        }

        AuroraSectionHeader("Routing")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SwitchRow(
                    label = "Dual stack",
                    subtitle = "IPv4 and IPv6 rather than IPv4 alone",
                    checked = settings.dualStack,
                    onCheckedChange = { viewModel.save(settings.copy(dualStack = it)) },
                )
                SwitchRow(
                    label = "Route memory",
                    subtitle = "Remember which routes worked on this network",
                    checked = settings.routeSniff,
                    onCheckedChange = { viewModel.save(settings.copy(routeSniff = it)) },
                )
                SwitchRow(
                    label = "Split tunneling",
                    subtitle = "Route chosen apps outside the tunnel",
                    checked = settings.splitTunnel.packages.isNotEmpty(),
                    onCheckedChange = {
                        viewModel.save(
                            settings.copy(
                                splitTunnel = settings.splitTunnel.copy(
                                    mode = if (it) com.whitedns.whiteaesther.data.SplitTunnelMode.EXCEPT else com.whitedns.whiteaesther.data.SplitTunnelMode.ALL,
                                ),
                            ),
                        )
                    },
                )
                AuroraTextField(
                    value = settings.routeBlock,
                    onValueChange = { viewModel.save(settings.copy(routeBlock = it)) },
                    label = "Blocked routes",
                    modifier = Modifier.fillMaxWidth(),
                )
                AuroraTextField(
                    value = settings.routeDirect,
                    onValueChange = { viewModel.save(settings.copy(routeDirect = it)) },
                    label = "Direct routes",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AuroraSectionHeader("Proxy")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuroraTextField(
                    value = settings.upstreamProxy,
                    onValueChange = { viewModel.save(settings.copy(upstreamProxy = it)) },
                    label = "SOCKS5 upstream proxy",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AuroraSectionHeader("Obfuscation")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SwitchRow(
                    label = "TLS fragmentation",
                    subtitle = "Break up the TLS ClientHello to defeat pattern matching",
                    checked = settings.fragmentTls,
                    onCheckedChange = { viewModel.save(settings.copy(fragmentTls = it)) },
                )
                SwitchRow(
                    label = "Encrypted ClientHello",
                    subtitle = "Encrypt the SNI so it cannot be read in transit",
                    checked = settings.encryptedHello,
                    onCheckedChange = { viewModel.save(settings.copy(encryptedHello = it)) },
                )
                AuroraTextField(
                    value = settings.noizeProfile,
                    onValueChange = { viewModel.save(settings.copy(noizeProfile = it)) },
                    label = "Noize profile",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AuroraSectionHeader("Engine")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuroraTextField(
                    value = settings.engineLogLevel,
                    onValueChange = { viewModel.save(settings.copy(engineLogLevel = it)) },
                    label = "Engine log level",
                    modifier = Modifier.fillMaxWidth(),
                )
                AuroraTextField(
                    value = settings.tlsGroups,
                    onValueChange = { viewModel.save(settings.copy(tlsGroups = it)) },
                    label = "TLS groups",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = AuroraTypography.CardTitle, color = AuroraColors.TextPrimary)
            Text(text = subtitle, style = AuroraTypography.BodySmall, color = AuroraColors.TextMuted)
        }
        AuroraSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
