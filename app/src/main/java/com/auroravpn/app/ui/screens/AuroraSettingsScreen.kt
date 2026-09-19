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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraChip
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraNavRow
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraSwitch
import com.auroravpn.app.ui.design.AuroraTypography
import com.whitedns.whiteaesther.data.AppLanguage
import com.whitedns.whiteaesther.data.ThemeMode

/**
 * Settings: the real SettingsRepository, written straight back through the
 * same save() everything else uses.
 *
 * Language and theme are the two preferences a user looks for first, and both
 * are real: language switches the app locale, theme switches the colour scheme.
 * Everything below them is a detail screen, so a setting is never more than
 * one tap from the thing it configures.
 */
@Composable
fun AuroraSettingsScreen(
    viewModel: AuroraViewModel,
    onIdentity: () -> Unit,
    onDiagnostics: () -> Unit,
    onLogs: () -> Unit,
    onCarrier: () -> Unit,
    onChain: () -> Unit,
    onAdvanced: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Settings", onBack = { }, modifier = modifier) {
        AuroraSectionHeader("Appearance")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Theme", style = AuroraTypography.CardTitle)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        AuroraChip(
                            text = stringResource(mode.label),
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.save(settings.copy(themeMode = mode)) },
                        )
                    }
                }
                Text(text = "Language", style = AuroraTypography.CardTitle)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { language ->
                        AuroraChip(
                            text = languageTag(language),
                            selected = settings.language == language,
                            onClick = { viewModel.save(settings.copy(language = language)) },
                        )
                    }
                }
            }
        }

        AuroraSectionHeader("Connection")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AuroraDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SwitchRow(
                    label = "Carrier fallback",
                    subtitle = "Let the app find the way out: engine first, then Psiphon and Tor",
                    checked = settings.automaticCarrier,
                    onCheckedChange = { viewModel.save(settings.copy(automaticCarrier = it)) },
                )
                SwitchRow(
                    label = "Auto-provisioning",
                    subtitle = "Ask the engine to provision an identity when none is present",
                    checked = settings.autoReprovision,
                    onCheckedChange = { viewModel.save(settings.copy(autoReprovision = it)) },
                )
                SwitchRow(
                    label = "Kill switch",
                    subtitle = "Block traffic that would otherwise escape the tunnel",
                    checked = settings.killSwitch,
                    onCheckedChange = { viewModel.save(settings.copy(killSwitch = it)) },
                )
                SwitchRow(
                    label = "Strict kill switch",
                    subtitle = "Also block traffic while reconnecting",
                    checked = settings.strictKillSwitch,
                    onCheckedChange = { viewModel.save(settings.copy(strictKillSwitch = it)) },
                )
                SwitchRow(
                    label = "Dual stack",
                    subtitle = "Use IPv4 and IPv6 rather than IPv4 alone",
                    checked = settings.dualStack,
                    onCheckedChange = { viewModel.save(settings.copy(dualStack = it)) },
                )
            }
        }

        AuroraSectionHeader("Diagnostics")
        AuroraNavRow(title = "Identity & Provisioning", subtitle = "Export, import and inspect the device identity", onClick = onIdentity)
        AuroraNavRow(title = "Diagnostics", subtitle = "VPN state, DNS, network identity and engine status", onClick = onDiagnostics)
        AuroraNavRow(title = "Logs", subtitle = "Severity, timestamps and the engine's own record", onClick = onLogs)

        AuroraSectionHeader("Advanced")
        AuroraNavRow(title = "Carrier", subtitle = "Aether, Psiphon, Tor, bridges and regions", onClick = onCarrier)
        AuroraNavRow(title = "Chain", subtitle = "Chain state, nodes and testing", onClick = onChain)
        AuroraNavRow(title = "Advanced", subtitle = "DNS, split tunneling, SOCKS5, routing rules and obfuscation", onClick = onAdvanced)
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

private fun languageTag(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> "System"
    else -> language.tag.uppercase()
}
