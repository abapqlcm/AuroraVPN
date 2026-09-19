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
import com.auroravpn.app.ui.design.AuroraButton
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTextField
import com.auroravpn.app.ui.design.AuroraTypography

@Composable
fun AuroraIdentityScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.identityMessage.collectAsStateWithLifecycle()
    val pasted = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    AuroraDetailScaffold(title = "Identity", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("Provisioning")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "The device's identity is provisioned by the engine and kept on the device. " +
                        "Uninstalling takes it, and exporting it lets a reinstall keep it.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
                Text(
                    text = "Auto-provisioning is ${if (settings.autoReprovision) "On" else "Off"}.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
            }
        }

        AuroraSectionHeader("Export & Import")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Exporting copies the identity to the clipboard. Importing replaces the " +
                        "device's identity with the one you paste.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
                message?.let { text ->
                    Text(
                        text = text.text,
                        style = AuroraTypography.BodySmall,
                        color = if (text.isError) AuroraColors.Error else AuroraColors.Secure,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuroraButton(text = "Export", onClick = { viewModel.exportIdentity() })
                    AuroraButton(
                        text = "Import",
                        onClick = { viewModel.importIdentity(pasted.value.trim()) },
                    )
                }
                AuroraTextField(
                    value = pasted.value,
                    onValueChange = { pasted.value = it },
                    label = "Paste an exported identity",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
