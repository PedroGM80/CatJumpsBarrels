package dev.pgm.game.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/**
 * Fuentes personalizadas del juego usando Google Fonts
 */
object GameFonts {
    /**
     * Fuente arcade retro Press Start 2P de Google Fonts
     * Perfecta para juegos estilo Donkey Kong / arcade clásico
     */
    val PressStart2P = FontFamily(
        Font(
            resource = "font/PressStart2P-Regular.ttf",
            weight = FontWeight.Normal
        )
    )

    /**
     * Fuente principal del juego - usar Press Start 2P para estilo arcade
     */
    val GameFont = PressStart2P
}
