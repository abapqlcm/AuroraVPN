package com.auroravpn.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraButton
import com.auroravpn.app.ui.design.AuroraChip
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraShapes
import com.auroravpn.app.ui.design.AuroraTypography
import com.auroravpn.app.ui.design.AuroraTextField
import com.whitedns.whiteaesther.EndpointOperation
import com.whitedns.whiteaesther.data.EndpointMode

/**
 * Endpoint discovery, testing and selection.
 *
 * The list, the progress text and the errors all come from
 * [com.whitedns.whiteaesther.MainViewModel.endpointScannerState], which is fed by
 * the engine's own scanner. Nothing here is cached or invented, and an empty list
 * means a scan has not produced results yet.
 *
 * The whole screen scrolls. An earlier version pinned the results list to the
 * bottom of a non-scrolling Column, which handed the LazyColumn whatever vertical
 * room the two cards above had left — on a small phone that was less than one
 * row, and the list then measured its items into that strip and squeezed the
 * address and the RTT together. Letting the list take its own intrinsic height
 * removes the constraint at the source rather than patching the row.
 *
 * The endpoint scan has an unresolved failure mode: see
 * docs/HERMES_CONTINUATION.md. The screen preserves the real scanner, keeps
 * cancellation and lifecycle safe, and reports errors instead of substituting
 * results.
 */
@Composable
fun AuroraEndpointsScreen(
    viewModel: AuroraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scanner by viewModel.endpointScannerState.collectAsStateWithLifecycle()

    val running = scanner.operation != null && scanner.operation != EndpointOperation.CANCELLING
    val selected = settings.customEndpoint

    AuroraDetailScaffold(
        title = "Endpoints",
        onBack = onBack,
        modifier = modifier,
        actions = {
            AuroraButton(
                text = if (running) "Cancel" else "Scan",
                onClick = {
                    if (running) {
                        viewModel.cancelEndpointScan()
                    } else {
                        viewModel.scanEndpoints(settings)
                    }
                },
            )
        },
    ) {
        AuroraSectionHeader("Discovery")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = when {
                        scanner.operation == EndpointOperation.SCANNING ->
                            "Scanning for reachable endpoints…"
                        scanner.operation == EndpointOperation.TESTING ->
                            "Testing the selected endpoint…"
                        scanner.operation == EndpointOperation.CANCELLING ->
                            "Cancelling…"
                        scanner.message != null -> scanner.message.orEmpty()
                        else -> "No scan has run yet."
                    },
                    style = AuroraTypography.Body,
                    color = AuroraColors.TextSecondary,
                )
                scanner.error?.let { error ->
                    Text(
                        text = error,
                        style = AuroraTypography.BodySmall,
                        color = AuroraColors.Error,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraButton(
                        text = "Test selected",
                        onClick = { viewModel.testEndpoint(settings) },
                        enabled = !running && selected.isNotBlank(),
                    )
                }
            }
        }

        AuroraSectionHeader("Selected endpoint")
        AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AuroraDimensions.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuroraTextField(
                    value = selected,
                    onValueChange = { viewModel.save(settings.copy(customEndpoint = it)) },
                    label = "Address",
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Endpoint address" },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EndpointMode.entries.forEach { mode ->
                        AuroraChip(
                            text = stringResource(mode.label),
                            selected = settings.endpointMode == mode,
                            onClick = { viewModel.save(settings.copy(endpointMode = mode)) },
                        )
                    }
                }
                if (selected.isNotBlank()) {
                    AuroraButton(
                        text = "Clear pinned endpoint",
                        onClick = { viewModel.resetEndpoint(settings) },
                        enabled = !running,
                    )
                }
            }
        }

        AuroraSectionHeader("Results (${scanner.results.size})")
        if (scanner.results.isEmpty()) {
            EmptyState(
                title = if (running) "Scanning…" else "No endpoints yet",
                body = if (running) {
                    "The engine is looking for reachable endpoints."
                } else {
                    "Run a scan to find endpoints the engine can reach from this network."
                },
            )
        } else {
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
private fun EmptyState(title: String, body: String) {
    AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AuroraDimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = AuroraTypography.CardTitle)
            Text(
                text = body,
                style = AuroraTypography.BodySmall,
                color = AuroraColors.TextMuted,
            )
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
    AuroraGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AuroraShapes.Card)
            .clickable(onClick = onSelect)
            .semantics { contentDescription = "Endpoint $peer" },
    ) {
        Column(modifier = Modifier.padding(AuroraDimensions.cardPadding)) {
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
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = endpointAddress(peer),
                            style = AuroraTypography.EndpointAddress,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start,
                        )
                    }
                    Text(
                        text = endpointFamily(peer),
                        style = AuroraTypography.MetricLabel,
                        color = AuroraColors.TextMuted,
                        maxLines = 1,
                    )
                }

                // Reserved before the address is measured, so a wide address can
                // never crowd the latency reading.
                Box(
                    modifier = Modifier.widthIn(min = 64.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = rttText(rttMillis),
                        style = AuroraTypography.EndpointAddress,
                        color = AuroraColors.TextSecondary,
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
                AuroraButton(
                    text = if (isSelected) "Selected" else "Select",
                    onClick = onSelect,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Use endpoint $peer" },
                )
            }
        }
    }
}

/**
 * The coloured dot that says what state this endpoint is in: filled and mint when
 * the user has chosen it, hollow grey when merely reachable, red when a test
 * reported it down. Colour alone never carries the meaning — the label and the
 * RTT text do too — so this is decoration rather than an accessibility dependency.
 */
@Composable
private fun StatusDot(
    selected: Boolean,
    reachable: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when {
        selected -> AuroraColors.AccentMint
        reachable -> AuroraColors.TextMuted
        else -> AuroraColors.Error
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
private fun endpointAddress(peer: String): AnnotatedString = buildAnnotatedString {
    val hostEnd = peer.lastIndexOf(':')
    if (hostEnd <= 0) {
        append(peer)
        return@buildAnnotatedString
    }
    append(peer.substring(0, hostEnd))
    withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold)) {
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
