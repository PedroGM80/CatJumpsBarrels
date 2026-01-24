package dev.pgm.game.audio

/**
 * Interfaz multiplataforma para el reproductor de música del menú.
 * Cada plataforma proporciona su implementación.
 */
expect object MusicController {
    
    /** Iniciar música del menú */
    fun playMenuMusic()
    
    /** Detener música */
    fun stopMusic()
}
