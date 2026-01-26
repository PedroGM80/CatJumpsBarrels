package dev.pgm.game.audio

import kotlinx.browser.window
import org.w3c.dom.Audio
import kotlin.js.json
import kotlin.math.PI
import kotlin.math.sin

/**
 * Implementación JS del controlador de música usando Web Audio API.
 * Genera música procedural similar a la versión JVM.
 */
actual object MusicController {
    
    private var audioContext: dynamic = null
    private var isPlaying = false
    private var oscillators = mutableListOf<dynamic>()
    private var gainNodes = mutableListOf<dynamic>()
    private var nextNoteTime = 0.0
    private var currentNoteIndex = 0
    private var timerID: Int? = null
    
    // Tempo: 100 BPM
    private const val SIXTEENTH = 0.15
    private const val EIGHTH = 0.3
    private const val QUARTER = 0.6
    private const val HALF = 1.2
    
    // Notas (Hz) - Escala pentatónica
    private const val SILENCE = 0.0
    private const val C3 = 130.81
    private const val D3 = 146.83
    private const val E3 = 164.81
    private const val G3 = 196.0
    private const val A3 = 220.0
    private const val C4 = 261.63
    private const val D4 = 293.66
    private const val E4 = 329.63
    private const val G4 = 392.0
    private const val A4 = 440.0
    private const val C5 = 523.25
    private const val D5 = 587.33
    private const val E5 = 659.25
    private const val G5 = 783.99
    private const val A5 = 880.0
    
    // Volumen base
    private const val MELODY_VOLUME = 0.12
    private const val BASS_VOLUME = 0.08
    private const val PAD_VOLUME = 0.04
    
    private data class Note(val frequency: Double, val duration: Double)
    
    private val melodyNotes = listOf(
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
    
    private val bassNotes = listOf(
        Note(C3, QUARTER), Note(C3, QUARTER), Note(G3, QUARTER), Note(G3, QUARTER),
        Note(C3, QUARTER), Note(E3, QUARTER), Note(G3, QUARTER), Note(C3, QUARTER),
        Note(G3, QUARTER), Note(G3, QUARTER), Note(E3, QUARTER), Note(C3, QUARTER),
        Note(A3, QUARTER), Note(G3, QUARTER), Note(C3, HALF), Note(SILENCE, QUARTER),
        Note(C3, QUARTER), Note(C3, QUARTER), Note(G3, QUARTER), Note(G3, QUARTER),
        Note(A3, QUARTER), Note(G3, QUARTER), Note(E3, QUARTER), Note(C3, QUARTER),
        Note(SILENCE, QUARTER), Note(SILENCE, QUARTER)
    )
    
    private val padNotes = listOf(
        Note(E4, HALF), Note(E4, HALF), Note(G4, HALF), Note(G4, HALF),
        Note(E4, HALF), Note(G4, HALF), Note(E4, HALF), Note(E4, HALF),
        Note(G4, HALF), Note(E4, HALF), Note(E4, HALF), Note(G4, HALF),
        Note(E4, HALF), Note(D4, HALF), Note(C4, HALF), Note(SILENCE, QUARTER),
        Note(E4, HALF), Note(E4, HALF), Note(G4, HALF), Note(G4, HALF),
        Note(E4, HALF), Note(D4, HALF), Note(E4, HALF), Note(C4, HALF),
        Note(SILENCE, QUARTER), Note(SILENCE, QUARTER)
    )
    
    private fun ensureAudioContext() {
        if (audioContext == null) {
            audioContext = js("new (window.AudioContext || window.webkitAudioContext)()")
        }
        // Resume si está suspendido
        val state = audioContext.state as? String
        if (state == "suspended") {
            audioContext.resume()
        }
    }
    
    actual fun playMenuMusic() {
        if (isPlaying) return
        
        try {
            ensureAudioContext()
            isPlaying = true
            currentNoteIndex = 0
            nextNoteTime = audioContext.currentTime as Double
            
            // Iniciar el scheduler
            scheduler()
        } catch (e: Exception) {
            console.log("Error starting music: ${e.message}")
        }
    }
    
    actual fun stopMusic() {
        isPlaying = false
        
        // Cancelar el timer
        timerID?.let { window.clearTimeout(it) }
        timerID = null
        
        // Detener todos los oscilladores
        oscillators.forEach { osc ->
            try {
                osc.stop()
            } catch (e: Exception) {
                // Ignorar si ya está detenido
            }
        }
        oscillators.clear()
        gainNodes.clear()
    }
    
    private fun scheduler() {
        if (!isPlaying) return
        
        val currentTime = audioContext.currentTime as Double
        
        // Programar notas con anticipación (100ms lookahead)
        while (nextNoteTime < currentTime + 0.1) {
            scheduleNotes(nextNoteTime)
            advanceNote()
        }
        
        // Programar siguiente llamada
        timerID = window.setTimeout({ scheduler() }, 25)
    }
    
    private fun scheduleNotes(time: Double) {
        val melodyNote = melodyNotes[currentNoteIndex % melodyNotes.size]
        val bassIndex = (currentNoteIndex * bassNotes.size / melodyNotes.size) % bassNotes.size
        val padIndex = (currentNoteIndex * padNotes.size / melodyNotes.size) % padNotes.size
        
        val bassNote = bassNotes[bassIndex]
        val padNote = padNotes[padIndex]
        
        // Melodía - onda triangular
        if (melodyNote.frequency > 0) {
            playNote(melodyNote.frequency, time, melodyNote.duration * 0.9, MELODY_VOLUME, "triangle")
        }
        
        // Bajo - onda sinusoidal
        if (bassNote.frequency > 0) {
            playNote(bassNote.frequency, time, bassNote.duration * 0.8, BASS_VOLUME, "sine")
        }
        
        // Pad - onda sinusoidal suave
        if (padNote.frequency > 0) {
            playNote(padNote.frequency, time, padNote.duration * 0.95, PAD_VOLUME, "sine")
        }
    }
    
    private fun playNote(frequency: Double, startTime: Double, duration: Double, volume: Double, waveType: String) {
        try {
            val oscillator = audioContext.createOscillator()
            val gainNode = audioContext.createGain()
            
            oscillator.type = waveType
            oscillator.frequency.setValueAtTime(frequency, startTime)
            
            // Envelope ADSR suave
            val attackTime = 0.05
            val decayTime = 0.1
            val sustainLevel = 0.7
            val releaseTime = 0.1
            
            gainNode.gain.setValueAtTime(0.0, startTime)
            gainNode.gain.linearRampToValueAtTime(volume, startTime + attackTime)
            gainNode.gain.linearRampToValueAtTime(volume * sustainLevel, startTime + attackTime + decayTime)
            gainNode.gain.setValueAtTime(volume * sustainLevel, startTime + duration - releaseTime)
            gainNode.gain.linearRampToValueAtTime(0.0, startTime + duration)
            
            oscillator.connect(gainNode)
            gainNode.connect(audioContext.destination)
            
            oscillator.start(startTime)
            oscillator.stop(startTime + duration + 0.01)
            
            // Auto-limpieza
            oscillator.onended = {
                oscillators.remove(oscillator)
                gainNodes.remove(gainNode)
            }
            
            oscillators.add(oscillator)
            gainNodes.add(gainNode)
        } catch (e: Exception) {
            // Ignorar errores de audio
        }
    }
    
    private fun advanceNote() {
        val melodyNote = melodyNotes[currentNoteIndex % melodyNotes.size]
        nextNoteTime += melodyNote.duration
        currentNoteIndex++
        
        // Loop
        if (currentNoteIndex >= melodyNotes.size) {
            currentNoteIndex = 0
        }
    }
}
