package dev.pgm.game.audio

import javax.sound.sampled.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Reproductor de música de fondo estilo 8-bit.
 * Compone melodías usando síntesis de ondas cuadradas.
 */
object MusicPlayer {

    private const val SAMPLE_RATE = 44100f
    private val audioFormat = AudioFormat(SAMPLE_RATE, 8, 1, true, false)

    private var musicThread: Thread? = null
    @Volatile private var isPlaying = false
    @Volatile private var musicEnabled = true

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            stopMusic()
        }
    }

    /**
     * Inicia la música del menú principal en loop
     */
    fun playMenuMusic() {
        if (!musicEnabled || isPlaying) return

        isPlaying = true
        musicThread = Thread {
            try {
                val line = AudioSystem.getSourceDataLine(audioFormat)
                line.open(audioFormat)
                line.start()

                while (isPlaying && musicEnabled) {
                    val melody = generateMenuMelody()
                    line.write(melody, 0, melody.size)
                }

                line.drain()
                line.close()
            } catch (e: Exception) {
                // Audio not available
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    /**
     * Detiene la música
     */
    fun stopMusic() {
        isPlaying = false
        musicThread?.interrupt()
        musicThread = null
    }

    /**
     * Genera una melodía pegadiza estilo 8-bit NES para el menú principal.
     * Inspirada en clásicos como Super Mario Bros y Zelda.
     */
    private fun generateMenuMelody(): ByteArray {
        // Tempo: 120 BPM (más alegre), cada nota negra = 500ms
        val sixteenthNote = 125  // Semicorchea
        val eighthNote = 250     // Corchea
        val quarterNote = 500    // Negra
        val halfNote = 1000      // Blanca

        // Melodía principal - patrón pegadizo estilo NES con arpegios
        val melody = listOf(
            // Motivo principal A - patrón alegre y saltarín
            Note(523.0, eighthNote),    // C5
            Note(659.0, eighthNote),    // E5
            Note(784.0, eighthNote),    // G5
            Note(659.0, eighthNote),    // E5
            Note(523.0, eighthNote),    // C5
            Note(659.0, eighthNote),    // E5
            Note(784.0, quarterNote),   // G5
            Note(0.0, eighthNote),      // Silencio

            // Repetición del motivo con variación
            Note(523.0, eighthNote),    // C5
            Note(659.0, eighthNote),    // E5
            Note(784.0, eighthNote),    // G5
            Note(880.0, eighthNote),    // A5
            Note(784.0, eighthNote),    // G5
            Note(659.0, eighthNote),    // E5
            Note(523.0, quarterNote),   // C5
            Note(0.0, eighthNote),      // Silencio

            // Motivo B - respuesta melódica
            Note(587.0, eighthNote),    // D5
            Note(659.0, eighthNote),    // E5
            Note(698.0, eighthNote),    // F5
            Note(784.0, eighthNote),    // G5
            Note(659.0, eighthNote),    // E5
            Note(587.0, eighthNote),    // D5
            Note(523.0, quarterNote),   // C5
            Note(0.0, eighthNote),      // Silencio

            // Cierre con salto octava (estilo Super Mario)
            Note(392.0, eighthNote),    // G4
            Note(523.0, eighthNote),    // C5
            Note(659.0, eighthNote),    // E5
            Note(784.0, eighthNote),    // G5
            Note(1047.0, eighthNote),   // C6 - salto de octava
            Note(784.0, eighthNote),    // G5
            Note(523.0, halfNote),      // C5 - resolución
            Note(0.0, quarterNote),     // Silencio
        )

        // Bajo estilo NES - patrón de walking bass con ritmo marcado
        val bass = listOf(
            // Patrón I - V (típico de 8-bit)
            Note(262.0, eighthNote),    // C4
            Note(0.0, sixteenthNote),
            Note(262.0, sixteenthNote), // C4
            Note(196.0, eighthNote),    // G3
            Note(0.0, sixteenthNote),
            Note(196.0, sixteenthNote), // G3
            Note(262.0, eighthNote),    // C4
            Note(0.0, eighthNote),

            Note(262.0, eighthNote),    // C4
            Note(0.0, sixteenthNote),
            Note(262.0, sixteenthNote), // C4
            Note(196.0, eighthNote),    // G3
            Note(220.0, eighthNote),    // A3
            Note(262.0, quarterNote),   // C4
            Note(0.0, eighthNote),

            // Patrón IV - V - I
            Note(175.0, eighthNote),    // F3
            Note(0.0, sixteenthNote),
            Note(175.0, sixteenthNote), // F3
            Note(196.0, eighthNote),    // G3
            Note(0.0, sixteenthNote),
            Note(196.0, sixteenthNote), // G3
            Note(262.0, quarterNote),   // C4
            Note(0.0, eighthNote),

            // Cierre con patrón de tónica
            Note(196.0, eighthNote),    // G3
            Note(262.0, eighthNote),    // C4
            Note(196.0, eighthNote),    // G3
            Note(262.0, eighthNote),    // C4
            Note(262.0, eighthNote),    // C4
            Note(0.0, sixteenthNote),
            Note(262.0, sixteenthNote), // C4
            Note(262.0, halfNote),      // C4 - final
            Note(0.0, quarterNote),
        )

        // Mezclar melodía y bajo con volúmenes muy bajos para fondo sutil
        return mixTracks(
            generateTrack(melody, volume = 8),   // Melodía más bajita
            generateTrack(bass, volume = 5)      // Bajo muy sutil
        )
    }

    private fun generateTrack(notes: List<Note>, volume: Int): ByteArray {
        val allSamples = mutableListOf<Byte>()

        for (note in notes) {
            val samples = generateNote(note.frequency, note.durationMs, volume)
            allSamples.addAll(samples.toList())
        }

        return allSamples.toByteArray()
    }

    private fun generateNote(frequency: Double, durationMs: Int, volume: Int): ByteArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val samples = ByteArray(numSamples)

        // Si la frecuencia es 0, generar silencio
        if (frequency == 0.0) {
            return samples // Array de ceros = silencio
        }

        for (i in 0 until numSamples) {
            val time = i / SAMPLE_RATE
            // Onda cuadrada
            val value = if (sin(2.0 * PI * frequency * time) >= 0) 1.0 else -1.0

            // Envelope ADSR más suave para música de fondo
            val attackTime = 0.08  // 80ms - ataque más suave
            val releaseTime = 0.15  // 150ms - release más largo
            val timeInSeconds = i / SAMPLE_RATE
            val durationSeconds = durationMs / 1000.0

            val envelope = when {
                timeInSeconds < attackTime -> timeInSeconds / attackTime
                timeInSeconds > durationSeconds - releaseTime ->
                    (durationSeconds - timeInSeconds) / releaseTime
                else -> 1.0
            }

            samples[i] = (value * volume * envelope).toInt().toByte()
        }

        return samples
    }

    private fun mixTracks(track1: ByteArray, track2: ByteArray): ByteArray {
        val maxLength = maxOf(track1.size, track2.size)
        val mixed = ByteArray(maxLength)

        for (i in 0 until maxLength) {
            val sample1 = if (i < track1.size) track1[i].toInt() else 0
            val sample2 = if (i < track2.size) track2[i].toInt() else 0

            // Mezclar y limitar para evitar clipping
            val mixedSample = (sample1 + sample2) / 2
            mixed[i] = mixedSample.coerceIn(-127, 127).toByte()
        }

        return mixed
    }

    private data class Note(
        val frequency: Double,
        val durationMs: Int
    )
}
