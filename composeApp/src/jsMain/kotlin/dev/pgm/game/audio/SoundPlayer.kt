package dev.pgm.game.audio

import kotlinx.browser.window

/**
 * Implementación JS del reproductor de sonidos usando Web Audio API.
 * Genera sonidos retro 8-bit idénticos a la versión de escritorio.
 */
actual object SoundPlayer {
    
    private var soundEnabled = true
    private var audioContext: dynamic = null
    
    private fun getAudioContext(): dynamic {
        if (audioContext == null) {
            audioContext = js("new (window.AudioContext || window.webkitAudioContext)()")
        }
        // Resume context if suspended (browsers require user interaction)
        val ctx = audioContext
        if (ctx.state == "suspended") {
            ctx.resume()
        }
        return ctx
    }
    
    actual fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }
    
    /**
     * Sonido de salto - barrido ascendente rápido (200Hz -> 600Hz, 100ms)
     */
    actual fun playJump() {
        if (!soundEnabled) return
        playFrequencySweep(
            startFreq = 200.0,
            endFreq = 600.0,
            durationSec = 0.1,
            squareWave = true
        )
    }
    
    /**
     * Sonido de puntuación - dos tonos alegres
     */
    actual fun playScore() {
        if (!soundEnabled) return
        val ctx = getAudioContext()
        val now = ctx.currentTime as Double
        
        // Tono 1: 880Hz, 50ms
        playTone(880.0, 0.05, now, squareWave = true)
        // Tono 2: 1100Hz, 80ms (después del primero)
        playTone(1100.0, 0.08, now + 0.05, squareWave = true)
    }
    
    /**
     * Sonido de muerte - barrido descendente largo (800Hz -> 100Hz, 400ms)
     */
    actual fun playDeath() {
        if (!soundEnabled) return
        playFrequencySweep(
            startFreq = 800.0,
            endFreq = 100.0,
            durationSec = 0.4,
            squareWave = true
        )
    }
    
    /**
     * Sonido de victoria - melodía ascendente C5-E5-G5-C6
     */
    actual fun playWin() {
        if (!soundEnabled) return
        val ctx = getAudioContext()
        val now = ctx.currentTime as Double
        
        playTone(523.0, 0.1, now, squareWave = true)         // C5
        playTone(659.0, 0.1, now + 0.1, squareWave = true)   // E5
        playTone(784.0, 0.1, now + 0.2, squareWave = true)   // G5
        playTone(1047.0, 0.2, now + 0.3, squareWave = true)  // C6
    }
    
    /**
     * Sonido de pausa - tono corto 440Hz, 50ms
     */
    actual fun playPause() {
        if (!soundEnabled) return
        playTone(440.0, 0.05, squareWave = true)
    }
    
    /**
     * Sonido de selección en menú - 660Hz, 30ms
     */
    actual fun playMenuSelect() {
        if (!soundEnabled) return
        playTone(660.0, 0.03, squareWave = true)
    }
    
    /**
     * Sonido de confirmación en menú - dos tonos
     */
    actual fun playMenuConfirm() {
        if (!soundEnabled) return
        val ctx = getAudioContext()
        val now = ctx.currentTime as Double
        
        playTone(440.0, 0.05, now, squareWave = true)
        playTone(880.0, 0.1, now + 0.05, squareWave = true)
    }
    
    /**
     * Sonido de barril lanzado - barrido descendente corto (300Hz -> 150Hz, 80ms)
     */
    actual fun playBarrelThrow() {
        if (!soundEnabled) return
        playFrequencySweep(
            startFreq = 300.0,
            endFreq = 150.0,
            durationSec = 0.08,
            squareWave = true
        )
    }
    
    /**
     * Reproduce un tono simple con envelope para evitar clicks.
     */
    private fun playTone(
        frequency: Double,
        durationSec: Double,
        startTime: Double? = null,
        squareWave: Boolean = false
    ) {
        try {
            val ctx = getAudioContext()
            val now = startTime ?: (ctx.currentTime as Double)
            
            val oscillator = ctx.createOscillator()
            val gainNode = ctx.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(ctx.destination)
            
            // Tipo de onda (square para sonido 8-bit auténtico)
            oscillator.type = if (squareWave) "square" else "sine"
            oscillator.frequency.value = frequency
            
            // Envelope: attack-sustain-release para evitar clicks
            val volume = 0.15  // Volumen reducido similar a JVM
            val attackTime = durationSec * 0.1
            val releaseTime = durationSec * 0.1
            
            gainNode.gain.setValueAtTime(0.0, now)
            gainNode.gain.linearRampToValueAtTime(volume, now + attackTime)
            gainNode.gain.setValueAtTime(volume, now + durationSec - releaseTime)
            gainNode.gain.linearRampToValueAtTime(0.0, now + durationSec)
            
            oscillator.start(now)
            oscillator.stop(now + durationSec)
        } catch (e: Throwable) {
            // Audio no disponible, ignorar silenciosamente
            console.log("Audio error: ${e.message}")
        }
    }
    
    /**
     * Reproduce un barrido de frecuencia (frequency sweep) con envelope.
     */
    private fun playFrequencySweep(
        startFreq: Double,
        endFreq: Double,
        durationSec: Double,
        squareWave: Boolean = false
    ) {
        try {
            val ctx = getAudioContext()
            val now = ctx.currentTime as Double
            
            val oscillator = ctx.createOscillator()
            val gainNode = ctx.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(ctx.destination)
            
            oscillator.type = if (squareWave) "square" else "sine"
            
            // Barrido de frecuencia
            oscillator.frequency.setValueAtTime(startFreq, now)
            oscillator.frequency.exponentialRampToValueAtTime(endFreq, now + durationSec)
            
            // Envelope para evitar clicks
            val volume = 0.15
            val attackTime = durationSec * 0.1
            val releaseTime = durationSec * 0.1
            
            gainNode.gain.setValueAtTime(0.0, now)
            gainNode.gain.linearRampToValueAtTime(volume, now + attackTime)
            gainNode.gain.setValueAtTime(volume, now + durationSec - releaseTime)
            gainNode.gain.linearRampToValueAtTime(0.0, now + durationSec)
            
            oscillator.start(now)
            oscillator.stop(now + durationSec)
        } catch (e: Throwable) {
            console.log("Audio error: ${e.message}")
        }
    }
}

// Acceso a console.log para debugging
private external val console: dynamic
