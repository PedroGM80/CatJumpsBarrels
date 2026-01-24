package dev.pgm.game.audio

import kotlinx.browser.window
import org.w3c.dom.AudioContext

/**
 * Implementación Web del controlador de música usando Web Audio API.
 * Genera música procedural similar a la versión JVM.
 */
actual object MusicController {
    
    private var audioContext: AudioContext? = null
    private var isPlaying = false
    private var oscillators = mutableListOf<dynamic>()
    private var gainNodes = mutableListOf<dynamic>()
    private var intervalId: Int? = null
    
    private fun getAudioContext(): AudioContext? {
        if (audioContext == null) {
            audioContext = try {
                AudioContext()
            } catch (e: Exception) {
                null
            }
        }
        return audioContext
    }
    
    actual fun playMenuMusic() {
        if (isPlaying) return
        isPlaying = true
        
        val ctx = getAudioContext() ?: return
        
        // Crear un loop simple de música
        startMusicLoop(ctx)
    }
    
    actual fun stopMusic() {
        isPlaying = false
        
        intervalId?.let { window.clearInterval(it) }
        intervalId = null
        
        oscillators.forEach { osc ->
            try {
                osc.stop()
            } catch (e: Exception) {}
        }
        oscillators.clear()
        gainNodes.clear()
    }
    
    private fun startMusicLoop(ctx: AudioContext) {
        // Melodía simple que se repite
        val melody = listOf(
            Pair(392.0, 300),  // G4
            Pair(440.0, 300),  // A4
            Pair(523.0, 600),  // C5
            Pair(659.0, 300),  // E5
            Pair(587.0, 300),  // D5
            Pair(523.0, 600),  // C5
            Pair(784.0, 300),  // G5
            Pair(659.0, 300),  // E5
            Pair(784.0, 300),  // G5
            Pair(880.0, 300),  // A5
            Pair(784.0, 600),  // G5
            Pair(659.0, 600),  // E5
        )
        
        var noteIndex = 0
        
        fun playNextNote() {
            if (!isPlaying) return
            
            val (frequency, duration) = melody[noteIndex]
            playMusicNote(ctx, frequency, duration / 1000.0)
            
            noteIndex = (noteIndex + 1) % melody.size
        }
        
        // Iniciar primera nota
        playNextNote()
        
        // Programar siguientes notas
        var totalTime = 0
        intervalId = window.setInterval({
            if (isPlaying) {
                playNextNote()
            }
        }, 300)
    }
    
    private fun playMusicNote(ctx: AudioContext, frequency: Double, duration: Double) {
        try {
            val oscillator = ctx.createOscillator()
            val gainNode = ctx.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(ctx.destination)
            
            oscillator.type = "triangle".asDynamic()
            oscillator.frequency.setValueAtTime(frequency, ctx.currentTime)
            
            // Envelope suave
            gainNode.gain.setValueAtTime(0.0, ctx.currentTime)
            gainNode.gain.linearRampToValueAtTime(0.15, ctx.currentTime + 0.05)
            gainNode.gain.linearRampToValueAtTime(0.1, ctx.currentTime + duration * 0.7)
            gainNode.gain.linearRampToValueAtTime(0.0, ctx.currentTime + duration)
            
            oscillator.start(ctx.currentTime)
            oscillator.stop(ctx.currentTime + duration)
        } catch (e: Exception) {
            // Ignorar errores de audio
        }
    }
}
