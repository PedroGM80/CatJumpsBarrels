package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.core.utils.GameRect

/**
 * Representa un barril enemigo que rueda por las plataformas.
 *
 * Los barriles son spawneados por Donkey Kong y ruedan por las plataformas,
 * con probabilidad de bajar por las escaleras (como en el Donkey Kong original).
 *
 * @property position Posición actual del barril (esquina superior izquierda)
 * @property size Tamaño del sprite del barril
 * @property velocity Velocidad actual (x: horizontal cuando rueda, y: vertical cuando cae)
 * @property rotation Rotación acumulada para el efecto visual de rodar
 * @property isOnLadder Indica si está actualmente cayendo por una escalera
 * @property isFalling Indica si está cayendo entre plataformas (por el borde)
 * @property currentPlatformIndex Índice de la plataforma en la que se encuentra
 * @property hasBeenJumped Indica si el jugador ya saltó sobre este barril (para evitar puntos duplicados)
 * @property lastLadderChecked ID de la última escalera verificada para evitar doble check
 */
data class Barrel(
    val position: Offset,
    val size: Float = GameConstants.BARREL_SIZE,
    val velocity: Offset = Offset(GameConstants.BARREL_SPEED, 0f),
    val rotation: Float = 0f,
    val isOnLadder: Boolean = false,
    val isFalling: Boolean = false,
    val currentPlatformIndex: Int = 0,
    val hasBeenJumped: Boolean = false,
    val lastLadderChecked: Int = -1,
    val targetPlatformIndex: Int? = null
) {
    /**
     * Hitbox reducida del barril para colisiones.
     * Ligeramente más pequeña que el sprite para mejor sensación de juego.
     */
    val hitbox: GameRect
        get() = GameRect(
            position.x + 3f,
            position.y + 3f,
            size - 6f,
            size - 6f
        )

    /** Centro X del barril */
    val centerX: Float get() = position.x + size / 2

    /** Parte inferior del barril */
    val bottom: Float get() = position.y + size
}
