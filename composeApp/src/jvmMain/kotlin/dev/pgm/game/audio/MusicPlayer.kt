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
     * Genera una melodía tranquila para el menú principal.
     * Usa una progresión armónica suave con tempo lento.
     */
    private fun generateMenuMelody(): ByteArray {
        // Tempo: 90 BPM, cada nota negra = 667ms
        val eighthNote = 333  // Corchea
        val quarterNote = 667 // Negra
        val halfNote = 1334   // Blanca

        // Melodía principal - escala pentatónica para sonar más tranquila
        val melody = listOf(
            // Frase 1 - Subida suave
            Note(523.0, quarterNote),   // C5
            Note(587.0, eighthNote),    // D5
            Note(659.0, eighthNote),    // E5
            Note(784.0, quarterNote),   // G5
            Note(659.0, quarterNote),   // E5
            Note(587.0, halfNote),      // D5

            // Frase 2 - Respuesta
            Note(523.0, quarterNote),   // C5
            Note(392.0, eighthNote),    // G4
            Note(440.0, eighthNote),    // A4
            Note(523.0, quarterNote),   // C5
            Note(440.0, quarterNote),   // A4
            Note(392.0, halfNote),      // G4

            // Frase 3 - Variación
            Note(659.0, quarterNote),   // E5
            Note(587.0, eighthNote),    // D5
            Note(523.0, eighthNote),    // C5
            Note(587.0, quarterNote),   // D5
            Note(523.0, quarterNote),   // C5
            Note(392.0, halfNote),      // G4

            // Frase 4 - Resolución
            Note(523.0, quarterNote),   // C5
            Note(440.0, eighthNote),    // A4
            Note(523.0, eighthNote),    // C5
            Note(392.0, quarterNote),   // G4
            Note(330.0, quarterNote),   // E4
            Note(262.0, halfNote + quarterNote),  // C4 - nota larga para cerrar
        )

        // Bajo acompañamiento - notas más graves, más espaciadas
        val bass = listOf(
            // Acompaña cada frase con las fundamentales
            Note(262.0, halfNote),      // C4
            Note(294.0, halfNote),      // D4
            Note(196.0, halfNote),      // G3
            Note(220.0, halfNote),      // A3

            Note(262.0, halfNote),      // C4
            Note(196.0, halfNote),      // G3
            Note(220.0, halfNote),      // A3
            Note(262.0, halfNote),      // C4

            Note(330.0, halfNote),      // E4
            Note(294.0, halfNote),      // D4
            Note(196.0, halfNote),      // G3
            Note(262.0, halfNote),      // C4

            Note(262.0, halfNote),      // C4
            Note(220.0, halfNote),      // A3
            Note(196.0, halfNote),      // G3
            Note(262.0, halfNote + quarterNote),  // C4
        )

        // Mezclar melodía y bajo (el bajo a menor volumen)
        return mixTracks(
            generateTrack(melody, volume = 25),
            generateTrack(bass, volume = 15)
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

        for (i in 0 until numSamples) {
            val time = i / SAMPLE_RATE
            // Onda cuadrada
            val value = if (sin(2.0 * PI * frequency * time) >= 0) 1.0 else -1.0

            // Envelope ADSR simple para suavizar el inicio y final
            val attackTime = 0.05  // 50ms
            val releaseTime = 0.1  // 100ms
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
