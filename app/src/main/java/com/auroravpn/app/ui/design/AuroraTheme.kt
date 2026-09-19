package com.auroravpn.app.ui.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The Aurora colour scheme.
 *
 * The app is dark by design: the background is #080D14 and the surfaces are
 * glass on top of it, and a light scheme would fight every screen. ThemeMode is
 * therefore honoured by the activity's resource configuration rather than by a
 * second palette here -- a light Aurora is not part of this visual language.
 */
@Composable
fun AuroraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AuroraColors.AccentMint,
            onPrimary = Color.Black,
            primaryContainer = AuroraColors.GlassSurface,
            onPrimaryContainer = AuroraColors.TextPrimary,
            secondary = AuroraColors.BlueSecondary,
            onSecondary = Color.White,
            background = AuroraColors.Background,
            onBackground = AuroraColors.TextPrimary,
            surface = AuroraColors.GlassSurface,
            onSurface = AuroraColors.TextPrimary,
            surfaceVariant = AuroraColors.GlassSurfaceStrong,
            onSurfaceVariant = AuroraColors.TextSecondary,
            outline = AuroraColors.GlassBorder,
            outlineVariant = AuroraColors.GlassBorder,
            error = AuroraColors.Error,
            onError = Color.Black,
        ),
        typography = AuroraTypography.material,
        content = content,
    )
}
