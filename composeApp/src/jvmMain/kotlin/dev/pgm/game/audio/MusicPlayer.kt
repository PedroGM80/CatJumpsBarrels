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
        // Tempo: 75 BPM (más lento), cada nota negra = 800ms
        val eighthNote = 400  // Corchea
        val quarterNote = 800 // Negra
        val halfNote = 1600   // Blanca
        val wholeNote = 3200  // Redonda

        // Melodía principal - progresión más armoniosa y espaciada
        val melody = listOf(
            // Frase 1 - Introducción tranquila (I - V - vi - IV)
            Note(523.0, halfNote),      // C5
            Note(0.0, quarterNote),     // Silencio
            Note(659.0, halfNote),      // E5
            Note(0.0, quarterNote),     // Silencio
            Note(587.0, halfNote),      // D5
            Note(523.0, quarterNote),   // C5
            Note(392.0, wholeNote),     // G4 - resolución larga

            // Frase 2 - Desarrollo (vi - IV - I - V)
            Note(440.0, halfNote),      // A4
            Note(0.0, quarterNote),     // Silencio
            Note(523.0, halfNote),      // C5
            Note(587.0, quarterNote),   // D5
            Note(523.0, halfNote),      // C5
            Note(0.0, quarterNote),     // Silencio
            Note(392.0, wholeNote),     // G4

            // Frase 3 - Clímax suave (I - iii - IV - V)
            Note(523.0, quarterNote),   // C5
            Note(587.0, quarterNote),   // D5
            Note(659.0, halfNote),      // E5
            Note(0.0, quarterNote),     // Silencio
            Note(587.0, halfNote),      // D5
            Note(523.0, quarterNote),   // C5
            Note(440.0, wholeNote),     // A4

            // Frase 4 - Resolución final (IV - V - I)
            Note(523.0, quarterNote),   // C5
            Note(440.0, quarterNote),   // A4
            Note(392.0, halfNote),      // G4
            Note(0.0, quarterNote),     // Silencio
            Note(330.0, halfNote),      // E4
            Note(262.0, wholeNote),     // C4 - cierre en tónica
        )

        // Bajo acompañamiento - fundamental de cada acorde, muy suave
        val bass = listOf(
            // Acorde C (I)
            Note(262.0, wholeNote),     // C4
            Note(0.0, quarterNote),
            // Acorde G (V)
            Note(196.0, wholeNote),     // G3
            Note(0.0, quarterNote),
            // Acorde Am (vi)
            Note(220.0, wholeNote),     // A3

            // Acorde F (IV)
            Note(175.0, wholeNote),     // F3
            Note(0.0, quarterNote),
            // Acorde C (I)
            Note(262.0, wholeNote),     // C4
            Note(0.0, quarterNote),
            // Acorde G (V)
            Note(196.0, wholeNote),     // G3

            // Acorde C (I)
            Note(262.0, wholeNote),     // C4
            Note(0.0, quarterNote),
            // Acorde Em (iii)
            Note(165.0, wholeNote),     // E3
            Note(220.0, wholeNote),     // A3

            // Acorde F (IV)
            Note(175.0, wholeNote),     // F3
            // Acorde G (V)
            Note(196.0, halfNote),      // G3
            // Acorde C (I) - final
            Note(262.0, wholeNote),     // C4
        )

        // Mezclar melodía y bajo con volúmenes mucho más bajos
        return mixTracks(
            generateTrack(melody, volume = 12),  // Melodía muy suave
            generateTrack(bass, volume = 8)      // Bajo casi imperceptible
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
