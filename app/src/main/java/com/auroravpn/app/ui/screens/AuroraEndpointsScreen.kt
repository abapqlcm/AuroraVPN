package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.EndpointOperation
import com.whitedns.whiteaesther.data.AppSettings
import com.whitedns.whiteaesther.data.EndpointMode

/**
 * Endpoint discovery, testing and selection.
 *
 * The list, the progress text and the errors all come from
 * [com.whitedns.whiteaesther.MainViewModel.endpointScannerState], which is fed by the
 * engine's own scanner. Nothing here is cached or invented, and an empty list means a
 * scan has not produced results yet.
 */
@Composable
fun AuroraEndpointsScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scanner by viewModel.endpointScannerState.collectAsStateWithLifecycle()

    val running = scanner.operation != null && scanner.operation != EndpointOperation.CANCELLING
    val selected = settings.customEndpoint

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Endpoints", style = MaterialTheme.typography.headlineSmall)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Discovery", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = scanner.message
                        ?: if (running) "Scanning…" else "Not scanning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                scanner.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.scanEndpoints(settings) },
                        enabled = !running,
                        modifier = Modifier.semantics { contentDescription = "Scan for endpoints" },
                    ) { Text("Scan") }
                    OutlinedButton(
                        onClick = { viewModel.testEndpoint(settings) },
                        enabled = !running && selected.isNotBlank(),
                    ) { Text("Test selected") }
                    OutlinedButton(
                        onClick = { viewModel.cancelEndpointScan() },
                        enabled = running,
                    ) { Text("Cancel") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Selected endpoint", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = selected,
                    onValueChange = { viewModel.save(settings.copy(customEndpoint = it)) },
                    label = { Text("Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EndpointMode.entries.forEach { mode ->
                        AuroraSelectableRow(
                            title = stringResource(mode.label),
                            selected = settings.endpointMode == mode,
                            onSelect = { viewModel.save(settings.copy(endpointMode = mode)) },
                        )
                    }
                }
                if (selected.isNotBlank()) {
                    OutlinedButton(onClick = { viewModel.resetEndpoint(settings) }) {
                        Text("Clear pinned endpoint")
                    }
                }
            }
        }

        if (scanner.results.isNotEmpty()) {
            Text(text = "Results", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(scanner.results, key = { it.peer }) { result ->
                    EndpointRow(
                        peer = result.peer,
                        rttMillis = result.rttMillis,
                        isSelected = selected == result.peer,
                        onSelect = {
                            viewModel.save(settings.copy(customEndpoint = result.peer))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EndpointRow(
    peer: String,
    rttMillis: Long,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Endpoint $peer" },
        onClick = onSelect,
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The address owns everything the latency reading does not need, and
            // a long one ends in an ellipsis rather than pushing the RTT off the
            // card. Sizing it with softWrap=false and Visible overflow -- the
            // earlier attempt -- makes the text lay out at its intrinsic width
            // and draw past its own box, which is what rendered the two columns
            // on top of each other.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    textAlign = TextAlign.Start,
                )
                if (isSelected) {
                    Text(
                        text = "Selected",
                        style = customLabelStyle(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
            // Reserved before the address is measured, so a wide address can
            // never crowd it. The longest thing a scan reports is four digits
            // plus the unit; 56dp holds that with room to spare on any screen.
            Text(
                text = if (rttMillis >= 0) "${rttMillis}ms" else "unreachable",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .widthIn(min = 48.dp)
                    .align(Alignment.CenterVertically),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun customLabelStyle() = MaterialTheme.typography.labelSmall
