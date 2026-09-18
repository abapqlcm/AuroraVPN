package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel
import com.whitedns.whiteaesther.service.LogLevel

/**
 * The engine and service log.
 *
 * The buffer is the one [com.whitedns.whiteaesther.service.EngineLog] holds: the engine
 * pushes into it from its native callback and the service from its own scope, and this
 * screen only reads it.
 */
@Composable
fun AuroraLogsScreen(
    viewModel: AuroraViewModel,
    onClear: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Follow the newest entry while the user is at the bottom of the list.
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Logs", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShare, enabled = logs.isNotEmpty()) { Text("Share") }
                OutlinedButton(onClick = onClear, enabled = logs.isNotEmpty()) { Text("Clear") }
            }
        }
        Text(
            text = "${logs.size} entries. The engine writes these; the screen only reads them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(logs, key = { it.timeMillis.toString() + it.message }) { entry ->
                LogRow(entry = entry)
            }
        }
    }
}

@Composable
private fun LogRow(entry: com.whitedns.whiteaesther.service.LogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = entry.formattedTime() + "  " + entry.tag,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = when (entry.level) {
                    LogLevel.ERROR -> MaterialTheme.colorScheme.error
                    LogLevel.WARN -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}
