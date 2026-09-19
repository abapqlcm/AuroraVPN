package com.auroravpn.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
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
 *
 * Layout: the whole screen scrolls. Earlier versions pinned the results list to the
 * bottom of a non-scrolling Column, which handed the LazyColumn whatever vertical
 * room the two cards above had left -- on a small phone that was less than one row,
 * and the list then measured its items into that strip and squeezed the address and
 * the RTT together. Scrolling the Column and letting the list take its own intrinsic
 * height removes the constraint at the source rather than patching the row.
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
            .verticalScroll(rememberScrollState())
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
            // Not fillMaxWidth on a height-constrained lazy list: the Column scrolls,
            // and the list measures its items against the Column's full width without
            // a fixed height taking a share of the space the rows need.
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusDot(
                    selected = isSelected,
                    reachable = rttMillis >= 0,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp),
                )

                // The address takes whatever room the RTT leaves, and a long one
                // ends in an ellipsis rather than pushing the RTT off the card.
                // LTR is pinned because an address is a technical value; without
                // it a Persian UI reverses the octets and the colons of a long
                // IPv6 literal land in the wrong order.
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = endpointAddress(peer),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                    )
                    Text(
                        text = endpointFamily(peer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                // Reserved before the address is measured, so a wide address can
                // never crowd the latency reading. wide enough for five digits and
                // the unit without the column ever widening on a big number.
                Box(
                    modifier = Modifier.widthIn(min = 64.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = rttText(rttMillis),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Start,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    enabled = true,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Use endpoint $peer" },
                ) { Text(if (isSelected) "Selected" else "Select") }
            }
        }
    }
}

/**
 * The coloured dot that says what state this endpoint is in: filled and green when the
 * user has chosen it, a hollow grey one when it is merely reachable, red when a test
 * reported it down. Colour alone never carries the meaning -- the label and the RTT
 * text do too -- so this is decoration rather than an accessibility dependency.
 */
@Composable
private fun StatusDot(
    selected: Boolean,
    reachable: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when {
        selected -> MaterialTheme.colorScheme.primary
        reachable -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/**
 * The address, with the port pulled aside so the part people recognise is what the
 * ellipsis reaches last. A scanned peer is an address plus a port, and a long IPv6
 * literal plus `:8648` is wide enough on its own to overflow a small screen.
 */
private fun endpointAddress(peer: String): AnnotatedString = androidx.compose.ui.text.buildAnnotatedString {
    val hostEnd = peer.lastIndexOf(':')
    if (hostEnd <= 0) {
        append(peer)
        return@buildAnnotatedString
    }
    append(peer.substring(0, hostEnd))
    withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
        append(peer.substring(hostEnd))
    }
}

/** IPv4, IPv6 or something else, from the shape of the address rather than a stored flag. */
private fun endpointFamily(peer: String): String {
    val host = peer.substringBeforeLast(':')
    return when {
        host.contains(':') -> "IPv6"
        host.contains('.') && host.split('.').all { it.toIntOrNull() != null } -> "IPv4"
        else -> "host"
    }
}

private fun rttText(rttMillis: Long): String = when {
    rttMillis < 0 -> "unreachable"
    rttMillis >= 1_000 -> "${rttMillis / 1_000}.${(rttMillis % 1_000) / 100}s"
    else -> "${rttMillis}ms"
}
