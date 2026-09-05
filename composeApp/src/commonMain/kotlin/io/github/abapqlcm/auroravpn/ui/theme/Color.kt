package io.github.abapqlcm.auroravpn.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Legacy elegant (kept for compat)
val ElegantPrimary = Color(0xFFD4AF37)
val ElegantOnPrimary = Color(0xFF0A0A0B)
val ElegantPrimaryContainer = Color(0xFF2A2008)
val ElegantOnPrimaryContainer = Color(0xFFFFE8A0)
val ElegantSecondary = Color(0xFFC9B88A)
val ElegantBackground = Color(0xFF0A0A0B)
val ElegantSurface = Color(0xFF141418)
val ElegantSurfaceCard = Color(0xFF1E1E20)
val ElegantSurfaceActive = Color(0xFF332F1A)
val ElegantOutline = Color(0xFF3A3520)
val ElegantTextPrimary = Color(0xFFF5F0D8)
val ElegantTextSecondary = Color(0xFF9E9580)
val ConnectedGreen = Color(0xFF34C759)
val ScanningAmber = Color(0xFFFF9500)
val ErrorRed = Color(0xFFFF3B30)

object AppPalette {
    // Gold lux
    val accent = Color(0xFFD4AF37)          // metallic gold
    val accentVariant = Color(0xFFB8860B)   // dark goldenrod
    val accentVariantAlt = Color(0xFFFFD700)
    val onAccent = Color(0xFF0A0A0B)

    val goldNeon = Color(0xFFFFD700)
    val goldDark = Color(0xFF8C6A12)
    val goldGlow = Color(0x33FFD700)

    val statusConnected = Color(0xFFD4AF37)
    val statusScanning = Color(0xFFFF9500)
    val statusError = Color(0xFFFF3B30)
    val debugCyan = Color(0xFFD4AF37)

    val surfaceRaised = Color(0xFF1A1A1D)
    val surfaceSunken = Color(0xFF0A0A0B)
    val groupBg = Color(0xFF1E1E20)
    val divider = Color(0xFF2A2A2C)
    val inactiveTrack = Color(0xFF3A3A3C)

    val textPrimary = Color(0xFFF5F0D8)
    val textSecondary = Color(0xFF9E9580)

    val navBackground = Color(0xFF141418)
    val navActive = Color(0xFFD4AF37)
    val navInactive = Color(0xFF8E8E93)
}

@Immutable
data class AppColors(
    val accent: Color,
    val onAccent: Color,
    val accentVariant: Color,
    val debugCyan: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val groupBg: Color,
    val divider: Color,
    val inactiveTrack: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val statusConnected: Color,
    val statusScanning: Color,
    val statusError: Color,
    val navBackground: Color,
    val navActive: Color,
    val navInactive: Color,
)

internal val darkAppColors = AppColors(
    accent = AppPalette.accent,
    onAccent = AppPalette.onAccent,
    accentVariant = AppPalette.accentVariant,
    debugCyan = AppPalette.debugCyan,
    surfaceRaised = AppPalette.surfaceRaised,
    surfaceSunken = AppPalette.surfaceSunken,
    groupBg = AppPalette.groupBg,
    divider = AppPalette.divider,
    inactiveTrack = AppPalette.inactiveTrack,
    textPrimary = AppPalette.textPrimary,
    textSecondary = AppPalette.textSecondary,
    statusConnected = AppPalette.statusConnected,
    statusScanning = AppPalette.statusScanning,
    statusError = AppPalette.statusError,
    navBackground = AppPalette.navBackground,
    navActive = AppPalette.navActive,
    navInactive = AppPalette.navInactive,
)

internal val lightAppColors = AppColors(
    accent = Color(0xFFB8860B),
    onAccent = Color(0xFFFFFFFF),
    accentVariant = Color(0xFF8C6A12),
    debugCyan = Color(0xFFB8860B),
    surfaceRaised = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFF7F3E6),
    groupBg = Color(0xFFECE8D8),
    divider = Color(0xFFDDD8C4),
    inactiveTrack = Color(0xFFD1CFC0),
    textPrimary = Color(0xFF1A1A0B),
    textSecondary = Color(0xFF6B6655),
    statusConnected = Color(0xFF7A6500),
    statusScanning = Color(0xFF8C5A00),
    statusError = Color(0xFFC0392B),
    navBackground = Color(0xFFF7F3E6),
    navActive = Color(0xFFB8860B),
    navInactive = Color(0xFF8A8A95),
)

val LocalAppColors = staticCompositionLocalOf { darkAppColors }

@Composable
fun appColors(): AppColors = LocalAppColors.current
