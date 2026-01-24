package dev.pgm.game.audio

/**
 * Implementación JVM del controlador de música usando MusicPlayer.
 */
actual object MusicController {
    
    actual fun playMenuMusic() {
        MusicPlayer.playMenuMusic()
    }
    
    actual fun stopMusic() {
        MusicPlayer.stopMusic()
    }
}
