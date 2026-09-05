package io.github.abapqlcm.auroravpn.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),
    onPrimary = Color(0xFF0A0A0B),
    primaryContainer = Color(0xFF2A2008),
    onPrimaryContainer = Color(0xFFFFE8A0),
    secondary = Color(0xFFC9B88A),
    background = Color(0xFF0A0A0B),
    onBackground = Color(0xFFF5F0D8),
    surface = Color(0xFF141418),
    onSurface = Color(0xFFF5F0D8),
    surfaceVariant = Color(0xFF1E1E20),
    onSurfaceVariant = Color(0xFFC9B88A),
    outline = Color(0xFF3A3520),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB8860B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE8A0),
    onPrimaryContainer = Color(0xFF2A2008),
    secondary = Color(0xFF6B6655),
    background = Color(0xFFF7F3E6),
    onBackground = Color(0xFF1A1A0B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A0B),
    surfaceVariant = Color(0xFFECE8D8),
    onSurfaceVariant = Color(0xFF6B6655),
    outline = Color(0xFFDDD8C4),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    isRtl: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) darkAppColors else lightAppColors

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalAppColors provides appColors,
    ) {
        BoxWithConstraints {
            val scale = (maxWidth.value / 411f).coerceIn(0.7f, 1.1f)
            CompositionLocalProvider(LocalScaleFactor provides scale) {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = appTypography(isRtl = isRtl),
                    content = content,
                )
            }
        }
    }
}
