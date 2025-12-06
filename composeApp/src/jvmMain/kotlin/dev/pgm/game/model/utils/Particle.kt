package dev.pgm.game.model.utils

import androidx.compose.ui.geometry.Offset

/**
 * Representa una partícula de efecto visual.
 *
 * Las partículas se usan para efectos de spawn de barriles, muerte del jugador,
 * obtención de puntos, y victoria.
 *
 * @property position Posición actual de la partícula
 * @property velocity Velocidad de movimiento (afectada por gravedad)
 * @property color Color de la partícula en formato ARGB (Long)
 * @property size Tamaño de la partícula en píxeles
 * @property lifetime Tiempo de vida total en segundos
 * @property age Edad actual de la partícula en segundos
 */
data class Particle(
    val position: Offset,
    val velocity: Offset,
    val color: Long,
    val size: Float,
    val lifetime: Float,
    val age: Float = 0f
) {
    /**
     * Indica si la partícula aún está viva y debe renderizarse.
     */
    val isAlive: Boolean get() = age < lifetime

    /**
     * Calcula la opacidad de la partícula basada en su edad.
     * Las partículas se desvanecen gradualmente hasta desaparecer.
     *
     * @return Alpha de 0.0 (transparente) a 1.0 (opaco)
     */
    val alpha: Float get() = 1f - (age / lifetime)
}
