package dev.pgm.game.domain.usecase

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.domain.factory.ParticleFactory
import dev.pgm.game.domain.services.TimeProvider
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import kotlin.math.abs

/**
 * Use Case: Actualizar el estado del jugador basado en el input.
 * Responsabilidad única: Manejar toda la lógica relacionada con el movimiento y estado del jugador.
 */
class UpdatePlayerUseCase(
    private val timeProvider: TimeProvider,
    private val particleFactory: ParticleFactory
) {

    operator fun invoke(state: GameState, input: GameInput, deltaTime: Float): GameState {
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

        // Detectar caída al vacío
        val fellOffLeft = player.position.x + player.size < 0  // Completamente fuera por la izquierda
        val fellOffBottom = player.position.y > state.screenSize.height + 100f  // Cayó por debajo de la pantalla

        if (fellOffLeft || fellOffBottom) {
            // El jugador cayó al vacío - ejecutar lógica de muerte
            return handleFallDeath(state.copy(player = player))
        }

        return state.copy(player = player)
    }

    private fun handleFallDeath(state: GameState): GameState {
        val newLives = state.lives - 1
        val isGameOver = newLives <= 0

        return state.copy(
            player = state.player.copy(state = PlayerState.DEAD),
            lives = newLives,
            isGameOver = isGameOver,
            playerDeathTimestamp = timeProvider.currentTimeMillis(),
            barrels = emptyList(), // Limpiar todos los barriles
            particles = state.particles + particleFactory.createParticles(
                state.player.position.x + state.player.size / 2,  // Centro del jugador
                state.player.position.y + state.player.size / 2,
                ParticleFactory.ParticleType.DEATH
            )
        )
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

        // Límite horizontal solo por la derecha (permitir caída por la izquierda)
        if (newPosition.x > GameConstants.LEVEL_WIDTH - player.size) {
            newPosition = newPosition.copy(x = GameConstants.LEVEL_WIDTH - player.size)
        }

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
            val visualOffset = 2f  // Ajuste para que parezca estar sobre la superficie de la viga
            return player.copy(
                position = Offset(newX, topPlatformY - player.size + visualOffset),
                isClimbing = false,
                velocity = Offset.Zero,
                isOnGround = true,
                state = PlayerState.IDLE
            )
        }

        // Verificar si llegó a plataforma inferior
        if (input.down && playerBottom >= bottomPlatformY - climbTolerance) {
            val visualOffset = 2f  // Ajuste para que parezca estar sobre la superficie de la viga
            return player.copy(
                position = Offset(newX, bottomPlatformY - player.size + visualOffset),
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
                // Pequeño ajuste visual para que el jugador parezca estar sobre la superficie de la viga
                val visualOffset = 2f
                return PlatformCollision(
                    position = Offset(position.x, platformY - size + visualOffset),
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
