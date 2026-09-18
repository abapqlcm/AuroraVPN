package com.auroravpn.app.ui.theme

import androidx.compose.ui.graphics.Color

// NETWORK ORBIT
// Deliberately desaturated. The spec is explicit about this: the app must
// remain mostly dark and neutral, and accent colour is a signal, not paint.
// Accent reads as "this thing is live"; everything else is structure.
internal val OrbitBackground = Color(0xFF080A0F)
internal val OrbitSurface = Color(0xFF0E1219)
internal val OrbitSurfaceElevated = Color(0xFF141923)

internal val OrbitTextPrimary = Color(0xFFF4F7FA)
internal val OrbitTextSecondary = Color(0xFF8B95A5)
internal val OrbitTextMuted = Color(0xFF566070)

internal val OrbitAccent = Color(0xFF7CFFB2)
internal val OrbitAccentDim = Color(0xFF4E9E77)
internal val OrbitAccentSecondary = Color(0xFF65A8FF)
internal val OrbitAccentSecondaryDim = Color(0xFF4A78B8)

internal val OrbitWarning = Color(0xFFFFCC66)
internal val OrbitError = Color(0xFFFF687C)

// Structure: hairlines and separators. Kept faint so they describe surfaces
// rather than drawing boxes around everything.
internal val OrbitHairline = Color(0xFF1F2632)
internal val OrbitHairlineStrong = Color(0xFF2A3240)

// Retained for the components that still reference the old gold identity until
// they migrate. Do not use in new code.
internal val Gold = OrbitAccentSecondary
internal val GoldDimmed = OrbitAccentSecondaryDim
internal val GoldSoft = Color(0xFF9FC2F0)
internal val GoldDeep = OrbitAccentSecondaryDim

internal val Onyx = OrbitBackground
internal val OnyxRaised = OrbitSurface
internal val OnyxCard = OrbitSurfaceElevated
internal val OnyxStroke = OrbitHairline

internal val AuroraGreen = OrbitAccent
internal val AuroraTeal = OrbitAccent
internal val AuroraRed = OrbitError
internal val AuroraAmber = OrbitWarning
