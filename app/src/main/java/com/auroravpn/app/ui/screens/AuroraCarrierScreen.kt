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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraButton
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraRadioButton
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.whitedns.whiteaesther.data.Carrier

@Composable
fun AuroraCarrierScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val country = androidx.compose.runtime.remember { viewModel.detectedCountry() }
    val bridgesMessage by viewModel.bridgesMessage.collectAsStateWithLifecycle()

    AuroraDetailScaffold(title = "Carrier", onBack = onBack, modifier = modifier) {
        AuroraSectionHeader("Carrier")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Carrier.entries.forEach { carrier ->
                    val selected = settings.carrier == carrier && settings.secondCarrier == null
                    val isSecond = settings.secondCarrier == carrier
                    CarrierRow(
                        label = stringResource(carrier.label),
                        description = carrierDescription(carrier),
                        selected = selected,
                        isSecond = isSecond,
                        onSelect = { viewModel.save(settings.copy(carrier = carrier, secondCarrier = null)) },
                    )
                }
                Text(
                    text = "Aether is the engine this app is built around. Psiphon and Tor are here " +
                        "for the networks the engine cannot get out of.",
                    style = AuroraTypography.BodySmall,
                    color = AuroraColors.TextMuted,
                )
            }
        }

        AuroraSectionHeader("Psiphon region")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = country.ifBlank { "Any" },
                    style = AuroraTypography.Endpoint,
                )
                AuroraButton(
                    text = "Favourite country",
                    onClick = { viewModel.fetchBridges(settings, country) },
                )
                bridgesMessage?.let { message ->
                    Text(
                        text = message.text,
                        style = AuroraTypography.BodySmall,
                        color = if (message.isError) AuroraColors.Error else AuroraColors.TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun CarrierRow(
    label: String,
    description: String,
    selected: Boolean,
    isSecond: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuroraRadioButton(selected = selected || isSecond, onClick = onSelect)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = AuroraTypography.CardTitle, color = AuroraColors.TextPrimary)
            Text(text = description, style = AuroraTypography.BodySmall, color = AuroraColors.TextMuted)
        }
    }
}

private fun carrierDescription(carrier: Carrier): String = when (carrier) {
    Carrier.AETHER -> "The MASQUE engine this app is built around."
    Carrier.PSIPHON -> "In its own process, for the networks the engine cannot reach."
    Carrier.TOR -> "Three relays deep, and no UDP at all."
}
