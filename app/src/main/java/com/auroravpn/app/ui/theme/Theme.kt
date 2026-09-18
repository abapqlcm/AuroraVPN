package com.auroravpn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AuroraVPN is dark-first: the black + gold identity reads correctly against a
// dark surface, and a tunnel client is the kind of app people use at night.
// The light path is intentionally not supported yet — it would need its own
// carefully-tuned palette, not an inverted dark one.
private val AuroraColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF050505),
    primaryContainer = GoldDimmed,
    onPrimaryContainer = GoldSoft,
    secondary = GoldSoft,
    onSecondary = Color(0xFF050505),
    tertiary = AuroraGreen,
    background = Onyx,
    onBackground = Color(0xFFEDEDF0),
    surface = OnyxRaised,
    onSurface = Color(0xFFEDEDF0),
    surfaceVariant = OnyxCard,
    onSurfaceVariant = Color(0xFFB4B4BC),
    outline = OnyxStroke,
    outlineVariant = Color(0xFF1E1E24),
    error = AuroraRed,
    onError = Color(0xFF050505),
)

@Composable
fun AuroraTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AuroraColors,
        typography = AuroraTypography,
        content = content,
    )
}
