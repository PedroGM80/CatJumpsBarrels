package dev.pgm.game.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Fuentes personalizadas del juego.
 */
object GameFonts {
    /**
     * Usamos Monospace por defecto ya que tiene un estilo "retro" / "arcade"
     * y está disponible en todos los sistemas sin necesidad de archivos externos.
     */
    val PressStart2P = FontFamily.Monospace

    /**
     * Fuente principal del juego
     */
    val GameFont = PressStart2P
}
