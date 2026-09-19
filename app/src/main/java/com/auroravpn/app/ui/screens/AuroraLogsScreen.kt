package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.auroravpn.app.ui.design.AuroraButton
import com.auroravpn.app.ui.design.AuroraColors
import com.auroravpn.app.ui.design.AuroraDetailScaffold
import com.auroravpn.app.ui.design.AuroraDimensions
import com.auroravpn.app.ui.design.AuroraGlassCard
import com.auroravpn.app.ui.design.AuroraSectionHeader
import com.auroravpn.app.ui.design.AuroraTypography
import com.whitedns.whiteaesther.service.LogEntry
import com.whitedns.whiteaesther.service.LogLevel

@Composable
fun AuroraLogsScreen(
    viewModel: AuroraViewModel,
    onClear: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    AuroraDetailScaffold(
        title = "Logs",
        onBack = onBack,
        modifier = modifier,
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuroraButton(text = "Clear", onClick = onClear, enabled = logs.isNotEmpty())
                AuroraButton(text = "Share", onClick = onShare, enabled = logs.isNotEmpty())
            }
        },
    ) {
        AuroraSectionHeader("Engine record (${logs.size})")
        if (logs.isEmpty()) {
            AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nothing has been logged yet.",
                    style = AuroraTypography.Body,
                    color = AuroraColors.TextMuted,
                    modifier = Modifier.padding(AuroraDimensions.cardPadding),
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs, key = { it.timeMillis.toString() + it.message.hashCode() }) { entry ->
                    LogRow(entry)
                }
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    AuroraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AuroraDimensions.cardPadding)) {
            Text(
                text = entry.level.name,
                style = AuroraTypography.MetricLabel,
                color = severityColor(entry.level),
            )
            Text(
                text = entry.message,
                style = AuroraTypography.Endpoint,
                color = AuroraColors.TextPrimary,
            )
        }
    }
}

private fun severityColor(level: LogLevel): Color = when (level) {
    LogLevel.ERROR -> AuroraColors.Error
    LogLevel.WARN -> AuroraColors.Warning
    else -> AuroraColors.TextMuted
}
