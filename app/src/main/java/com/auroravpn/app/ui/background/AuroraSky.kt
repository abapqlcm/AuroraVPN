package com.auroravpn.app.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (PI * 2).toFloat()

private data class Ribbon(
  val yFraction: Float,
  val thickness: Float,
  val amplitude: Float,
  val frequency: Float,
  val speed: Float,
  val phase: Float,
  val driftSpeed: Float,
  val driftPhase: Float,
  val color: Color,
)

private data class Star(
  val x: Float,
  val y: Float,
  val radius: Float,
  val baseAlpha: Float,
  val twinkleSpeed: Float,
  val twinklePhase: Float,
)

private const val LOOP_MS = 26_000

/**
 * The living background of the app: soft ribbons of light drifting across a
 * black sky, with a faint starfield and a vignette that pushes focus toward
 * the centre where the connect control lives.
 *
 * [intensity] lets the caller dim the whole field — the sky is calmer when the
 * tunnel is down and comes alive once it is up. That is the only input besides
 * the modifier; the motion is self-driving.
 */
@Composable
fun AuroraSky(
  modifier: Modifier = Modifier,
  intensity: Float = 1f,
) {
  val transition = rememberInfiniteTransition(label = "aurora")

  // A single master clock drives the ribbons; the starfield gets its own
  // slower one so the two layers do not lock together visually.
  val t by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(LOOP_MS, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "aurora-phase",
  )
  val twinkle by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(9_000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "twinkle",
  )

  // Seeded so the sky is identical on every recomposition and every device.
  val ribbons = remember { defaultRibbons() }
  val stars = remember { defaultStars() }

  Canvas(modifier) {
    drawRect(color = Color(0xFF050505))
    drawStars(stars, twinkle)
    drawRibbons(ribbons, t, intensity)
    drawVignette()
  }
}

private fun DrawScope.drawRibbons(ribbons: List<Ribbon>, t: Float, intensity: Float) {
  val w = size.width
  val h = size.height
  val segments = 64

  ribbons.forEach { ribbon ->
    // Slow vertical drift so the field breathes instead of repeating exactly.
    val yBase = h * ribbon.yFraction +
      sin(t * TAU * ribbon.driftSpeed + ribbon.driftPhase) * h * 0.05f
    val amplitude = h * ribbon.amplitude
    val halfThickness = h * ribbon.thickness
    val phaseShift = t * TAU * ribbon.speed + ribbon.phase

    val band = Path()
    // Top edge, left to right.
    for (i in 0..segments) {
      val x = w * i / segments
      val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift) - halfThickness
      if (i == 0) band.moveTo(x, y) else band.lineTo(x, y)
    }
    // Bottom edge, right to left, closing the band.
    for (i in segments downTo 0) {
      val x = w * i / segments
      val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift) + halfThickness
      band.lineTo(x, y)
    }
    band.close()

    // A filled band with hard edges would look like a flag. The gradient fades
    // the top and bottom to nothing so each ribbon reads as volumetric light.
    val alpha = (0.34f * intensity).coerceIn(0f, 1f)
    val brush = Brush.verticalGradient(
      colors = listOf(
        ribbon.color.copy(alpha = 0f),
        ribbon.color.copy(alpha = alpha),
        ribbon.color.copy(alpha = alpha * 0.55f),
        ribbon.color.copy(alpha = 0f),
      ),
      startY = yBase - halfThickness,
      endY = yBase + halfThickness,
    )
    // Screen blend is additive on black: where ribbons overlap they glow
    // brighter instead of just stacking opacity.
    drawPath(band, brush, blendMode = BlendMode.Screen)

    // A thin bright core gives each band a spine.
    val core = Path()
    for (i in 0..segments) {
      val x = w * i / segments
      val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift)
      if (i == 0) core.moveTo(x, y) else core.lineTo(x, y)
    }
    drawPath(
      core,
      color = ribbon.color.copy(alpha = 0.42f * intensity),
      style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
      blendMode = BlendMode.Screen,
    )
  }
}

private fun DrawScope.drawStars(stars: List<Star>, twinkle: Float) {
  stars.forEach { star ->
    val a = (star.baseAlpha * (0.55f + 0.45f * sin(twinkle * TAU * star.twinkleSpeed + star.twinklePhase)))
      .coerceIn(0f, 1f)
    drawCircle(
      color = Color(0xFFFFE873).copy(alpha = a),
      radius = star.radius,
      center = Offset(size.width * star.x, size.height * star.y),
    )
  }
}

private fun DrawScope.drawVignette() {
  drawRect(
    brush = Brush.radialGradient(
      colors = listOf(Color.Transparent, Color(0xFF050505)),
      center = Offset(size.width * 0.5f, size.height * 0.40f),
      radius = maxOf(size.width, size.height) * 0.78f,
    ),
  )
}

private fun defaultRibbons(): List<Ribbon> = listOf(
  Ribbon(0.15f, 0.085f, 0.055f, 1.6f, 0.55f, 0.0f, 0.30f, 0.5f, Color(0xFFFFD700)),
  Ribbon(0.31f, 0.060f, 0.080f, 2.2f, 0.40f, 1.7f, 0.22f, 2.1f, Color(0xFF34D1A6)),
  Ribbon(0.48f, 0.105f, 0.045f, 1.2f, 0.70f, 3.4f, 0.38f, 4.0f, Color(0xFFFFE873)),
  Ribbon(0.67f, 0.055f, 0.070f, 2.8f, 0.33f, 2.5f, 0.26f, 1.2f, Color(0xFF2FB8A8)),
  Ribbon(0.83f, 0.075f, 0.050f, 1.5f, 0.62f, 5.1f, 0.34f, 3.3f, Color(0xFFD9A40B)),
)

private fun defaultStars(): List<Star> {
  // Fixed seed: the sky is the same every time the app opens.
  val random = Random(seed = 20260918)
  return List(46) {
    Star(
      x = random.nextFloat(),
      y = random.nextFloat(),
      radius = random.nextFloat() * 1.4f + 0.5f,
      baseAlpha = random.nextFloat() * 0.5f + 0.15f,
      twinkleSpeed = random.nextFloat() * 0.8f + 0.2f,
      twinklePhase = random.nextFloat() * TAU,
    )
  }
}
