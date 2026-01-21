package dev.pgm.game.audio

import javax.sound.sampled.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generador de sonidos retro estilo 8-bit usando síntesis de audio.
 * Genera ondas cuadradas y barridos de frecuencia sin archivos WAV.
 */
object RetroSoundGenerator {

    private const val SAMPLE_RATE = 44100f
    private val audioFormat = AudioFormat(SAMPLE_RATE, 8, 1, true, false)

    private var soundEnabled = true

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    /**
     * Sonido de salto - barrido ascendente rápido
     */
    fun playJump() {
        if (!soundEnabled) return
        playAsync {
            generateFrequencySweep(
                startFreq = 200.0,
                endFreq = 600.0,
                durationMs = 100,
                waveType = WaveType.SQUARE
            )
        }
    }

    /**
     * Sonido de puntuación - dos tonos alegres
     */
    fun playScore() {
        if (!soundEnabled) return
        playAsync {
            val tone1 = generateTone(880.0, 50, WaveType.SQUARE)
            val tone2 = generateTone(1100.0, 80, WaveType.SQUARE)
            tone1 + tone2
        }
    }

    /**
     * Sonido de muerte - barrido descendente largo
     */
    fun playDeath() {
        if (!soundEnabled) return
        playAsync {
            generateFrequencySweep(
                startFreq = 800.0,
                endFreq = 100.0,
                durationMs = 400,
                waveType = WaveType.SQUARE
            )
        }
    }

    /**
     * Sonido de victoria - melodía ascendente
     */
    fun playWin() {
        if (!soundEnabled) return
        playAsync {
            val notes = listOf(
                generateTone(523.0, 100, WaveType.SQUARE),  // C5
                generateTone(659.0, 100, WaveType.SQUARE),  // E5
                generateTone(784.0, 100, WaveType.SQUARE),  // G5
                generateTone(1047.0, 200, WaveType.SQUARE)  // C6
            )
            notes.reduce { acc, bytes -> acc + bytes }
        }
    }

    /**
     * Sonido de pausa - tono corto
     */
    fun playPause() {
        if (!soundEnabled) return
        playAsync {
            generateTone(440.0, 50, WaveType.SQUARE)
        }
    }

    /**
     * Sonido de selección en menú
     */
    fun playMenuSelect() {
        if (!soundEnabled) return
        playAsync {
            generateTone(660.0, 30, WaveType.SQUARE)
        }
    }

    /**
     * Sonido de confirmación en menú
     */
    fun playMenuConfirm() {
        if (!soundEnabled) return
        playAsync {
            val tone1 = generateTone(440.0, 50, WaveType.SQUARE)
            val tone2 = generateTone(880.0, 100, WaveType.SQUARE)
            tone1 + tone2
        }
    }

    /**
     * Sonido de barril lanzado
     */
    fun playBarrelThrow() {
        if (!soundEnabled) return
        playAsync {
            generateFrequencySweep(
                startFreq = 300.0,
                endFreq = 150.0,
                durationMs = 80,
                waveType = WaveType.SQUARE
            )
        }
    }

    /**
     * Jingle de intro del menú principal - melodía retro corta
     */
    fun playMenuIntro() {
        if (!soundEnabled) return
        playAsync {
            val notes = listOf(
                generateTone(330.0, 80, WaveType.SQUARE),   // E4
                generateTone(392.0, 80, WaveType.SQUARE),   // G4
                generateTone(523.0, 80, WaveType.SQUARE),   // C5
                generateTone(659.0, 150, WaveType.SQUARE)   // E5
            )
            notes.reduce { acc, bytes -> acc + bytes }
        }
    }

    private fun playAsync(generator: () -> ByteArray) {
        Thread {
            try {
                val samples = generator()
                playBuffer(samples)
            } catch (e: Exception) {
                // Silently ignore audio errors
            }
        }.start()
    }

    private fun playBuffer(samples: ByteArray) {
        try {
            val line = AudioSystem.getSourceDataLine(audioFormat)
            line.open(audioFormat)
            line.start()
            line.write(samples, 0, samples.size)
            line.drain()
            line.close()
        } catch (e: Exception) {
            // Audio not available
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, waveType: WaveType): ByteArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val samples = ByteArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i / SAMPLE_RATE
            val value = when (waveType) {
                WaveType.SINE -> sin(2.0 * PI * frequency * time)
                WaveType.SQUARE -> if (sin(2.0 * PI * frequency * time) >= 0) 1.0 else -1.0
            }
            // Reducir volumen y aplicar envelope simple
            val envelope = if (i < numSamples / 10) i.toDouble() / (numSamples / 10)
                          else if (i > numSamples * 9 / 10) (numSamples - i).toDouble() / (numSamples / 10)
                          else 1.0
            samples[i] = (value * 40 * envelope).toInt().toByte()
        }

        return samples
    }

    private fun generateFrequencySweep(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        waveType: WaveType
    ): ByteArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val samples = ByteArray(numSamples)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val frequency = startFreq + (endFreq - startFreq) * progress

            val value = when (waveType) {
                WaveType.SINE -> sin(phase)
                WaveType.SQUARE -> if (sin(phase) >= 0) 1.0 else -1.0
            }

            // Envelope para evitar clicks
            val envelope = if (i < numSamples / 10) i.toDouble() / (numSamples / 10)
                          else if (i > numSamples * 9 / 10) (numSamples - i).toDouble() / (numSamples / 10)
                          else 1.0

            samples[i] = (value * 40 * envelope).toInt().toByte()
            phase += 2.0 * PI * frequency / SAMPLE_RATE
        }

        return samples
    }

    private enum class WaveType {
        SINE,
        SQUARE
    }
}
