package com.auroravpn.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// NETWORK ORBIT — dark is the primary and only theme.
//
// Light mode is not implemented. The spec allows that, and inverting this
// palette would not produce the same visual language; it would need its own
// careful pass that is not part of this phase.
private val OrbitColors = darkColorScheme(
  primary = OrbitAccent,
  onPrimary = Color(0xFF04150E),
  primaryContainer = OrbitAccentDim,
  onPrimaryContainer = Color(0xFFD6FFE6),
  secondary = OrbitAccentSecondary,
  onSecondary = Color(0xFF0A1424),
  secondaryContainer = OrbitAccentSecondaryDim,
  onSecondaryContainer = Color(0xFFDCE9FF),
  tertiary = OrbitAccentSecondary,
  onTertiary = Color(0xFF0A1424),
  background = OrbitBackground,
  onBackground = OrbitTextPrimary,
  surface = OrbitSurface,
  onSurface = OrbitTextPrimary,
  surfaceVariant = OrbitSurfaceElevated,
  onSurfaceVariant = OrbitTextSecondary,
  outline = OrbitHairlineStrong,
  outlineVariant = OrbitHairline,
  error = OrbitError,
  onError = Color(0xFF1A0309),
  scrim = Color(0xFF04060A),
)

@Composable
fun AuroraTheme(
  @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = OrbitColors,
    typography = AuroraTypography,
    content = content,
  )
}
