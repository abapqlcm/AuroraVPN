package com.auroravpn.app.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Spacing and sizing. One set of numbers for every screen, so two cards never
 * sit at different distances and a thumb never lands on a target smaller than
 * the platform asks for.
 */
object AuroraDimensions {
    val screenMargin = 16.dp
    val screenMarginLarge = 20.dp
    val cardGap = 12.dp
    val cardGapLarge = 16.dp
    val cardPadding = 16.dp
    val touchTarget = 44.dp
    val pillHeight = 28.dp
    val sectionGap = 24.dp

    /** The height of the bottom navigation bar, content padding below it. */
    val bottomBarHeight = 64.dp

    /** Bottom bar plus its inset: the clearance content needs to clear it. */
    val bottomBarClearance = bottomBarHeight + 12.dp

    /** The vertical rhythm of the Home screen. */
    val homeGap = 18.dp
}

/**
 * The glass card every surface shares.
 *
 * Not a Material Card: those carry an elevation shadow, and a shadow on a dark
 * interface is a black blur on black. This is a translucent fill with a hairline
 * border, which is how the depth is read instead.
 */
@Composable
fun AuroraGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(AuroraShapes.Card)
            .background(AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, AuroraColors.Translucency.Border)),
    ) {
        content()
    }
}

@Composable
fun AuroraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tint = if (enabled) AuroraColors.AccentMint else AuroraColors.TextMuted
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(AuroraShapes.Pill)
            .background(if (enabled) AuroraColors.Translucency.Mint else AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, if (enabled) AuroraColors.AccentMint.copy(alpha = 0.6f) else AuroraColors.GlassBorder), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AuroraTypography.Button,
            color = if (enabled) Color.Black else AuroraColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AuroraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(AuroraShapes.Pill)
            .background(AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, AuroraColors.GlassBorder), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AuroraTypography.Button,
            color = if (enabled) AuroraColors.TextPrimary else AuroraColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AuroraStatusPill(label: String, dotColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 28.dp)
            .clip(AuroraShapes.Pill)
            .background(AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, AuroraColors.Translucency.Border), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(dotColor))
        Text(
            text = label,
            style = AuroraTypography.MetricLabel,
            color = AuroraColors.TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
fun AuroraMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = AuroraTypography.MetricValue,
            color = AuroraColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = AuroraTypography.MetricLabel,
            color = AuroraColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AuroraMetricDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 1.dp, height = 32.dp)
            .background(AuroraColors.Translucency.Divider),
    )
}

@Composable
fun AuroraSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AuroraTypography.MetricLabel,
        color = AuroraColors.TextMuted,
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
        modifier = modifier.padding(start = 4.dp, bottom = 0.dp),
    )
}

@Composable
fun AuroraChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) AuroraColors.BrightMint else AuroraColors.TextSecondary
    Box(
        modifier = modifier
            .clip(AuroraShapes.Pill)
            .background(if (selected) AuroraColors.Translucency.Mint else AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, if (selected) AuroraColors.AccentMint.copy(alpha = 0.7f) else AuroraColors.GlassBorder), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AuroraTypography.Button,
            // A washed mint wash with a mint text is a low-contrast smear; a
            // bright mint on the dark glass beneath it is the readable pairing.
            color = if (selected) AuroraColors.BrightMint else tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AuroraRadioButton(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, if (selected) AuroraColors.AccentMint else AuroraColors.GlassBorder), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AuroraColors.AccentMint),
            )
        }
    }
}

@Composable
fun AuroraSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val width = 44.dp
    val height = 24.dp
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(AuroraShapes.Pill)
            .background(if (checked) AuroraColors.Translucency.Mint else AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, if (checked) AuroraColors.AccentMint.copy(alpha = 0.7f) else AuroraColors.GlassBorder), CircleShape)
            .clickable { onCheckedChange(!checked) },
    ) {
        val align = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        Box(
            modifier = Modifier
                .align(align)
                .padding(2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(if (checked) AuroraColors.AccentMint else AuroraColors.TextMuted),
        )
    }
}

@Composable
fun AuroraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(
        modifier = modifier
            .clip(AuroraShapes.CardSmall)
            .background(AuroraColors.Translucency.Glass)
            .border(BorderStroke(1.dp, AuroraColors.Translucency.Border))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = AuroraTypography.MetricLabel,
            color = AuroraColors.TextMuted,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AuroraTypography.Endpoint,
            cursorBrush = Brush.verticalGradient(listOf(AuroraColors.AccentMint, AuroraColors.AccentMint)),
            keyboardOptions = keyboardType,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AuroraLoadingState(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(BorderStroke(2.dp, AuroraColors.Translucency.Divider), CircleShape),
        )
        Text(text = label, style = AuroraTypography.Body, color = AuroraColors.TextMuted)
    }
}
