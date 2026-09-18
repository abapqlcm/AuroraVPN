package com.auroravpn.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auroravpn.app.vpn.VpnStatus
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting
import kotlin.math.PI
import kotlin.math.sin

private const val TAU = (PI * 2).toFloat()

/**
 * The main connect control: a drawn ring whose state is the whole story.
 *
 * Each state is drawn rather than animated with a progress composable, so the
 * colours, arc gaps, glow layers and centre glyph all live in one place.
 *
 * - IDLE      hollow gold ring, diamond mark — the invitation to tap
 * - busy      an indeterminate arc that rotates and breathes; a determinate
 *             bar here would lie about a handshake of unknown length
 * - CONNECTED a complete ring drawn in three glow layers so it reads as light
 *             rather than as a painted circle, with a slow pulse
 * - ERROR     red ring, cross mark
 *
 * A press scales the whole ring down slightly — the control has to feel like
 * something under the finger, not a static image.
 */
@Composable
fun ConnectButton(
  status: VpnStatus,
  enabled: Boolean = true,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val measurer = rememberTextMeasurer()
  val infinite = rememberInfiniteTransition(label = "ring")
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()

  // The indeterminate arc's own length. Pulsing the arc's size — not only
  // rotating it — is what makes it read as working rather than as a fixed
  // percentage stuck on a spinner.
  val sweep by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1_400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "sweep",
  )
  // A slower rotation on top, so the arc does not retrace the same path.
  val spin by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3_600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "spin",
  )
  // Connected ambient pulse: the only motion once up, kept slow on purpose.
  val pulse by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2_800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse",
  )

  val gold = MaterialTheme.colorScheme.primary
  val goldSoft = MaterialTheme.colorScheme.secondary
  val track = MaterialTheme.colorScheme.outline
  val onSurface = MaterialTheme.colorScheme.onSurface
  val errorColor = MaterialTheme.colorScheme.error

  val label = when (status) {
    VpnStatus.Idle -> "اتصال"
    VpnStatus.Preparing -> "آماده‌سازی"
    is VpnStatus.Connecting -> "در حال اتصال"
    is VpnStatus.Connected -> "متصل"
    VpnStatus.Disconnecting -> "قطع ارتباط"
    is VpnStatus.Error -> "خطا"
    VpnStatus.PermissionRequired -> "اجازه"
    is VpnStatus.Reconnecting -> "اتصال مجدد"
  }

  // Pressed feedback: a small, immediate scale-down.
  val scale = if (pressed) 0.94f else 1f

  Box(
    modifier = modifier
      .size(236.dp)
      .semantics { role = Role.Button }
      .progressSemantics(
        if (status.isProtecting) 1f
        else if (status.isBusy) 0f
        else 0f,
      )
      .clickable(
        interactionSource = interaction,
        indication = null,
        enabled = !status.isBusy,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val stroke = 12.dp.toPx() * scale
      val diameter = (size.minDimension - stroke * 1.4f) * scale
      val topLeft = Offset(
        (size.width - diameter) / 2f,
        (size.height - diameter) / 2f,
      )
      val arcSize = Size(diameter, diameter)

      // Faint full ring, always present, as the track.
      drawArc(
        color = track,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
      )

      when {
        status.isProtecting -> {
          // Three glow layers, widest and faintest first: this is what makes a
          // drawn ring read as emitted light instead of a painted circle.
          val breathe = 0.5f + 0.5f * sin(pulse * TAU)
          drawArc(
            color = gold.copy(alpha = 0.16f + 0.10f * breathe),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 3.2f, cap = StrokeCap.Round),
          )
          drawArc(
            color = gold.copy(alpha = 0.34f + 0.18f * breathe),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 2.0f, cap = StrokeCap.Round),
          )
          drawArc(
            color = gold,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
          )
          drawCheck(topLeft, arcSize, gold)
        }

        status.isBusy -> {
          // A rotating open arc. The gap is what carries "indeterminate": a
          // full rotating ring would read as progress pretending to move.
          val start = -90f + spin * 360f
          val sweepAngle = 110f + 60f * sin(sweep * TAU)
          // Outer faint layer first for a trail of light behind the arc.
          drawArc(
            color = goldSoft.copy(alpha = 0.25f),
            startAngle = start,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round),
          )
          drawArc(
            color = goldSoft,
            startAngle = start,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
          )
        }

        status is VpnStatus.Error -> {
          drawArc(
            color = errorColor.copy(alpha = 0.30f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round),
          )
          drawArc(
            color = errorColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
          )
          drawCross(topLeft, arcSize, errorColor)
        }

        else -> {
          // IDLE. Slightly brighter than the track so it reads as tappable.
          drawArc(
            color = gold.copy(alpha = 0.14f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 2.4f, cap = StrokeCap.Round),
          )
          drawArc(
            color = gold.copy(alpha = 0.75f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
          )
          drawDiamond(topLeft, arcSize, gold)
        }
      }

      drawCenteredLabel(measurer, label, onSurface, topLeft, arcSize, diameter)
    }
  }
}

private fun DrawScope.drawCheck(
  topLeft: Offset,
  size: Size,
  color: Color,
) {
  val cx = topLeft.x + size.width / 2f
  val cy = topLeft.y + size.height / 2f
  val s = size.width * 0.20f
  drawLine(color, Offset(cx - s, cy + s * 0.2f), Offset(cx - s * 0.2f, cy + s), 6.dp.toPx(), StrokeCap.Round)
  drawLine(color, Offset(cx - s * 0.2f, cy + s), Offset(cx + s, cy - s * 0.8f), 6.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawCross(
  topLeft: Offset,
  size: Size,
  color: Color,
) {
  val cx = topLeft.x + size.width / 2f
  val cy = topLeft.y + size.height / 2f
  val s = size.width * 0.18f
  drawLine(color, Offset(cx - s, cy - s), Offset(cx + s, cy + s), 6.dp.toPx(), StrokeCap.Round)
  drawLine(color, Offset(cx + s, cy - s), Offset(cx - s, cy + s), 6.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawDiamond(
  topLeft: Offset,
  size: Size,
  color: Color,
) {
  val cx = topLeft.x + size.width / 2f
  val cy = topLeft.y + size.height / 2f
  val s = size.width * 0.16f
  val p = Path().apply {
    moveTo(cx, cy - s)
    lineTo(cx + s * 0.72f, cy)
    lineTo(cx, cy + s)
    lineTo(cx - s * 0.72f, cy)
    close()
  }
  // A faint backing glow plus the solid shape gives the mark some weight.
  drawPath(p, color.copy(alpha = 0.25f), style = Stroke(width = 8.dp.toPx()))
  drawPath(p, color)
}

/**
 * Draws the action label inside the ring, horizontally centred and sitting low
 * enough to clear the centre glyph above it.
 */
private fun DrawScope.drawCenteredLabel(
  measurer: TextMeasurer,
  text: String,
  color: Color,
  topLeft: Offset,
  size: Size,
  diameter: Float,
) {
  val style = TextStyle(
    color = color,
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
  )
  val result = measurer.measure(
    AnnotatedString(text),
    style,
    constraints = Constraints(maxWidth = diameter.toInt()),
  )
  val x = topLeft.x + (size.width - result.size.width) / 2f
  val y = topLeft.y + size.height * 0.58f
  drawText(result, topLeft = Offset(x, y))
}

private val DrawScope.minDimension: Float
  get() = minOf(size.width, size.height)
