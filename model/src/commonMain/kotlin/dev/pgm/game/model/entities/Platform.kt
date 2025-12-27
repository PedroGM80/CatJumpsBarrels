package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset

/**
 * Representa una plataforma en el nivel.
 *
 * Las plataformas pueden tener inclinación, lo que afecta el movimiento
 * de los barriles y la posición Y del jugador al estar sobre ellas.
 *
 * @property position Posición de la esquina superior izquierda
 * @property width Ancho de la plataforma
 * @property height Altura visual de la plataforma
 * @property index Índice de la plataforma (0 = inferior, 5 = DK, 6 = princesa)
 * @property slope Inclinación de la plataforma (positivo = sube hacia la derecha, negativo = baja)
 */
data class Platform(
    val position: Offset,
    val width: Float,
    val height: Float = 10f,
    val index: Int = 0,
    val slope: Float = 0f
) {
    /** Coordenada X del borde izquierdo */
    val left: Float get() = position.x

    /** Coordenada X del borde derecho */
    val right: Float get() = position.x + width

    /** Coordenada Y del borde superior (superficie donde se camina) */
    val top: Float get() = position.y

    /**
     * Calcula la altura Y de la superficie de la plataforma en una coordenada X dada.
     * Tiene en cuenta la inclinación de la plataforma.
     *
     * @param x Coordenada X donde calcular la altura
     * @return Coordenada Y de la superficie en ese punto
     */
    fun getYAt(x: Float): Float {
        // La inclinación se aplica proporcionalmente desde el inicio de la plataforma
        val xOffset = x - position.x
        return top + xOffset * slope
    }
}
