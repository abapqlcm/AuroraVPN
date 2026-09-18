package com.auroravpn.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auroravpn.app.ui.AuroraViewModel

/**
 * Identity and provisioning.
 *
 * Export reads the identity the engine holds; import writes one back. Neither pretends
 * to succeed: the outcome the user sees is the message the bridge returned, and a null
 * payload means nothing was written.
 */
@Composable
fun AuroraIdentityScreen(
    viewModel: AuroraViewModel,
    modifier: Modifier = Modifier,
) {
    val identityMessage by viewModel.identityMessage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Export lands in a file the user names; import comes from one they pick.
    val createFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val payload = viewModel.exportIdentity()
        if (payload == null) {
            viewModel.reportIdentityWrite("The engine has no identity to export.")
            return@rememberLauncherForActivityResult
        }
        uri ?: return@rememberLauncherForActivityResult
        val error = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toByteArray())
            } ?: "Could not open that file."
        }.exceptionOrNull()?.message
        viewModel.reportIdentityWrite(error)
    }

    val openFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val payload = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().decodeToString()
            }
        }.getOrNull()
        if (payload.isNullOrBlank()) {
            viewModel.reportIdentityWrite("That file has no identity in it.")
            return@rememberLauncherForActivityResult
        }
        viewModel.importIdentity(payload)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Identity", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Identity file", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "The engine keeps its own identity. Export copies it out; " +
                        "import replaces it. A bad import is refused, and the engine says " +
                        "what was wrong.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { createFile.launch("aurora-identity.json") }) {
                        Text("Export")
                    }
                    OutlinedButton(onClick = { openFile.launch(arrayOf("application/json", "text/plain")) }) {
                        Text("Import")
                    }
                }
            }
        }

        SwitchCard(
            title = "Automatic reprovisioning",
            subtitle = "Ask the engine for a fresh identity when a connection keeps " +
                "failing, rather than leaving the user to notice.",
            checked = settings.autoReprovision,
            onCheckedChange = { viewModel.save(settings.copy(autoReprovision = it)) },
        )

        identityMessage?.let { message ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (message.isError) "Failed" else "Done",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (message.isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { viewModel.clearIdentityMessage() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
