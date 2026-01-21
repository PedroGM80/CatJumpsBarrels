package dev.pgm.game.domain.usecase

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel
import dev.pgm.game.model.entities.Ladder
import dev.pgm.game.model.entities.Platform
import kotlin.math.abs
import kotlin.random.Random

/**
 * Use Case: Actualizar el estado de un solo barril.
 * Responsabilidad única: Manejar el movimiento y física de un barril individual.
 */
class UpdateSingleBarrelUseCase {

    operator fun invoke(barrel: Barrel, state: GameState): Barrel? {
        // Eliminar barriles fuera del área de juego (usando altura de diseño)
        if (barrel.position.y > GameConstants.LEVEL_HEIGHT + GameConstants.BARREL_SCREEN_CLEANUP_OFFSET) {
            return null
        }

        // Actualizar rotación visual según dirección
        val rotationSpeed = if (barrel.velocity.x != 0f) {
            barrel.velocity.x * GameConstants.BARREL_ROLL_SPEED
        } else {
            GameConstants.BARREL_LADDER_FALL_SPEED * GameConstants.BARREL_ROLL_SPEED
        }
        val newRotation = barrel.rotation + rotationSpeed

        return when {
            barrel.isOnLadder -> updateBarrelOnLadder(barrel, newRotation, state)
            barrel.isFalling -> updateBarrelFalling(barrel, newRotation, state)
            else -> updateBarrelOnPlatform(barrel, newRotation, state)
        }
    }

    /**
     * Barril bajando por una escalera
     */
    private fun updateBarrelOnLadder(barrel: Barrel, rotation: Float, state: GameState): Barrel {
        val fallSpeed = GameConstants.BARREL_LADDER_FALL_SPEED
        val newY = barrel.position.y + fallSpeed

        // Buscar la plataforma inferior donde debe aterrizar
        val targetPlatformIndex = barrel.targetPlatformIndex ?: (barrel.currentPlatformIndex - 1)
        if (targetPlatformIndex >= 0 && targetPlatformIndex < state.platforms.size) {
            val targetPlatform = state.platforms[targetPlatformIndex]
            val platformY = targetPlatform.getYAt(barrel.centerX)

            // Verificar si el barril ha llegado a la plataforma
            if (newY + barrel.size >= platformY) {
                // Aterriza en la plataforma inferior y cambia de dirección
                val newDirection = getBarrelDirectionForPlatform(targetPlatformIndex)
                return barrel.copy(
                    position = Offset(barrel.position.x, platformY - barrel.size),
                    isOnLadder = false,
                    currentPlatformIndex = targetPlatformIndex,
                    velocity = Offset(GameConstants.BARREL_SPEED * newDirection, 0f),
                    rotation = rotation,
                    lastLadderChecked = -1,
                    targetPlatformIndex = null
                )
            }
        }

        return barrel.copy(
            position = Offset(barrel.position.x, newY),
            velocity = Offset(0f, fallSpeed),
            rotation = rotation,
            isOnLadder = true
        )
    }

    /**
     * Barril cayendo en caída libre (por el borde de la plataforma)
     */
    private fun updateBarrelFalling(barrel: Barrel, rotation: Float, state: GameState): Barrel {
        val fallSpeed = GameConstants.BARREL_FREE_FALL_SPEED

        // Mantener velocidad horizontal mientras cae
        val newX = barrel.position.x + barrel.velocity.x
        val newY = barrel.position.y + fallSpeed

        // Buscar colisión con cualquier plataforma inferior
        for (platform in state.platforms) {
            if (platform.index >= barrel.currentPlatformIndex) continue

            // Verificar si el barril está dentro del rango horizontal de la plataforma
            val barrelCenterX = newX + barrel.size / 2
            if (barrelCenterX >= platform.left && barrelCenterX <= platform.right) {
                val platformY = platform.getYAt(barrelCenterX)

                // Verificar si el barril ha llegado a esta plataforma
                if (barrel.bottom < platformY && newY + barrel.size >= platformY) {
                    val newDirection = getBarrelDirectionForPlatform(platform.index)
                    return barrel.copy(
                        position = Offset(newX, platformY - barrel.size),
                        isFalling = false,
                        currentPlatformIndex = platform.index,
                        velocity = Offset(GameConstants.BARREL_SPEED * newDirection, 0f),
                        rotation = rotation,
                        lastLadderChecked = -1
                    )
                }
            }
        }

        return barrel.copy(
            position = Offset(newX, newY),
            velocity = Offset(barrel.velocity.x, fallSpeed),
            rotation = rotation
        )
    }

    /**
     * Barril rodando sobre una plataforma
     */
    private fun updateBarrelOnPlatform(barrel: Barrel, rotation: Float, state: GameState): Barrel {
        val platform = state.platforms.getOrNull(barrel.currentPlatformIndex) ?: return barrel

        // Calcular nueva posición X
        val newX = barrel.position.x + barrel.velocity.x

        // Calcular Y según la pendiente de la plataforma
        val newY = platform.getYAt(newX + barrel.size / 2) - barrel.size

        // PRIMERO verificar si el barril está sobre una escalera (puede bajar)
        val ladderBelow = findLadderBelowBarrel(barrel.copy(position = Offset(newX, newY)), platform, state)
        if (ladderBelow != null && barrel.lastLadderChecked != ladderBelow.hashCode()) {
            // Probabilidad aleatoria de bajar por la escalera (como en DK original)
            if (Random.nextFloat() < GameConstants.BARREL_LADDER_PROBABILITY) {
                return barrel.copy(
                    position = Offset(ladderBelow.centerX - barrel.size / 2, newY),
                    isOnLadder = true,
                    velocity = Offset(0f, GameConstants.BARREL_LADDER_FALL_SPEED),
                    rotation = rotation,
                    lastLadderChecked = ladderBelow.hashCode(),
                    targetPlatformIndex = ladderBelow.bottomPlatformIndex
                )
            } else {
                // Marcamos que ya verificamos esta escalera para no preguntar de nuevo
                return barrel.copy(
                    position = Offset(newX, newY),
                    rotation = rotation,
                    lastLadderChecked = ladderBelow.hashCode()
                )
            }
        }

        // DESPUES verificar bordes de la plataforma
        // Usar el centro del barril para que caiga cuando esté más del borde
        val barrelCenterX = newX + barrel.size / 2
        val atLeftEdge = barrelCenterX < platform.left
        val atRightEdge = barrelCenterX > platform.right

        if (atLeftEdge || atRightEdge) {
            return handleBarrelAtEdge(barrel, platform, atLeftEdge, rotation, state)
        }

        return barrel.copy(
            position = Offset(newX, newY),
            rotation = rotation
        )
    }

    /**
     * Busca una escalera debajo del barril en la plataforma actual
     */
    private fun findLadderBelowBarrel(barrel: Barrel, platform: Platform, state: GameState): Ladder? {
        return state.ladders.find { ladder ->
            val matchesPlatform = ladder.topPlatformIndex == platform.index
            val distance = abs(barrel.centerX - ladder.centerX)
            val withinTolerance = distance < GameConstants.BARREL_OVER_LADDER_TOLERANCE
            matchesPlatform && withinTolerance
        }
    }

    /**
     * Maneja cuando el barril llega al borde de una plataforma
     */
    private fun handleBarrelAtEdge(
        barrel: Barrel,
        platform: Platform,
        atLeftEdge: Boolean,
        rotation: Float,
        state: GameState
    ): Barrel {
        val nextPlatformIndex = barrel.currentPlatformIndex - 1

        if (nextPlatformIndex < 0) {
            // Ya está en la plataforma más baja, cae fuera de pantalla
            return barrel.copy(
                isFalling = true,
                currentPlatformIndex = -1,
                velocity = Offset(0f, GameConstants.BARREL_FREE_FALL_SPEED),
                rotation = rotation
            )
        }

        // Buscar escalera en el borde
        val ladderAtEdge = findLadderAtEdge(platform, atLeftEdge, state)

        return if (ladderAtEdge != null && Random.nextFloat() < GameConstants.BARREL_LADDER_PROBABILITY) {
            // Bajar por la escalera del borde (con probabilidad aleatoria)
            barrel.copy(
                position = Offset(ladderAtEdge.centerX - barrel.size / 2, barrel.position.y),
                isOnLadder = true,
                velocity = Offset(0f, GameConstants.BARREL_LADDER_FALL_SPEED),
                rotation = rotation,
                targetPlatformIndex = ladderAtEdge.bottomPlatformIndex
            )
        } else {
            // Caer por el borde de la plataforma con velocidad horizontal reducida
            val reducedHorizontalSpeed = barrel.velocity.x * 0.3f
            barrel.copy(
                isFalling = true,
                velocity = Offset(reducedHorizontalSpeed, GameConstants.BARREL_FREE_FALL_SPEED),
                rotation = rotation
            )
        }
    }

    /**
     * Busca una escalera en el borde de la plataforma
     */
    private fun findLadderAtEdge(platform: Platform, atLeftEdge: Boolean, state: GameState): Ladder? {
        val checkX = if (atLeftEdge) {
            platform.left + GameConstants.BARREL_LADDER_CHECK_OFFSET
        } else {
            platform.right - GameConstants.BARREL_LADDER_CHECK_OFFSET
        }
        return state.ladders.find { ladder ->
            ladder.topPlatformIndex == platform.index &&
            abs(checkX - ladder.centerX) < GameConstants.BARREL_LADDER_CHECK_TOLERANCE
        }
    }

    /**
     * Determina la dirección del barril según el índice de la plataforma
     * (alterna izquierda/derecha como en Donkey Kong)
     */
    private fun getBarrelDirectionForPlatform(platformIndex: Int): Float {
        // Plataformas pares: barril va hacia la derecha
        // Plataformas impares: barril va hacia la izquierda
        return if (platformIndex % 2 == 0) 1f else -1f
    }
}
