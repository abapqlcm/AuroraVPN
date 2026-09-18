package com.auroravpn.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// AuroraVPN is Persian-first. The default Material type scale ships with a
// Latin system family whose Persian glyphs fall back to whatever the device
// happens to have, which is rarely centred or weighted correctly. Asking for
// the device's default family explicitly lets the platform pick the right
// Persian face instead.
private val Default = FontFamily.Default

private val Display = TextStyle(
  fontFamily = Default,
  fontWeight = FontWeight.Bold,
  fontSize = 34.sp,
  letterSpacing = 0.5.sp,
)

private val Headline = TextStyle(
  fontFamily = Default,
  fontWeight = FontWeight.SemiBold,
  fontSize = 22.sp,
  letterSpacing = 0.3.sp,
)

private val Title = TextStyle(
  fontFamily = Default,
  fontWeight = FontWeight.SemiBold,
  fontSize = 18.sp,
  letterSpacing = 0.2.sp,
)

private val Body = TextStyle(
  fontFamily = Default,
  fontWeight = FontWeight.Normal,
  fontSize = 15.sp,
  lineHeight = 22.sp,
  letterSpacing = 0.2.sp,
)

private val Label = TextStyle(
  fontFamily = Default,
  fontWeight = FontWeight.Medium,
  fontSize = 13.sp,
  letterSpacing = 0.4.sp,
)

internal val AuroraTypography = Typography(
  displayLarge = Display,
  displayMedium = Display.copy(fontSize = 28.sp),
  displaySmall = Display.copy(fontSize = 24.sp),
  headlineLarge = Headline,
  headlineMedium = Headline,
  headlineSmall = Headline.copy(fontSize = 19.sp),
  titleLarge = Title,
  titleMedium = Title.copy(fontSize = 16.sp),
  titleSmall = Title.copy(fontSize = 14.sp),
  bodyLarge = Body,
  bodyMedium = Body.copy(fontSize = 14.sp),
  bodySmall = Body.copy(fontSize = 12.sp, lineHeight = 18.sp),
  labelLarge = Label,
  labelMedium = Label.copy(fontSize = 12.sp),
  labelSmall = Label.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
)
