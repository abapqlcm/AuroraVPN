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
  /** Multiplier on this ribbon's own brightness; some bands carry more light. */
  val energy: Float,
)

private data class Star(
  val x: Float,
  val y: Float,
  val radius: Float,
  val baseAlpha: Float,
  val twinkleSpeed: Float,
  val twinklePhase: Float,
)

private const val LOOP_MS = 24_000

/**
 * The living background of the app: wide ribbons of light drifting across a
 * deep night sky, a faint starfield, a soft halo behind the connect control,
 * and a vignette that pushes focus to the centre.
 *
 * [intensity] is the single dial the home screen turns with the tunnel state —
 * the sky rests while the tunnel is down and comes alive once it is up.
 */
@Composable
fun AuroraSky(
  modifier: Modifier = Modifier,
  intensity: Float = 1f,
) {
  val transition = rememberInfiniteTransition(label = "aurora")

  // One master clock for the ribbons; the starfield gets its own slower one so
  // the two layers never lock together visually.
  val t by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(LOOP_MS, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "aurora-phase",
  )
  // A slow whole-field breathing, so even the idle sky is never a still frame.
  val breathe by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(7_000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "breathe",
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

  // Seeded: the sky is identical on every recomposition and every device.
  val ribbons = remember { defaultRibbons() }
  val stars = remember { defaultStars() }

  Canvas(modifier) {
    drawBaseGradient(breathe)
    drawHalo(intensity, breathe)
    drawStars(stars, twinkle)
    drawRibbons(ribbons, t, intensity, breathe)
    drawVignette()
  }
}

/**
 * The sky is never flat black. A vertical gradient gives it depth, and a slow
 * breathing on the top colour keeps the field alive while nothing is happening.
 */
private fun DrawScope.drawBaseGradient(breathe: Float) {
  val top = lerpColor(Color(0xFF0B0D16), Color(0xFF101320), 0.5f + 0.5f * sin(breathe * TAU))
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(top, Color(0xFF07070C), Color(0xFF050505)),
      startY = 0f,
      endY = size.height,
    ),
  )
}

/**
 * A large soft glow sitting behind the connect control. This is what makes the
 * centre of the screen feel lit rather than empty, and it brightens with the
 * tunnel — the visual payoff for connecting.
 */
private fun DrawScope.drawHalo(intensity: Float, breathe: Float) {
  val cx = size.width * 0.5f
  val cy = size.height * 0.42f
  val pulse = 0.82f + 0.18f * sin(breathe * TAU)
  val radius = size.width * 0.62f * pulse
  val a = (0.30f * intensity).coerceIn(0f, 1f)
  drawRect(
    brush = Brush.radialGradient(
      colors = listOf(
        Color(0xFFD9A40B).copy(alpha = a),
        Color(0xFF8A6D06).copy(alpha = a * 0.35f),
        Color.Transparent,
      ),
      center = Offset(cx, cy),
      radius = radius,
    ),
  )
}

/**
 * The ribbons. Compose has no blur primitive, so each band is drawn four times:
 * a wide faint pass, a medium pass, then the sharp core. Stacked, that reads as
 * volumetric light rather than a painted sine line.
 */
private fun DrawScope.drawRibbons(
  ribbons: List<Ribbon>,
  t: Float,
  intensity: Float,
  breathe: Float,
) {
  val w = size.width
  val h = size.height
  val segments = 72
  // The whole field swells and fades a little; keeps idle motion alive.
  val field = 0.86f + 0.14f * sin(breathe * TAU)

  ribbons.forEach { ribbon ->
    val yBase = h * ribbon.yFraction +
      sin(t * TAU * ribbon.driftSpeed + ribbon.driftPhase) * h * 0.06f
    val amplitude = h * ribbon.amplitude
    val halfThickness = h * ribbon.thickness
    val phaseShift = t * TAU * ribbon.speed + ribbon.phase
    val energy = ribbon.energy * intensity * field

    val band = buildBand(w, h, segments, yBase, amplitude, halfThickness, ribbon, phaseShift)
    val core = buildCore(w, h, segments, yBase, amplitude, ribbon, phaseShift)

    // Fake gaussian blur: wide and faint first, narrowing toward the bright
    // core. Screen blend means each pass adds light rather than covering it.
    val passes = listOf(
      StrokePass(thicknessMul = 3.4f, alphaMul = 0.10f),
      StrokePass(thicknessMul = 2.2f, alphaMul = 0.18f),
      StrokePass(thicknessMul = 1.4f, alphaMul = 0.32f),
    )
    passes.forEach { pass ->
      val alpha = (0.62f * energy * pass.alphaMul).coerceIn(0f, 1f)
      drawPath(
        band,
        brush = Brush.verticalGradient(
          colors = listOf(
            ribbon.color.copy(alpha = 0f),
            ribbon.color.copy(alpha = alpha),
            ribbon.color.copy(alpha = alpha * 0.7f),
            ribbon.color.copy(alpha = 0f),
          ),
          startY = yBase - halfThickness * pass.thicknessMul,
          endY = yBase + halfThickness * pass.thicknessMul,
        ),
        blendMode = BlendMode.Screen,
      )
    }

    // The bright spine.
    drawPath(
      core,
      color = ribbon.color.copy(alpha = (0.72f * energy).coerceIn(0f, 1f)),
      style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
      blendMode = BlendMode.Screen,
    )
  }
}

private fun buildBand(
  w: Float,
  h: Float,
  segments: Int,
  yBase: Float,
  amplitude: Float,
  halfThickness: Float,
  ribbon: Ribbon,
  phaseShift: Float,
): Path {
  val band = Path()
  for (i in 0..segments) {
    val x = w * i / segments
    val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift) - halfThickness
    if (i == 0) band.moveTo(x, y) else band.lineTo(x, y)
  }
  for (i in segments downTo 0) {
    val x = w * i / segments
    val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift) + halfThickness
    band.lineTo(x, y)
  }
  band.close()
  return band
}

private fun buildCore(
  w: Float,
  h: Float,
  segments: Int,
  yBase: Float,
  amplitude: Float,
  ribbon: Ribbon,
  phaseShift: Float,
): Path {
  val core = Path()
  for (i in 0..segments) {
    val x = w * i / segments
    val y = yBase + amplitude * sin(ribbon.frequency * TAU * x / w + phaseShift)
    if (i == 0) core.moveTo(x, y) else core.lineTo(x, y)
  }
  return core
}

private fun DrawScope.drawStars(stars: List<Star>, twinkle: Float) {
  stars.forEach { star ->
    val a = (star.baseAlpha * (0.5f + 0.5f * sin(twinkle * TAU * star.twinkleSpeed + star.twinklePhase)))
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
      center = Offset(size.width * 0.5f, size.height * 0.42f),
      radius = maxOf(size.width, size.height) * 0.85f,
    ),
  )
}

private data class StrokePass(val thicknessMul: Float, val alphaMul: Float)

private fun lerpColor(a: Color, b: Color, fraction: Float): Color {
  return Color(
    red = a.red + (b.red - a.red) * fraction,
    green = a.green + (b.green - a.green) * fraction,
    blue = a.blue + (b.blue - a.blue) * fraction,
    alpha = a.alpha + (b.alpha - a.alpha) * fraction,
  )
}

private fun defaultRibbons(): List<Ribbon> = listOf(
  Ribbon(0.14f, 0.095f, 0.055f, 1.6f, 0.55f, 0.0f, 0.30f, 0.5f, Color(0xFFFFD700), 1.00f),
  Ribbon(0.30f, 0.070f, 0.085f, 2.2f, 0.40f, 1.7f, 0.22f, 2.1f, Color(0xFF34D1A6), 0.80f),
  Ribbon(0.47f, 0.115f, 0.045f, 1.2f, 0.70f, 3.4f, 0.38f, 4.0f, Color(0xFFFFE873), 0.85f),
  Ribbon(0.66f, 0.060f, 0.075f, 2.8f, 0.33f, 2.5f, 0.26f, 1.2f, Color(0xFF2FB8A8), 0.65f),
  Ribbon(0.84f, 0.085f, 0.050f, 1.5f, 0.62f, 5.1f, 0.34f, 3.3f, Color(0xFFD9A40B), 0.75f),
)

private fun defaultStars(): List<Star> {
  // Fixed seed: the sky is the same every time the app opens.
  val random = Random(seed = 20260918)
  return List(54) {
    Star(
      x = random.nextFloat(),
      y = random.nextFloat(),
      radius = random.nextFloat() * 1.6f + 0.6f,
      baseAlpha = random.nextFloat() * 0.55f + 0.2f,
      twinkleSpeed = random.nextFloat() * 0.8f + 0.2f,
      twinklePhase = random.nextFloat() * TAU,
    )
  }
}
