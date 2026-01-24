package dev.pgm.game.audio

/**
 * Interfaz multiplataforma para generación de sonidos retro.
 * Cada plataforma proporciona su implementación (JVM usa Java Sound API, Web usa Web Audio API).
 */
expect object SoundPlayer {
    
    /** Habilitar o deshabilitar sonidos */
    fun setSoundEnabled(enabled: Boolean)
    
    /** Sonido de salto */
    fun playJump()
    
    /** Sonido al obtener puntos */
    fun playScore()
    
    /** Sonido de muerte */
    fun playDeath()
    
    /** Sonido de victoria */
    fun playWin()
    
    /** Sonido de pausa */
    fun playPause()
    
    /** Sonido de selección en menú */
    fun playMenuSelect()
    
    /** Sonido de confirmación en menú */
    fun playMenuConfirm()
    
    /** Sonido de barril lanzado */
    fun playBarrelThrow()
}
