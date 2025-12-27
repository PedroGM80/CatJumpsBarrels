package dev.pgm.game.core.utils

/**
 * Representa un rectángulo alineado a los ejes para detección de colisiones.
 *
 * @property x Coordenada X de la esquina superior izquierda
 * @property y Coordenada Y de la esquina superior izquierda
 * @property width Ancho del rectángulo
 * @property height Alto del rectángulo
 */
data class GameRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    /** Coordenada X del borde izquierdo */
    val left: Float get() = x

    /** Coordenada X del borde derecho */
    val right: Float get() = x + width

    /** Coordenada Y del borde superior */
    val top: Float get() = y

    /** Coordenada Y del borde inferior */
    val bottom: Float get() = y + height

    /**
     * Detecta si este rectángulo se solapa con otro.
     * Usa el algoritmo de Axis-Aligned Bounding Box (AABB).
     *
     * @param other El otro rectángulo a verificar
     * @return true si hay solapamiento, false si no
     */
    fun overlaps(other: GameRect): Boolean {
        return left < other.right && right > other.left &&
               top < other.bottom && bottom > other.top
    }
}
