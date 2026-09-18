package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.data.AppLanguage
import com.whitedns.whiteaesther.data.ScanStrategy
import com.whitedns.whiteaesther.data.ThemeMode
import com.whitedns.whiteaesther.data.TunnelProtocol

/**
 * Transport, obfuscation and TLS hardening.
 *
 * Each choice maps to a field the engine reads from its config JSON, so a change here is
 * a change to what the engine will be told on the next connect. Nothing here invents a
 * transport the engine does not have.
 */
@Composable
fun AuroraTransportScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Transport", style = MaterialTheme.typography.headlineSmall)

        ChoiceCard(
            title = "Tunnel protocol",
            subtitle = "Automatic works out what this network allows, and remembers " +
                "what succeeded so the next connect starts there.",
            entries = TunnelProtocol.entries,
            selected = settings.transport,
            labelOf = { stringResource(it.label) },
            onSelect = { viewModel.save(settings.copy(transport = it)) },
        )

        ChoiceCard(
            title = "Scan mode",
            subtitle = "How hard the endpoint search looks.",
            entries = ScanStrategy.entries,
            selected = settings.scanStrategy,
            labelOf = { stringResource(it.label) },
            onSelect = { viewModel.save(settings.copy(scanStrategy = it)) },
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Obfuscation", style = MaterialTheme.typography.titleMedium)
                ObfuscationChoice(
                    selected = settings.noizeProfile,
                    onSelect = { viewModel.save(settings.copy(noizeProfile = it)) },
                )
                SwitchRow(
                    title = "Fragment the TLS handshake",
                    subtitle = "Splits the ClientHello so an inspector reading only the " +
                        "first segment does not see the SNI.",
                    checked = settings.fragmentTls,
                    onCheckedChange = { viewModel.save(settings.copy(fragmentTls = it)) },
                )
                SwitchRow(
                    title = "Encrypted Client Hello",
                    subtitle = "Hides the SNI from an inspector that reassembles.",
                    checked = settings.encryptedHello,
                    onCheckedChange = { viewModel.save(settings.copy(encryptedHello = it)) },
                )
            }
        }

        ChoiceCard(
            title = "Language",
            entries = AppLanguage.entries,
            selected = settings.language,
            labelOf = { labelOfLanguage(it) },
            onSelect = { viewModel.save(settings.copy(language = it)) },
        )

        ChoiceCard(
            title = "Theme",
            entries = ThemeMode.entries,
            labelOf = { stringResource(it.label) },
            selected = settings.themeMode,
            onSelect = { viewModel.save(settings.copy(themeMode = it)) },
        )
    }
}

@Composable
private fun ObfuscationChoice(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val profiles = listOf("off", "light", "balanced", "firewall", "aggressive")
    profiles.forEach { profile ->
        AuroraSelectableRow(
            title = profile.replaceFirstChar { it.uppercase() },
            selected = selected == profile,
            onSelect = { onSelect(profile) },
        )
    }
}

private fun labelOfLanguage(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> "System"
    AppLanguage.ENGLISH -> "English"
    AppLanguage.PERSIAN -> "فارسی"
}
