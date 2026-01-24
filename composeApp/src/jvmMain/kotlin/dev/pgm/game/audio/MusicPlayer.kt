package dev.pgm.game.audio

import dev.pgm.game.data.preferences.GamePreferences
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.*
import kotlin.math.PI
import kotlin.math.abs
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
    // ENVELOPE (más suave)
    // =========================================================

    private const val ATTACK_TIME_SEC = 0.08
    private const val DECAY_TIME_SEC = 0.1
    private const val SUSTAIN_LEVEL = 0.7
    private const val RELEASE_TIME_SEC = 0.15

    // =========================================================
    // VOLUME
    // =========================================================

    private const val BASE_VOLUME_MAIN = 6000
    private const val BASE_VOLUME_BASS = 4000
    private const val BASE_VOLUME_PAD = 2000

    // =========================================================
    // TEMPO (100 BPM - más relajado)
    // =========================================================

    private const val SIXTEENTH = 150
    private const val EIGHTH = 300
    private const val QUARTER = 600
    private const val HALF = 1200
    private const val WHOLE = 2400

    // =========================================================
    // NOTES (Hz) - Escala pentatónica mayor (suena agradable)
    // =========================================================

    private const val SILENCE = 0.0

    // Octava 3
    private const val C3 = 130.81
    private const val D3 = 146.83
    private const val E3 = 164.81
    private const val G3 = 196.0
    private const val A3 = 220.0

    // Octava 4
    private const val C4 = 261.63
    private const val D4 = 293.66
    private const val E4 = 329.63
    private const val G4 = 392.0
    private const val A4 = 440.0

    // Octava 5
    private const val C5 = 523.25
    private const val D5 = 587.33
    private const val E5 = 659.25
    private const val G5 = 783.99
    private const val A5 = 880.0

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
    // WAVE TYPES
    // =========================================================

    private enum class WaveType {
        SINE,       // Suave y pura
        TRIANGLE,   // Suave con algo de carácter
        SOFT_SAW    // Sierra suavizada para pads
    }

    private fun generateWave(waveType: WaveType, phase: Double): Double {
        return when (waveType) {
            WaveType.SINE -> sin(phase)
            
            WaveType.TRIANGLE -> {
                val normalized = (phase % (2 * PI)) / (2 * PI)
                if (normalized < 0.5) {
                    4.0 * normalized - 1.0
                } else {
                    3.0 - 4.0 * normalized
                }
            }
            
            WaveType.SOFT_SAW -> {
                // Sierra suavizada con armónicos limitados
                val normalized = (phase % (2 * PI)) / PI - 1.0
                sin(phase) * 0.6 + normalized * 0.3 + sin(phase * 2) * 0.1
            }
        }
    }

    // =========================================================
    // SONG GENERATION
    // =========================================================

    private fun generateMenuSong(): ByteArray {
        val volumeFactor = GamePreferences.getNormalizedMusicVolume()

        val volumeMain = (BASE_VOLUME_MAIN * volumeFactor).toInt()
        val volumeBass = (BASE_VOLUME_BASS * volumeFactor).toInt()
        val volumePad = (BASE_VOLUME_PAD * volumeFactor).toInt()

        val melody = melodyNotes()
        val bass = bassNotes()
        val pad = padNotes()

        val totalSamples = melody.sumOf { samplesFor(it.durationMs) }
        val buffer = ByteArray(totalSamples * BYTES_PER_SAMPLE)

        var offset = 0
        for (i in melody.indices) {
            val m = melody[i]
            val b = bass.getOrNull(i) ?: Note(SILENCE, m.durationMs)
            val p = pad.getOrNull(i) ?: Note(SILENCE, m.durationMs)

            val samples = samplesFor(m.durationMs)

            // Melodía con onda triangular (suave pero con carácter)
            mixNote(buffer, offset, samples, m.frequency, volumeMain, WaveType.TRIANGLE)
            
            // Bajo con sinusoide pura (profundo y limpio)
            mixNote(buffer, offset, samples, b.frequency, volumeBass, WaveType.SINE)
            
            // Pad con sierra suave (ambiental)
            mixNote(buffer, offset, samples, p.frequency, volumePad, WaveType.SOFT_SAW)

            offset += samples * BYTES_PER_SAMPLE
        }

        return buffer
    }

    private fun mixNote(
        buffer: ByteArray,
        offset: Int,
        samples: Int,
        frequency: Double,
        volume: Int,
        waveType: WaveType = WaveType.SINE
    ) {
        if (frequency == SILENCE) return

        val attackSamples = (SAMPLE_RATE * ATTACK_TIME_SEC).toInt()
        val decaySamples = (SAMPLE_RATE * DECAY_TIME_SEC).toInt()
        val releaseSamples = (SAMPLE_RATE * RELEASE_TIME_SEC).toInt()
        val sustainStart = attackSamples + decaySamples
        val releaseStart = samples - releaseSamples

        for (i in 0 until samples) {
            val phase = 2.0 * PI * frequency * i / SAMPLE_RATE
            
            // Onda principal con armónico suave
            val wave = generateWave(waveType, phase) * 0.8 + 
                       generateWave(WaveType.SINE, phase * 2) * 0.15 +
                       generateWave(WaveType.SINE, phase * 3) * 0.05

            // Envelope ADSR suave
            val envelope = when {
                i < attackSamples -> {
                    // Attack con curva suave
                    val t = i.toDouble() / attackSamples
                    t * t * (3 - 2 * t) // Smoothstep
                }
                i < sustainStart -> {
                    // Decay hacia sustain
                    val t = (i - attackSamples).toDouble() / decaySamples
                    1.0 - (1.0 - SUSTAIN_LEVEL) * t
                }
                i > releaseStart -> {
                    // Release con curva suave
                    val t = (samples - i).toDouble() / releaseSamples
                    SUSTAIN_LEVEL * t * t
                }
                else -> SUSTAIN_LEVEL
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
    // MELODY - Tema alegre y juguetón estilo plataformas
    // =========================================================

    private fun melodyNotes() = listOf(
        // Frase 1: Intro ascendente
        Note(G4, EIGHTH), Note(A4, EIGHTH), Note(C5, QUARTER),
        Note(E5, EIGHTH), Note(D5, EIGHTH), Note(C5, QUARTER),
        
        // Frase 2: Juguetona
        Note(G5, EIGHTH), Note(E5, EIGHTH), Note(G5, EIGHTH), Note(A5, EIGHTH),
        Note(G5, QUARTER), Note(E5, QUARTER),
        
        // Frase 3: Respuesta melódica
        Note(D5, EIGHTH), Note(E5, EIGHTH), Note(G5, QUARTER),
        Note(E5, EIGHTH), Note(D5, EIGHTH), Note(C5, QUARTER),
        
        // Frase 4: Resolución
        Note(A4, EIGHTH), Note(C5, EIGHTH), Note(D5, EIGHTH), Note(E5, EIGHTH),
        Note(C5, HALF), Note(SILENCE, QUARTER),
        
        // Frase 5: Variación
        Note(E5, EIGHTH), Note(G5, EIGHTH), Note(A5, QUARTER),
        Note(G5, EIGHTH), Note(E5, EIGHTH), Note(D5, QUARTER),
        
        // Frase 6: Cierre del loop
        Note(C5, EIGHTH), Note(D5, EIGHTH), Note(E5, EIGHTH), Note(G5, EIGHTH),
        Note(E5, QUARTER), Note(C5, QUARTER),
        
        // Descanso
        Note(SILENCE, QUARTER), Note(SILENCE, QUARTER)
    )

    private fun bassNotes() = listOf(
        // Frase 1
        Note(C3, QUARTER), Note(C3, QUARTER),
        Note(G3, QUARTER), Note(G3, QUARTER),
        
        // Frase 2
        Note(C3, QUARTER), Note(E3, QUARTER),
        Note(G3, QUARTER), Note(C3, QUARTER),
        
        // Frase 3
        Note(G3, QUARTER), Note(G3, QUARTER),
        Note(E3, QUARTER), Note(C3, QUARTER),
        
        // Frase 4
        Note(A3, QUARTER), Note(G3, QUARTER),
        Note(C3, HALF), Note(SILENCE, QUARTER),
        
        // Frase 5
        Note(C3, QUARTER), Note(C3, QUARTER),
        Note(G3, QUARTER), Note(G3, QUARTER),
        
        // Frase 6
        Note(A3, QUARTER), Note(G3, QUARTER),
        Note(E3, QUARTER), Note(C3, QUARTER),
        
        // Descanso
        Note(SILENCE, QUARTER), Note(SILENCE, QUARTER)
    )

    private fun padNotes() = listOf(
        // Acordes sostenidos de fondo (C mayor)
        Note(E4, HALF), Note(E4, HALF),
        Note(G4, HALF), Note(G4, HALF),
        
        Note(E4, HALF), Note(G4, HALF),
        Note(E4, HALF), Note(E4, HALF),
        
        Note(G4, HALF), Note(E4, HALF),
        Note(E4, HALF), Note(G4, HALF),
        
        Note(E4, HALF), Note(D4, HALF),
        Note(C4, HALF), Note(SILENCE, QUARTER),
        
        Note(E4, HALF), Note(E4, HALF),
        Note(G4, HALF), Note(G4, HALF),
        
        Note(E4, HALF), Note(D4, HALF),
        Note(E4, HALF), Note(C4, HALF),
        
        Note(SILENCE, QUARTER), Note(SILENCE, QUARTER)
    )

    // =========================================================
    // DATA
    // =========================================================

    private data class Note(
        val frequency: Double,
        val durationMs: Int
    )
}
