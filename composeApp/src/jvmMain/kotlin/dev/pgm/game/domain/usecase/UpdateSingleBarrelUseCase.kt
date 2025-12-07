package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel

/**
 * Use Case: Actualizar el estado de un solo barril.
 * Responsabilidad única: Manejar el movimiento y física de un barril individual.
 */
class UpdateSingleBarrelUseCase {

    operator fun invoke(barrel: Barrel, state: GameState): Barrel? {
        // Eliminar barriles fuera de pantalla
        if (barrel.position.y > state.screenSize.height + GameConstants.BARREL_SCREEN_CLEANUP_OFFSET) {
            return null
        }

        val newRotation = barrel.rotation + GameConstants.BARREL_ROLL_SPEED

        return when {
            barrel.isOnLadder -> updateBarrelOnLadder(barrel, newRotation)
            barrel.isFalling -> updateBarrelFalling(barrel, newRotation)
            else -> updateBarrelOnPlatform(barrel, newRotation, state)
        }
    }

    private fun updateBarrelOnLadder(barrel: Barrel, rotation: Float): Barrel {
        val newY = barrel.position.y + GameConstants.BARREL_LADDER_FALL_SPEED
        return barrel.copy(
            position = barrel.position.copy(y = newY),
            rotation = rotation
        )
    }

    private fun updateBarrelFalling(barrel: Barrel, rotation: Float): Barrel {
        val newY = barrel.position.y + GameConstants.BARREL_FREE_FALL_SPEED
        return barrel.copy(
            position = barrel.position.copy(y = newY),
            rotation = rotation
        )
    }

    private fun updateBarrelOnPlatform(barrel: Barrel, rotation: Float, state: GameState): Barrel {
        val platformY = state.platforms.getOrNull(barrel.currentPlatformIndex)
            ?.getYAt(barrel.position.x + barrel.size / 2) ?: return barrel

        val newX = barrel.position.x + barrel.velocity.x
        val newY = platformY - barrel.size

        return barrel.copy(
            position = barrel.position.copy(x = newX, y = newY),
            rotation = rotation
        )
    }
}
