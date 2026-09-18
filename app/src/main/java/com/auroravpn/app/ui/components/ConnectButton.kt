package com.auroravpn.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.auroravpn.app.vpn.VpnState
import com.auroravpn.app.vpn.isBusy
import com.auroravpn.app.vpn.isProtecting
import kotlin.math.PI
import kotlin.math.sin

private const val TAU = (PI * 2).toFloat()

/**
 * The main connect control: a ring whose state is the whole story.
 *
 * Idle shows a hollow ring and the brand mark. Busy shows an indeterminate
 * sweep that never resolves — a determinate bar here would be a lie, since the
 * handshake takes however long it takes. Connected shows a complete ring with a
 * soft glow, because "you are safe now" should not look the same as "wait".
 *
 * The ring is drawn rather than animated with a progress composable so the
 * colours, the arc gaps and the halo are all in one place and all tuneable.
 */
@Composable
fun ConnectButton(
  state: VpnState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val measurer = rememberTextMeasurer()
  val infinite = rememberInfiniteTransition(label = "ring")

  // The indeterminate sweep length. Pulsing the arc's own size (rather than
  // only rotating it) is what makes it read as working rather than as a
  // spinner stuck on a fixed percentage.
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
  // Connected gets a gentle pulse — the only ambient motion in the connected
  // state, kept slow so it reads as calm rather than demanding attention.
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

  val label = when (state) {
    VpnState.IDLE -> "اتصال"
    VpnState.REQUESTING -> "درخواست"
    VpnState.CONNECTING -> "در حال اتصال"
    VpnState.CONNECTED -> "متصل"
    VpnState.DISCONNECTING -> "قطع ارتباط"
    VpnState.ERROR -> "خطا"
  }

  Box(
    modifier = modifier
      .size(216.dp)
      .semantics { role = Role.Button }
      .progressSemantics(if (state.isProtecting) 1f else 0f)
      .clickable(enabled = !state.isBusy, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val stroke = 10.dp.toPx()
      val diameter = size.minDimension - stroke
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
        state.isProtecting -> {
          // Complete ring plus a halo that breathes with [pulse].
          val glow = 0.30f + 0.20f * sin(pulse * TAU)
          drawArc(
            color = gold.copy(alpha = glow),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 2.4f, cap = StrokeCap.Round),
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

        state.isBusy -> {
          // An open arc that rotates. The gap is what makes it indeterminate:
          // a full rotating ring would look like progress pretending to move.
          val start = -90f + spin * 360f
          val sweepAngle = 110f + 50f * sin(sweep * TAU)
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

        state == VpnState.ERROR -> {
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
          // IDLE: hollow ring, brand mark. The invitation to tap.
          drawArc(
            color = gold.copy(alpha = 0.55f),
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
  drawLine(color, Offset(cx - s, cy + s * 0.2f), Offset(cx - s * 0.2f, cy + s), 5.dp.toPx(), StrokeCap.Round)
  drawLine(color, Offset(cx - s * 0.2f, cy + s), Offset(cx + s, cy - s * 0.8f), 5.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawCross(
  topLeft: Offset,
  size: Size,
  color: Color,
) {
  val cx = topLeft.x + size.width / 2f
  val cy = topLeft.y + size.height / 2f
  val s = size.width * 0.18f
  drawLine(color, Offset(cx - s, cy - s), Offset(cx + s, cy + s), 5.dp.toPx(), StrokeCap.Round)
  drawLine(color, Offset(cx + s, cy - s), Offset(cx - s, cy + s), 5.dp.toPx(), StrokeCap.Round)
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
  drawPath(p, color)
}

/**
 * Draws the action label inside the ring, horizontally centred and sitting in
 * the lower third so it clears the centre glyph above it.
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
    fontSize = 15.sp,
    fontWeight = FontWeight.SemiBold,
  )
  val result = measurer.measure(
    AnnotatedString(text),
    style,
    constraints = Constraints(maxWidth = diameter.toInt()),
  )
  val x = topLeft.x + (size.width - result.size.width) / 2f
  val y = topLeft.y + size.height * 0.60f
  drawText(result, topLeft = Offset(x, y))
}

private val DrawScope.minDimension: Float
  get() = minOf(size.width, size.height)

private fun Size.minDimension(): Float = minOf(width, height)
