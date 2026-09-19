package com.auroravpn.app.ui.design

import androidx.compose.ui.graphics.Color

/**
 * The Aurora colour system.
 *
 * Every screen draws from these and from nothing else: a raw hex literal in a
 * composable is a colour the theme cannot change and the contrast audit cannot
 * see.
 */
object AuroraColors {

    // ---- Surfaces, dark to light. ----

    /** The base the whole application floats on. */
    val Background: Color = Color(0xFF080D14)

    /** A step up from the base, for recessed areas. */
    val BackgroundSecondary: Color = Color(0xFF0B111A)

    /** The glass a card is made of. */
    val GlassSurface: Color = Color(0xFF0F1722)

    /** The hairline a glass card is edged with. */
    val GlassBorder: Color = Color(0xFF1D2C3F)

    // ---- Text. ----

    val TextPrimary: Color = Color(0xFFFFFFFF)
    val TextSecondary: Color = Color(0xFF7F92A7)
    val TextMuted: Color = Color(0xFF6C7D93)

    // ---- Accents. ----

    /** The mint that reads as "secure". */
    val AccentMint: Color = Color(0xFF3FF2C2)
    val BrightMint: Color = Color(0xFF6BFFE3)

    /** The blue the orbits and links are drawn in. */
    val Blue: Color = Color(0xFF1E6BFF)
    val BlueSecondary: Color = Color(0xFF2B82F6)

    // ---- States. ----

    /** Same family as the mint, for the "you are protected" reading. */
    val Secure: Color = Color(0xFF73FFD8)

    val Error: Color = Color(0xFFFF6B7A)
    val Warning: Color = Color(0xFFFFC46B)

    /**
     * Translucent layers used in place of blur, which Compose does not offer.
     * Stacked strokes of these read as frosted glass without a single raster
     * pass.
     */
    /**
     * Translucent layers used in place of blur, which Compose does not offer.
     * Stacked strokes of these read as frosted glass without a single raster
     * pass.
     */
    object Translucency {
        val GlassStrong: Color = Color(0xCC0F1722)
        val GlassMedium: Color = Color(0x990F1722)
        val GlassWeak: Color = Color(0x660F1722)
        val Glass: Color = GlassMedium

        val BorderStrong: Color = Color(0xCC1D2C3F)
        val BorderWeak: Color = Color(0x661D2C3F)
        val Border: Color = BorderWeak

        /** The faint hairline between two columns. */
        val Divider: Color = Color(0x331D2C3F)

        /** A mint wash, for the active state of something the user picked. */
        val Mint: Color = Color(0x333FF2C2)
    }

    /** Alias kept for readability where a surface is the stronger of two. */
    val GlassSurfaceStrong: Color = Color(0xFF121B29)
}
