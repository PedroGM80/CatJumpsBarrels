package dev.pgm.game.domain.usecase

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import kotlin.math.abs

/**
 * Use Case: Actualizar el estado del jugador basado en el input.
 * Responsabilidad única: Manejar toda la lógica relacionada con el movimiento y estado del jugador.
 */
class UpdatePlayerUseCase {

    operator fun invoke(state: GameState, input: GameInput, deltaTime: Float): GameState {
        if (input.left || input.right || input.up || input.down || input.jump) {
            println("LOG 5: UpdatePlayerUseCase - input=$input, playerPos=${state.player.position}")
        }

        if (state.player.state == PlayerState.DEAD) {
            return state
        }

        var player = state.player

        // Determinar si debe escalar
        val nearLadder = findNearbyLadder(player, state.ladders)
        val shouldClimb = player.isClimbing || (nearLadder != null && (input.up || input.down))

        player = if (shouldClimb) {
            handleClimbing(player, input, nearLadder, state.platforms)
        } else {
            handleNormalMovement(player, input, state.platforms)
        }

        return state.copy(player = player)
    }

    private fun handleNormalMovement(
        player: Player,
        input: GameInput,
        platforms: List<Platform>
    ): Player {
        var newPlayer = player

        // Determinar dirección según input
        var direction = player.direction

        // Movimiento horizontal
        val horizontalVelocity = when {
            input.left -> {
                direction = Direction.LEFT
                -GameConstants.MOVE_SPEED
            }
            input.right -> {
                direction = Direction.RIGHT
                GameConstants.MOVE_SPEED
            }
            else -> 0f
        }

        // Salto
        val shouldJump = input.jump && player.isOnGround
        val verticalVelocity = if (shouldJump) {
            GameConstants.JUMP_STRENGTH
        } else {
            player.velocity.y + GameConstants.GRAVITY
        }

        val newVelocity = Offset(horizontalVelocity, verticalVelocity)
        var newPosition = player.position + newVelocity

        // Limites horizontales
        newPosition = newPosition.copy(
            x = newPosition.x.coerceIn(0f, GameConstants.LEVEL_WIDTH - player.size)
        )

        // Colisión con plataformas
        val collision = checkPlatformCollision(
            newPosition,
            player.size,
            newVelocity,
            platforms
        )

        newPlayer = if (collision != null) {
            player.copy(
                position = collision.position,
                velocity = Offset(horizontalVelocity, 0f),
                isOnGround = true,
                direction = direction,
                state = determinePlayerState(input, isOnGround = true)
            )
        } else {
            player.copy(
                position = newPosition,
                velocity = newVelocity,
                isOnGround = false,
                direction = direction,
                state = determinePlayerState(input, isOnGround = false, velocityY = newVelocity.y)
            )
        }

        return newPlayer
    }

    private fun handleClimbing(
        player: Player,
        input: GameInput,
        ladder: Ladder?,
        platforms: List<Platform>
    ): Player {
        if (ladder == null) {
            return player.copy(isClimbing = false)
        }

        val newX = ladder.centerX - player.size / 2
        val playerCenterX = newX + player.size / 2

        val topPlatform = platforms.getOrNull(ladder.topPlatformIndex)
        val bottomPlatform = platforms.getOrNull(ladder.bottomPlatformIndex)

        if (topPlatform == null || bottomPlatform == null) {
            return player.copy(isClimbing = false)
        }

        val topPlatformY = topPlatform.getYAt(playerCenterX)
        val bottomPlatformY = bottomPlatform.getYAt(playerCenterX)

        var newY = player.position.y
        if (input.up) newY -= GameConstants.CLIMB_SPEED
        if (input.down) newY += GameConstants.CLIMB_SPEED

        val playerBottom = newY + player.size
        val climbTolerance = player.size * 0.6f

        // Verificar si llegó a plataforma superior
        if (input.up && playerBottom <= topPlatformY + climbTolerance) {
            return player.copy(
                position = Offset(newX, topPlatformY - player.size),
                isClimbing = false,
                velocity = Offset.Zero,
                isOnGround = true,
                state = PlayerState.IDLE
            )
        }

        // Verificar si llegó a plataforma inferior
        if (input.down && playerBottom >= bottomPlatformY - climbTolerance) {
            return player.copy(
                position = Offset(newX, bottomPlatformY - player.size),
                isClimbing = false,
                velocity = Offset.Zero,
                isOnGround = true,
                state = PlayerState.IDLE
            )
        }

        return player.copy(
            position = Offset(newX, newY),
            isClimbing = true,
            velocity = Offset.Zero,
            isOnGround = false,
            state = PlayerState.CLIMBING
        )
    }

    private fun findNearbyLadder(player: Player, ladders: List<Ladder>): Ladder? {
        val playerCenterX = player.position.x + player.size / 2
        val playerTop = player.position.y
        val playerBottom = player.position.y + player.size

        return ladders.find { ladder ->
            val horizontalClose = abs(playerCenterX - ladder.centerX) < GameConstants.LADDER_HORIZONTAL_TOLERANCE
            val verticalMargin = player.size * 0.8f
            val onLadderVertical = playerBottom > ladder.top - verticalMargin && playerTop < ladder.bottom + verticalMargin
            horizontalClose && onLadderVertical
        }
    }

    private fun checkPlatformCollision(
        position: Offset,
        size: Float,
        velocity: Offset,
        platforms: List<Platform>
    ): PlatformCollision? {
        if (velocity.y <= 0) return null

        val playerBottom = position.y + size
        val playerCenterX = position.x + size / 2

        for (platform in platforms) {
            if (playerCenterX < platform.left || playerCenterX > platform.right) continue

            val platformY = platform.getYAt(playerCenterX)
            if (playerBottom >= platformY - GameConstants.PLATFORM_COLLISION_TOLERANCE &&
                playerBottom <= platformY + GameConstants.PLATFORM_COLLISION_TOLERANCE
            ) {
                return PlatformCollision(
                    position = Offset(position.x, platformY - size),
                    platform = platform
                )
            }
        }
        return null
    }

    private fun determinePlayerState(
        input: GameInput,
        isOnGround: Boolean,
        velocityY: Float = 0f
    ): PlayerState {
        return when {
            !isOnGround && velocityY < 0 -> PlayerState.JUMPING
            !isOnGround && velocityY > 0 -> PlayerState.FALLING
            input.left || input.right -> PlayerState.RUNNING
            else -> PlayerState.IDLE
        }
    }

    private data class PlatformCollision(
        val position: Offset,
        val platform: Platform
    )
}
