package com.auroravpn.app.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CornerSize

/**
 * The Aurora type scale.
 *
 * Sizes follow the design specification, and every style carries an explicit
 * line height so a label cannot be crowded by the value above it. Letter
 * spacing is used the way the wordmark uses it: wide on small caps, normal on
 * body text.
 */
object AuroraTypography {

    /** A U R O R A */
    val Brand: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 6.sp,
        lineHeight = 22.sp,
        color = AuroraColors.TextPrimary,
    )

    /** NETWORK ORBIT */
    val BrandSubtitle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 3.sp,
        lineHeight = 14.sp,
        color = AuroraColors.TextMuted,
    )

    /** SECURE */
    val StatusLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 2.sp,
        lineHeight = 28.sp,
    )

    /** VPN Ready, in the top bar capsule. */
    val StatusPill: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 16.sp,
    )

    /** An endpoint or transport name. */
    val Endpoint: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp,
        lineHeight = 18.sp,
        color = AuroraColors.TextSecondary,
    )

    /** A metric value: 12 ms */
    val MetricValue: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = AuroraColors.TextPrimary,
    )

    /** A metric label: Latency */
    val MetricLabel: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 14.sp,
        color = AuroraColors.TextMuted,
    )

    /** A card title. */
    val CardTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 18.sp,
        color = AuroraColors.TextPrimary,
    )

    /** A technical reading: 127.0.0.1:1819 */
    val Technical: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = AuroraColors.TextMuted,
    )

    /** Bottom navigation labels. */
    val Navigation: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    )

    val Body: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = AuroraColors.TextSecondary,
    )

    val BodySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = AuroraColors.TextSecondary,
    )

    val Button: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 18.sp,
    )

    val ScreenTitle: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 22.sp,
        color = AuroraColors.TextPrimary,
    )

    val EndpointAddress: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontFamily = FontFamily.Monospace,
        color = AuroraColors.TextPrimary,
    )

    /**
     * The same styles, as a Material [Typography].
     *
     * Screens draw from [AuroraTypography] directly -- a style fetched here is
     * one the palette cannot recolour, and the contrast audit cannot find. This
     * exists so the small number of Material components still in use pick up
     * the same sizes as everything around them.
     */
    val material: Typography = Typography(
        headlineSmall = ScreenTitle,
        titleMedium = CardTitle,
        titleSmall = CardTitle,
        bodyLarge = Body,
        bodyMedium = Body,
        bodySmall = BodySmall,
        labelLarge = Button,
        labelMedium = MetricLabel,
        labelSmall = MetricLabel,
    )
}

/**
 * Radii. Kept as [CornerSize] values so a shape can be built from them without
 * a recomposition allocating a new one.
 */
object AuroraShapes {
    val Card = RoundedCornerShape(16.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(50)
    val Node = RoundedCornerShape(50)
}
