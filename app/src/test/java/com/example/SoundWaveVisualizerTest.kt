package com.example

import com.example.ui.components.SoundWaveStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sin

class SoundWaveVisualizerTest {

    @Test
    fun testSoundWaveStyles() {
        val styles = SoundWaveStyle.values()
        assertEquals(3, styles.size)
        assertTrue(styles.contains(SoundWaveStyle.BARS))
        assertTrue(styles.contains(SoundWaveStyle.FLUID_WAVE))
        assertTrue(styles.contains(SoundWaveStyle.DUAL))
    }

    @Test
    fun testNormalizedAudioInputScaling() {
        // Sound level from SpeechRecognizer onRmsChanged is typically clamped between 0f and 1f
        val silentLevel = 0.0f
        val normalSpeech = 0.45f
        val peakSpeech = 1.0f

        val minHeight = 4f
        val maxHeight = 60f

        val midIndex = 14f
        for (i in 0 until 28) {
            val centerDist = abs(i - midIndex) / midIndex
            val bellWeight = (1.0f - (centerDist * centerDist * 0.7f)).coerceIn(0.25f, 1.0f)

            // When silent, level is minimal
            val silentAmp = minHeight + (maxHeight - minHeight) * (silentLevel * bellWeight).coerceIn(0f, 1f)
            assertEquals(minHeight, silentAmp, 0.01f)

            // When speaking normally, amplitude scales smoothly
            val normalAmp = minHeight + (maxHeight - minHeight) * (normalSpeech * bellWeight).coerceIn(0f, 1f)
            assertTrue("Normal speech amp should exceed minimum", normalAmp > minHeight)
            assertTrue("Normal speech amp should not exceed max", normalAmp <= maxHeight)

            // Center bars should have higher amplitude than edge bars
            if (i == 14) {
                val edgeDist = abs(0 - midIndex) / midIndex
                val edgeWeight = (1.0f - (edgeDist * edgeDist * 0.7f)).coerceIn(0.25f, 1.0f)
                val edgeAmp = minHeight + (maxHeight - minHeight) * (normalSpeech * edgeWeight).coerceIn(0f, 1f)
                assertTrue("Center bar amp should exceed edge bar amp", normalAmp > edgeAmp)
            }
        }
    }
}
