package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.auroravpn.app.ui.AuroraAdvancedDestination
import com.auroravpn.app.ui.AuroraViewModel

/**
 * The index of advanced screens.
 *
 * Five readable icons is what a phone holds, so Identity, Diagnostics, Logs, Settings
 * and Carrier & Chain live here rather than on the bottom bar.
 */
@Composable
fun AuroraMoreScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "More", style = MaterialTheme.typography.headlineSmall)
        AuroraAdvancedDestination.entries.forEach { destination ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(destination.route) },
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = destination.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = destination.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val AuroraAdvancedDestination.description: String
    get() = when (this) {
        AuroraAdvancedDestination.Identity -> "Export and import the engine's identity, and provisioning."
        AuroraAdvancedDestination.Diagnostics -> "Engine, carrier, network and address state as it happens."
        AuroraAdvancedDestination.Logs -> "What the engine and the service have said."
        AuroraAdvancedDestination.Settings -> "Every field the engine reads."
        AuroraAdvancedDestination.Carrier -> "Psiphon, Tor, bridges and the mihomo chain."
    }
