package dev.pgm.game.audio

import dev.pgm.game.data.preferences.GamePreferences
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.*
import kotlin.math.PI
import kotlin.math.sin

object MusicPlayer {

    // =========================================================
    // AUDIO CONFIG
    // =========================================================

    private const val SAMPLE_RATE = 44100
    private const val CHANNELS = 1
    private const val BIT_DEPTH = 16
    private const val BYTES_PER_SAMPLE = BIT_DEPTH / 8

    private const val SIGNED = true
    private const val BIG_ENDIAN = false

    // =========================================================
    // ENVELOPE
    // =========================================================

    private const val ATTACK_TIME_SEC = 0.05
    private const val RELEASE_TIME_SEC = 0.10

    // =========================================================
    // VOLUME
    // =========================================================

    private const val BASE_VOLUME_MAIN = 8000
    private const val BASE_VOLUME_BASS = 5000

    // =========================================================
    // TEMPO (120 BPM)
    // =========================================================

    private const val SIXTEENTH = 125
    private const val EIGHTH = 250
    private const val QUARTER = 500
    private const val HALF = 1000

    // =========================================================
    // NOTES (Hz)
    // =========================================================

    private const val SILENCE = 0.0

    private const val G3 = 196.0
    private const val A3 = 220.0
    private const val C4 = 262.0
    private const val F3 = 175.0

    private const val C5 = 523.0
    private const val D5 = 587.0
    private const val E5 = 659.0
    private const val F5 = 698.0
    private const val G5 = 784.0
    private const val A5 = 880.0
    private const val C6 = 1047.0

    // =========================================================
    // STATE
    // =========================================================

    private val playing = AtomicBoolean(false)
    private var thread: Thread? = null

    private val FORMAT = AudioFormat(
        SAMPLE_RATE.toFloat(),
        BIT_DEPTH,
        CHANNELS,
        SIGNED,
        BIG_ENDIAN
    )

    private val songBuffer: ByteArray by lazy { generateMenuSong() }

    // =========================================================
    // PUBLIC API
    // =========================================================

    fun playMenuMusic() {
        if (!GamePreferences.musicEnabled || playing.get()) return

        playing.set(true)

        thread = Thread {
            val line = AudioSystem.getSourceDataLine(FORMAT)
            line.open(FORMAT)
            line.start()

            try {
                while (playing.get() && GamePreferences.musicEnabled) {
                    line.write(songBuffer, 0, songBuffer.size)
                }
            } finally {
                line.drain()
                line.close()
                playing.set(false)
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    fun stopMusic() {
        playing.set(false)
    }

    // =========================================================
    // SONG GENERATION
    // =========================================================

    private fun generateMenuSong(): ByteArray {
        val volumeFactor = GamePreferences.getNormalizedMusicVolume()

        val volumeMain = (BASE_VOLUME_MAIN * volumeFactor).toInt()
        val volumeBass = (BASE_VOLUME_BASS * volumeFactor).toInt()

        val melody = melodyNotes()
        val bass = bassNotes()

        val totalSamples = melody.sumOf { samplesFor(it.durationMs) }
        val buffer = ByteArray(totalSamples * BYTES_PER_SAMPLE)

        var offset = 0
        melody.zip(bass) { m, b ->
            val samples = samplesFor(m.durationMs)

            mixNote(buffer, offset, samples, m.frequency, volumeMain)
            mixNote(buffer, offset, samples, b.frequency, volumeBass)

            offset += samples * BYTES_PER_SAMPLE
        }

        return buffer
    }

    private fun mixNote(
        buffer: ByteArray,
        offset: Int,
        samples: Int,
        frequency: Double,
        volume: Int
    ) {
        if (frequency == SILENCE) return

        val attackSamples = (SAMPLE_RATE * ATTACK_TIME_SEC).toInt()
        val releaseSamples = (SAMPLE_RATE * RELEASE_TIME_SEC).toInt()

        for (i in 0 until samples) {
            val time = i.toDouble() / SAMPLE_RATE
            val wave = if (sin(2.0 * PI * frequency * time) >= 0) 1.0 else -1.0

            val envelope = when {
                i < attackSamples ->
                    i.toDouble() / attackSamples

                i > samples - releaseSamples ->
                    (samples - i).toDouble() / releaseSamples

                else -> 1.0
            }

            val sample = (wave * volume * envelope).toInt()
            val index = offset + i * BYTES_PER_SAMPLE

            val current =
                (buffer[index + 1].toInt() shl 8) or
                        (buffer[index].toInt() and 0xFF)

            val mixed = (current + sample)
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

            buffer[index] = (mixed and 0xFF).toByte()
            buffer[index + 1] = (mixed shr 8).toByte()
        }
    }

    private fun samplesFor(durationMs: Int): Int =
        SAMPLE_RATE * durationMs / 1000

    // =========================================================
    // MELODY DEFINITIONS (SIN NÚMEROS MÁGICOS)
    // =========================================================

    private fun melodyNotes() = listOf(
        Note(C5, EIGHTH), Note(E5, EIGHTH), Note(G5, EIGHTH), Note(E5, EIGHTH),
        Note(C5, EIGHTH), Note(E5, EIGHTH), Note(G5, QUARTER), Note(SILENCE, EIGHTH),

        Note(C5, EIGHTH), Note(E5, EIGHTH), Note(G5, EIGHTH), Note(A5, EIGHTH),
        Note(G5, EIGHTH), Note(E5, EIGHTH), Note(C5, QUARTER), Note(SILENCE, EIGHTH),

        Note(G3, EIGHTH), Note(C5, EIGHTH), Note(E5, EIGHTH), Note(G5, EIGHTH),
        Note(C6, EIGHTH), Note(G5, EIGHTH), Note(C5, HALF), Note(SILENCE, QUARTER)
    )

    private fun bassNotes() = listOf(
        Note(C4, EIGHTH), Note(SILENCE, SIXTEENTH), Note(C4, SIXTEENTH),
        Note(G3, EIGHTH), Note(SILENCE, SIXTEENTH), Note(G3, SIXTEENTH),
        Note(C4, EIGHTH), Note(SILENCE, EIGHTH),

        Note(F3, EIGHTH), Note(SILENCE, SIXTEENTH), Note(F3, SIXTEENTH),
        Note(G3, EIGHTH), Note(A3, EIGHTH),
        Note(C4, QUARTER), Note(SILENCE, EIGHTH),

        Note(G3, EIGHTH), Note(C4, EIGHTH), Note(G3, EIGHTH), Note(C4, EIGHTH),
        Note(C4, EIGHTH), Note(SILENCE, SIXTEENTH), Note(C4, SIXTEENTH),
        Note(C4, HALF), Note(SILENCE, QUARTER)
    )

    // =========================================================
    // DATA
    // =========================================================

    private data class Note(
        val frequency: Double,
        val durationMs: Int
    )
}
