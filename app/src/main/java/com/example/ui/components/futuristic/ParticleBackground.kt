package com.example.ui.components.futuristic

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.FuturisticTheme
import java.util.Random
import kotlin.math.sin

/**
 * Precomputed, immutable specification for a single background particle.
 * Kept memory-compact with zero allocations during animation ticks.
 */
private class ParticleSpec(
    val normX: Float,
    val normY: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val alphaVariance: Float,
    val speedX: Float,
    val speedY: Float,
    val freqAlpha: Float,
    val phaseX: Float,
    val phaseY: Float,
    val phaseAlpha: Float
)

/**
 * Generates deterministic particle specs with pseudorandom seed.
 * Count scales gracefully between 150 (compact mobile) and 400 (tablets / foldables).
 */
private fun generateParticles(count: Int): List<ParticleSpec> {
    val random = Random(0x50415254L) // Deterministic seed ("PART")
    val list = ArrayList<ParticleSpec>(count)

    for (i in 0 until count) {
        val normX = random.nextFloat()
        val normY = random.nextFloat()
        // Radius between 0.5dp and 2.0dp
        val radiusDp = 0.5f + random.nextFloat() * 1.5f
        // Very low opacity: 0.04 to 0.16 (never degrades text readability)
        val baseAlpha = 0.04f + random.nextFloat() * 0.12f
        val alphaVariance = 0.02f + random.nextFloat() * 0.05f

        // Extremely slow drift speeds (in normalized screen space / second)
        val speedX = (random.nextFloat() - 0.5f) * 0.012f
        val speedY = (random.nextFloat() - 0.5f) * 0.016f

        // Independent, incommensurate frequencies so particles don't repeat in an obvious loop
        val freqAlpha = 0.25f + random.nextFloat() * 0.65f
        val phaseX = random.nextFloat() * 6.283185f
        val phaseY = random.nextFloat() * 6.283185f
        val phaseAlpha = random.nextFloat() * 6.283185f

        list.add(
            ParticleSpec(
                normX = normX,
                normY = normY,
                radiusDp = radiusDp,
                baseAlpha = baseAlpha,
                alphaVariance = alphaVariance,
                speedX = speedX,
                speedY = speedY,
                freqAlpha = freqAlpha,
                phaseX = phaseX,
                phaseY = phaseY,
                phaseAlpha = phaseAlpha
            )
        )
    }
    return list
}

/**
 * Reusable Android Compose ParticleBackground component.
 *
 * Requirements fulfilled:
 * - Full-screen background with near-white background (#FCFDFF or theme background).
 * - 150–400 tiny soft blue particles dynamically scaled based on screen size (width/height).
 * - Random but deterministic positions (fixed seed, no random shifts on recomposition).
 * - Particle radius around 0.5–2.0dp.
 * - Very low opacity (0.03 – 0.20 max), ensuring foreground text readability is never compromised.
 * - Slight natural variation in opacity and size.
 * - Extremely slow, serene movement.
 * - Gentle individual fade in/out per particle with no obvious looping patterns.
 * - No heavy blur, no large glowing circles, no distracting animations.
 *
 * Performance optimizations:
 * - Single Canvas with DrawScope primitives (zero canvas/path object allocations per frame).
 * - Single Frame ticker (withFrameNanos) driving animation state; NO coroutines per particle.
 * - Automatically pauses ticking when the app goes to Lifecycle ON_PAUSE / ON_STOP.
 * - Respects system reduced-motion accessibility preference (freezes movement if animations disabled).
 * - Voice reactive hook: optionally modulates particle energy slightly during voice speech.
 */
@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleColor: Color = FuturisticTheme.particleDot,
    backgroundColor: Color = FuturisticTheme.background,
    soundLevel: Float = 0f,
    isVoiceActive: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check system animation duration scale / reduced motion preference
    val isReducedMotion = remember(context) {
        try {
            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            animatorScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // Lifecycle state awareness: pause ticker when backgrounded
    var isAppVisible by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isAppVisible = event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Single unified elapsed time accumulator (in seconds)
    var elapsedTimeSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isAppVisible, isReducedMotion) {
        if (!isAppVisible || isReducedMotion) return@LaunchedEffect
        var lastNanoTime = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastNanoTime != 0L) {
                    val dt = (frameTimeNanos - lastNanoTime) / 1_000_000_000f
                    // Cap delta time to prevent large jumps when returning from background
                    elapsedTimeSec += dt.coerceIn(0f, 0.05f)
                }
                lastNanoTime = frameTimeNanos
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("futuristic_particle_background")
    ) {
        val widthDp = maxWidth
        val heightDp = maxHeight

        // Scale particle count between 150 and 400 depending on screen size
        val particleCount = remember(widthDp, heightDp) {
            val areaFactor = ((widthDp.value * heightDp.value) / (380f * 700f)).coerceIn(0.8f, 2.5f)
            (160 * areaFactor).toInt().coerceIn(150, 400)
        }

        val particles = remember(particleCount) {
            generateParticles(particleCount)
        }

        // Voice reactivity multiplier (smoothed and capped very low to avoid distraction)
        val voiceEnergy = if (isVoiceActive) (soundLevel.coerceIn(0f, 1f) * 0.15f) else 0f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val t = elapsedTimeSec

            val particleColorR = particleColor.red
            val particleColorG = particleColor.green
            val particleColorB = particleColor.blue

            val count = particles.size
            for (i in 0 until count) {
                val p = particles[i]

                // Slow, non-repeating gentle sinusoidal drift in normalized space
                val dx = sin(t * 0.12f * (p.speedX * 1000f) + p.phaseX) * 0.025f
                val dy = sin(t * 0.09f * (p.speedY * 1000f) + p.phaseY) * 0.035f

                // Wrap-around normalized coordinates
                var xNorm = (p.normX + dx) % 1.0f
                if (xNorm < 0f) xNorm += 1.0f

                var yNorm = (p.normY + dy) % 1.0f
                if (yNorm < 0f) yNorm += 1.0f

                val xPos = xNorm * canvasW
                val yPos = yNorm * canvasH

                // Independent gentle breathing alpha oscillation per particle
                val alphaWave = sin(t * p.freqAlpha + p.phaseAlpha) * p.alphaVariance
                val dynamicAlpha = (p.baseAlpha + alphaWave + voiceEnergy).coerceIn(0.025f, 0.22f)

                // Sub-pixel to 2dp radius
                val radiusPx = (p.radiusDp + (voiceEnergy * 0.8f)) * density

                drawCircle(
                    color = Color(
                        red = particleColorR,
                        green = particleColorG,
                        blue = particleColorB,
                        alpha = dynamicAlpha
                    ),
                    radius = radiusPx,
                    center = Offset(xPos, yPos)
                )
            }
        }

        // Foreground content strictly positioned above the particle layer
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
