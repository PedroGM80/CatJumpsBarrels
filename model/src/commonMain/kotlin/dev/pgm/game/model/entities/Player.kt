package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.core.utils.GameRect

/**
 * Dirección horizontal del jugador.
 */
enum class Direction {
    /** Mirando hacia la izquierda */
    LEFT,
    /** Mirando hacia la derecha */
    RIGHT
}

/**
 * Estados posibles del jugador para controlar animaciones y comportamiento.
 */
enum class PlayerState {
    /** Quieto sin moverse */
    IDLE,
    /** Corriendo horizontalmente */
    RUNNING,
    /** Saltando (en ascenso) */
    JUMPING,
    /** Cayendo (en descenso) */
    FALLING,
    /** Subiendo o bajando por una escalera */
    CLIMBING,
    /** Recibiendo daño */
    HURT,
    /** Muerto */
    DEAD
}

/**
 * Representa al jugador (gato) en el juego.
 *
 * @property position Posición actual en pantalla (esquina superior izquierda)
 * @property size Tamaño del sprite del jugador
 * @property velocity Velocidad actual (x: horizontal, y: vertical)
 * @property isJumping Indica si está ejecutando un salto
 * @property isOnGround Indica si está tocando el suelo/plataforma
 * @property isClimbing Indica si está en una escalera
 * @property direction Dirección hacia donde mira el jugador
 * @property state Estado actual para animación
 * @property invincibleUntil Timestamp hasta cuándo es invencible (tras morir)
 * @property animationFrame Frame actual de animación (para walking)
 */
data class Player(
    val position: Offset,
    val size: Float = 32f,
    val velocity: Offset = Offset.Zero,
    val isJumping: Boolean = false,
    val isOnGround: Boolean = false,
    val isClimbing: Boolean = false,
    val direction: Direction = Direction.RIGHT,
    val state: PlayerState = PlayerState.IDLE,
    val invincibleUntil: Long = 0L,
    val animationFrame: Int = 0
) {
    /**
     * Determina si el jugador es actualmente invencible.
     * Usado para evitar daño tras respawnear.
     */
    val isInvincible: Boolean
        get() = System.currentTimeMillis() < invincibleUntil

    /**
     * Hitbox reducida del jugador para colisiones más precisas.
     * Es ligeramente más pequeña que el sprite para mejor gameplay.
     */
    val hitbox: GameRect
        get() = GameRect(
            position.x + 4f,     // Reducir 4px por cada lado
            position.y + 4f,
            size - 8f,           // Ancho reducido
            size - 4f            // Alto ligeramente menos reducido
        )
}
