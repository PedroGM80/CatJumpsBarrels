package dev.pgm.game.audio

/**
 * Implementación JVM del reproductor de sonidos usando RetroSoundGenerator.
 */
actual object SoundPlayer {
    
    actual fun setSoundEnabled(enabled: Boolean) {
        RetroSoundGenerator.setSoundEnabled(enabled)
    }
    
    actual fun playJump() {
        RetroSoundGenerator.playJump()
    }
    
    actual fun playScore() {
        RetroSoundGenerator.playScore()
    }
    
    actual fun playDeath() {
        RetroSoundGenerator.playDeath()
    }
    
    actual fun playWin() {
        RetroSoundGenerator.playWin()
    }
    
    actual fun playPause() {
        RetroSoundGenerator.playPause()
    }
    
    actual fun playMenuSelect() {
        RetroSoundGenerator.playMenuSelect()
    }
    
    actual fun playMenuConfirm() {
        RetroSoundGenerator.playMenuConfirm()
    }
    
    actual fun playBarrelThrow() {
        RetroSoundGenerator.playBarrelThrow()
    }
}
