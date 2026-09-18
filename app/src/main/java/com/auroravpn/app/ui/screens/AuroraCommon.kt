package com.auroravpn.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * The small pieces every Aurora screen shares.
 *
 * Kept deliberately plain: this phase is about each control being wired to the setting
 * it writes, and a custom look is what the Network Orbit phase is for.
 */

/** A titled card of mutually exclusive options. */
@Composable
fun <T> ChoiceCard(
    title: String,
    entries: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                entries.forEach { entry ->
                    AuroraSelectableRow(
                        title = labelOf(entry),
                        selected = entry == selected,
                        onSelect = { onSelect(entry) },
                    )
                }
            }
        }
    }
}

/** A labelled radio row. */
@Composable
fun AuroraSelectableRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

/** A card with one switch in it. */
@Composable
fun SwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        SwitchRow(
            title = title,
            subtitle = subtitle,
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/** A switch row, with the label doing the describing. */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
