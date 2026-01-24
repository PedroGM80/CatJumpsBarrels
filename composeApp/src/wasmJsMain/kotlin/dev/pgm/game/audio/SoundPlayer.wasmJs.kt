package dev.pgm.game.audio

import kotlinx.browser.window
import org.w3c.dom.AudioContext

/**
 * Implementación Web del reproductor de sonidos usando Web Audio API.
 */
actual object SoundPlayer {
    
    private var audioContext: AudioContext? = null
    private var soundEnabled = true
    
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
    
    actual fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }
    
    actual fun playJump() {
        if (!soundEnabled) return
        playTone(frequency = 400.0, duration = 0.1, type = "square", frequencyEnd = 600.0)
    }
    
    actual fun playScore() {
        if (!soundEnabled) return
        playTone(frequency = 880.0, duration = 0.05, type = "square")
        window.setTimeout({ playTone(frequency = 1100.0, duration = 0.08, type = "square") }, 50)
    }
    
    actual fun playDeath() {
        if (!soundEnabled) return
        playTone(frequency = 800.0, duration = 0.4, type = "square", frequencyEnd = 100.0)
    }
    
    actual fun playWin() {
        if (!soundEnabled) return
        playTone(frequency = 523.0, duration = 0.1, type = "square")
        window.setTimeout({ playTone(frequency = 659.0, duration = 0.1, type = "square") }, 100)
        window.setTimeout({ playTone(frequency = 784.0, duration = 0.1, type = "square") }, 200)
        window.setTimeout({ playTone(frequency = 1047.0, duration = 0.2, type = "square") }, 300)
    }
    
    actual fun playPause() {
        if (!soundEnabled) return
        playTone(frequency = 440.0, duration = 0.05, type = "square")
    }
    
    actual fun playMenuSelect() {
        if (!soundEnabled) return
        playTone(frequency = 660.0, duration = 0.03, type = "square")
    }
    
    actual fun playMenuConfirm() {
        if (!soundEnabled) return
        playTone(frequency = 440.0, duration = 0.05, type = "square")
        window.setTimeout({ playTone(frequency = 880.0, duration = 0.1, type = "square") }, 50)
    }
    
    actual fun playBarrelThrow() {
        if (!soundEnabled) return
        playTone(frequency = 300.0, duration = 0.08, type = "square", frequencyEnd = 150.0)
    }
    
    private fun playTone(
        frequency: Double, 
        duration: Double, 
        type: String = "square",
        frequencyEnd: Double? = null
    ) {
        val ctx = getAudioContext() ?: return
        
        try {
            val oscillator = ctx.createOscillator()
            val gainNode = ctx.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(ctx.destination)
            
            oscillator.type = type.toOscillatorType()
            oscillator.frequency.setValueAtTime(frequency, ctx.currentTime)
            
            if (frequencyEnd != null) {
                oscillator.frequency.linearRampToValueAtTime(frequencyEnd, ctx.currentTime + duration)
            }
            
            // Envelope para evitar clicks
            gainNode.gain.setValueAtTime(0.0, ctx.currentTime)
            gainNode.gain.linearRampToValueAtTime(0.3, ctx.currentTime + 0.01)
            gainNode.gain.linearRampToValueAtTime(0.0, ctx.currentTime + duration)
            
            oscillator.start(ctx.currentTime)
            oscillator.stop(ctx.currentTime + duration)
        } catch (e: Exception) {
            // Ignorar errores de audio
        }
    }
    
    private fun String.toOscillatorType(): dynamic {
        return this.asDynamic()
    }
}
