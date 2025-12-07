package dev.pgm.game

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import dev.pgm.game.model.utils.GameRect
import dev.pgm.game.model.utils.Particle
import dev.pgm.game.model.utils.ScorePopup
import kotlin.math.abs
import kotlin.random.Random

class GameEngine(private var state: GameState) {

    private var lastBarrelSpawnTime = System.currentTimeMillis()
    private var animationTimer = 0f

    fun getState(): GameState = state

    fun tick(deltaTime: Float, input: GameInput) {
        if (state.isGameOver || state.isWon || state.isPaused) return

        animationTimer += deltaTime

        state = checkAndRespawnPlayer()

        if (state.player.state != PlayerState.DEAD) {
            state = updatePlayer(input)
        } else {
            // Still update animation during death
            state = state.copy(player = updatePlayerAnimation(state.player))
        }

        state = updateDonkeyKong()
        state = updateBarrels()
        state = spawnBarrels()
        state = updateParticles(deltaTime)
        state = checkPlayerBarrelCollisions()
        state = checkWinCondition()
        state = cleanupPopups()
    }

    private fun updatePlayer(input: GameInput): GameState {
        var player = state.player
        player = updatePlayerAnimation(player)

        val nearLadder = findNearbyLadder(player, state.ladders)
        val shouldClimb = player.isClimbing || (nearLadder != null && (input.up || input.down))

        player = if (shouldClimb) {
            handlePlayerClimbing(player, input, nearLadder)
        } else {
            var updatedPlayer = handlePlayerHorizontalMovement(player, input)
            updatedPlayer = handlePlayerJumpingAndGravity(updatedPlayer, input)
            updatedPlayer = applyPlayerPlatformCollision(updatedPlayer)
            updatedPlayer
        }

        state = state.copy(player = player)
        return checkPlayerFallDeath()
    }

    private fun applyPlayerPlatformCollision(player: Player): Player {
        // No aplicar colision de plataforma si el jugador esta escalando
        if (player.isClimbing) {
            return player
        }
        
        var newPlayer = player
        var onAnyPlatform = false

        if (!newPlayer.isJumping) {
            for (platform in state.platforms) {
                if (isPlayerNearPlatform(newPlayer, platform)) {
                    val platformY = platform.getYAt(newPlayer.position.x + newPlayer.size / 2)
                    if (newPlayer.velocity.y >= 0 && newPlayer.position.y + newPlayer.size >= platformY - GameConstants.PLATFORM_COLLISION_TOLERANCE) {
                        newPlayer = newPlayer.copy(
                            position = Offset(newPlayer.position.x, platformY - newPlayer.size),
                            velocity = Offset(newPlayer.velocity.x, 0f),
                            isOnGround = true,
                            isJumping = false,
                            state = if (newPlayer.state == PlayerState.JUMPING || newPlayer.state == PlayerState.FALLING) PlayerState.IDLE else newPlayer.state
                        )
                        onAnyPlatform = true
                        break
                    }
                }
            }
        }

        if (!onAnyPlatform && newPlayer.isOnGround) {
            newPlayer = newPlayer.copy(isOnGround = false)
        }

        return newPlayer
    }

    private fun isPlayerNearPlatform(player: Player, platform: Platform): Boolean {
        val playerCenterX = player.position.x + player.size / 2
        return playerCenterX >= platform.left && playerCenterX <= platform.right
    }

    private fun updatePlayerAnimation(player: Player): Player {
        if (animationTimer > GameConstants.ANIMATION_FRAME_DURATION) {
            animationTimer = 0f
            val animation = CatAnimation.animations[player.state] ?: CatAnimation.animations[PlayerState.IDLE]!!
            val nextFrame = (player.animationFrame + 1) % animation.size
            return player.copy(animationFrame = nextFrame)
        }
        return player
    }

    private fun handlePlayerClimbing(player: Player, input: GameInput, nearLadder: Ladder?): Player {
        val ladder = nearLadder ?: return player.copy(isClimbing = false)

        if (input.jump) {
            return createJumpingPlayer(player)
        }

        val newX = ladder.centerX - player.size / 2
        val playerState = determineClimbingState(input)

        val topPlatform = state.platforms.getOrNull(ladder.topPlatformIndex)
        val bottomPlatform = state.platforms.getOrNull(ladder.bottomPlatformIndex)
        
        if (topPlatform == null || bottomPlatform == null) {
            return player.copy(isClimbing = false)
        }
        
        val playerCenterX = newX + player.size / 2
        val topTargetY = topPlatform.getYAt(playerCenterX) - player.size
        val bottomTargetY = bottomPlatform.getYAt(playerCenterX) - player.size

        var newY = player.position.y
        if (input.up) newY -= GameConstants.CLIMB_SPEED
        if (input.down) newY += GameConstants.CLIMB_SPEED

        // Si sube y llega al tope, colocar en plataforma superior
        if (input.up && newY <= topTargetY + GameConstants.CLIMB_SPEED) {
            return player.copy(
                position = Offset(newX, topTargetY),
                isClimbing = false,
                velocity = Offset.Zero,
                isOnGround = true,
                state = PlayerState.IDLE
            )
        }

        // Si baja y llega al fondo, colocar en plataforma inferior
        if (input.down && newY >= bottomTargetY - GameConstants.CLIMB_SPEED) {
            return player.copy(
                position = Offset(newX, bottomTargetY),
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
            state = playerState
        )
    }

    private fun calculateNewClimbingY(currentY: Float, input: GameInput): Float {
        var newY = currentY
        if (input.up) newY -= GameConstants.CLIMB_SPEED
        if (input.down) newY += GameConstants.CLIMB_SPEED
        return newY
    }

    private fun determineClimbingState(input: GameInput): PlayerState {
        return if (input.up || input.down) PlayerState.CLIMBING else PlayerState.IDLE
    }

    private fun createPlayerOnPlatform(player: Player, x: Float, platform: Platform): Player {
        val playerCenterX = x + player.size / 2
        val y = platform.getYAt(playerCenterX) - player.size
        return player.copy(
            position = Offset(x, y),
            isClimbing = false,
            velocity = Offset.Zero,
            isOnGround = true,
            state = PlayerState.IDLE
        )
    }

    private fun createClimbingPlayer(player: Player, x: Float, y: Float, playerState: PlayerState): Player {
        return player.copy(
            position = Offset(x, y),
            isClimbing = true,
            velocity = Offset.Zero,
            isOnGround = false,
            state = playerState
        )
    }

    private fun createJumpingPlayer(player: Player): Player {
        return player.copy(
            isClimbing = false,
            velocity = Offset(0f, GameConstants.JUMP_STRENGTH),
            isJumping = true,
            isOnGround = false,
            state = PlayerState.JUMPING
        )
    }

    private fun handlePlayerHorizontalMovement(player: Player, input: GameInput): Player {
        var newX = player.position.x
        var direction = player.direction
        var playerState = player.state

        if (input.left) {
            newX -= GameConstants.MOVE_SPEED
            direction = Direction.LEFT
            if (player.isOnGround) playerState = PlayerState.RUNNING
        }
        if (input.right) {
            newX += GameConstants.MOVE_SPEED
            direction = Direction.RIGHT
            if (player.isOnGround) playerState = PlayerState.RUNNING
        }
        if (!input.left && !input.right && player.isOnGround) {
            playerState = PlayerState.IDLE
        }

        // Permitir salir por los lados (sin coerceIn)
        return player.copy(position = Offset(newX, player.position.y), direction = direction, state = playerState)
    }

    private fun handlePlayerJumpingAndGravity(player: Player, input: GameInput): Player {
        var velocity = player.velocity
        var newPlayer = player

        if (input.jump && player.isOnGround && !player.isJumping) {
            velocity = Offset(velocity.x, GameConstants.JUMP_STRENGTH)
            newPlayer = newPlayer.copy(isJumping = true, isOnGround = false, state = PlayerState.JUMPING)
        }

        if (!player.isOnGround) {
            velocity = Offset(velocity.x, velocity.y + GameConstants.GRAVITY)
            if (!player.isClimbing) {
                newPlayer = if (velocity.y > 0) {
                    newPlayer.copy(state = PlayerState.FALLING)
                } else {
                    newPlayer.copy(state = PlayerState.JUMPING)
                }
            }
        }

        val newY = player.position.y + velocity.y
        return newPlayer.copy(position = Offset(player.position.x, newY), velocity = velocity)
    }

    private fun checkPlayerFallDeath(): GameState {
        val player = state.player
        // Muerte por caer abajo o salir por los lados
        val fellDown = player.position.y > state.screenSize.height
        val fellLeft = player.position.x + player.size < 0
        val fellRight = player.position.x > GameConstants.LEVEL_WIDTH
        
        if (fellDown || fellLeft || fellRight) {
            return handlePlayerDeathAndRespawn()
        }
        return state
    }

    // ========== BARREL LOGIC (DONKEY KONG STYLE) ==========

    private fun updateBarrels(): GameState {
        val updatedBarrels = state.barrels.mapNotNull { barrel -> updateBarrelMovement(barrel) }
        return state.copy(barrels = updatedBarrels)
    }

    private fun updateBarrelMovement(barrel: Barrel): Barrel? {
        // Eliminar barriles fuera de pantalla
        if (barrel.position.y > state.screenSize.height + GameConstants.BARREL_SCREEN_CLEANUP_OFFSET) {
            return null
        }

        // Actualizar rotación visual según dirección: positivo = derecha, negativo = izquierda
        val rotationSpeed = if (barrel.velocity.x != 0f) {
            barrel.velocity.x * GameConstants.BARREL_ROLL_SPEED
        } else {
            GameConstants.BARREL_LADDER_FALL_SPEED * GameConstants.BARREL_ROLL_SPEED
        }
        val newRotation = barrel.rotation + rotationSpeed

        return when {
            barrel.isOnLadder -> updateBarrelOnLadder(barrel, newRotation)
            barrel.isFalling -> updateBarrelFalling(barrel, newRotation)
            else -> updateBarrelOnPlatform(barrel, newRotation)
        }
    }

    /**
     * Barril bajando por una escalera (como en Donkey Kong original)
     */
    private fun updateBarrelOnLadder(barrel: Barrel, rotation: Float): Barrel {
        val fallSpeed = GameConstants.BARREL_LADDER_FALL_SPEED
        val newY = barrel.position.y + fallSpeed

        println("DEBUG LADDER: barrel en escalera, currentPlatformIndex=${barrel.currentPlatformIndex}, targetPlatformIndex=${barrel.targetPlatformIndex}, newY=$newY")

        // Buscar la plataforma inferior donde debe aterrizar
        val targetPlatformIndex = barrel.targetPlatformIndex ?: (barrel.currentPlatformIndex - 1)
        if (targetPlatformIndex >= 0 && targetPlatformIndex < state.platforms.size) {
            val targetPlatform = state.platforms[targetPlatformIndex]
            val platformY = targetPlatform.getYAt(barrel.centerX)

            println("DEBUG LADDER: targetPlatformIndex=$targetPlatformIndex, platformY=$platformY, barrelBottom=${newY + barrel.size}")

            // Verificar si el barril ha llegado a la plataforma
            if (newY + barrel.size >= platformY) {
                println("DEBUG LADDER: barril aterriza en plataforma $targetPlatformIndex")
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
    private fun updateBarrelFalling(barrel: Barrel, rotation: Float): Barrel {
        val fallSpeed = GameConstants.BARREL_FREE_FALL_SPEED
        val newY = barrel.position.y + fallSpeed

        // Buscar colisión con cualquier plataforma inferior
        for (platform in state.platforms) {
            if (platform.index >= barrel.currentPlatformIndex) continue
            
            // Verificar si el barril está dentro del rango horizontal de la plataforma
            if (barrel.centerX >= platform.left && barrel.centerX <= platform.right) {
                val platformY = platform.getYAt(barrel.centerX)
                
                // Verificar si el barril ha llegado a esta plataforma
                if (barrel.bottom < platformY && newY + barrel.size >= platformY) {
                    val newDirection = getBarrelDirectionForPlatform(platform.index)
                    return barrel.copy(
                        position = Offset(barrel.position.x, platformY - barrel.size),
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
            position = Offset(barrel.position.x, newY),
            rotation = rotation
        )
    }

    /**
     * Barril rodando sobre una plataforma (lógica principal estilo Donkey Kong)
     */
    private fun updateBarrelOnPlatform(barrel: Barrel, rotation: Float): Barrel {
        val platform = state.platforms.getOrNull(barrel.currentPlatformIndex) ?: return barrel

        // Calcular nueva posición X
        val newX = barrel.position.x + barrel.velocity.x

        // Calcular Y según la pendiente de la plataforma
        val newY = platform.getYAt(newX + barrel.size / 2) - barrel.size

        // PRIMERO verificar si el barril está sobre una escalera (puede bajar)
        if (platform.index >= 4) {
            println("DEBUG PLATFORM: barril en plataforma ${platform.index}, x=$newX, centerX=${newX + barrel.size/2}")
        }
        val ladderBelow = findLadderBelowBarrel(barrel.copy(position = Offset(newX, newY)), platform)
        if (ladderBelow != null && barrel.lastLadderChecked != ladderBelow.hashCode()) {
            // Probabilidad aleatoria de bajar por la escalera (como en DK original)
            val randomValue = Random.nextFloat()
            println("DEBUG: Escalera encontrada! random=$randomValue, threshold=${GameConstants.BARREL_LADDER_PROBABILITY}")
            if (randomValue < GameConstants.BARREL_LADDER_PROBABILITY) {
                println("DEBUG: BARRIL VA A BAJAR POR ESCALERA!")
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
        val atLeftEdge = newX <= platform.left
        val atRightEdge = newX + barrel.size >= platform.right

        if (atLeftEdge || atRightEdge) {
            return handleBarrelAtEdge(barrel, platform, atLeftEdge, rotation)
        }

        return barrel.copy(
            position = Offset(newX, newY),
            rotation = rotation
        )
    }

    /**
     * Busca una escalera debajo del barril en la plataforma actual
     */
    private fun findLadderBelowBarrel(barrel: Barrel, platform: Platform): Ladder? {
        for (ladder in state.ladders) {
            val matchesPlatform = ladder.topPlatformIndex == platform.index
            val distance = abs(barrel.centerX - ladder.centerX)
            val withinTolerance = distance < GameConstants.BARREL_OVER_LADDER_TOLERANCE
            
            if (platform.index >= 4) {
                println("DEBUG FIND: platform=${platform.index}, ladder.topIndex=${ladder.topPlatformIndex}, barrelCenterX=${barrel.centerX}, ladderCenterX=${ladder.centerX}, distance=$distance, tolerance=${GameConstants.BARREL_OVER_LADDER_TOLERANCE}, match=$matchesPlatform, within=$withinTolerance")
            }
            
            if (matchesPlatform && withinTolerance) {
                return ladder
            }
        }
        return null
    }

    /**
     * Maneja cuando el barril llega al borde de una plataforma
     */
    private fun handleBarrelAtEdge(barrel: Barrel, platform: Platform, atLeftEdge: Boolean, rotation: Float): Barrel {
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
        val ladderAtEdge = findLadderAtEdge(platform, atLeftEdge)
        
        return if (ladderAtEdge != null) {
            // Bajar por la escalera del borde
            barrel.copy(
                position = Offset(ladderAtEdge.centerX - barrel.size / 2, barrel.position.y),
                isOnLadder = true,
                velocity = Offset(0f, GameConstants.BARREL_LADDER_FALL_SPEED),
                rotation = rotation
            )
        } else {
            // Caída libre al borde
            barrel.copy(
                isFalling = true,
                currentPlatformIndex = nextPlatformIndex,
                velocity = Offset(0f, GameConstants.BARREL_FREE_FALL_SPEED),
                rotation = rotation
            )
        }
    }

    /**
     * Busca una escalera en el borde de la plataforma
     */
    private fun findLadderAtEdge(platform: Platform, atLeftEdge: Boolean): Ladder? {
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

    private fun spawnBarrels(): GameState {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBarrelSpawnTime > GameConstants.BARREL_SPAWN_INTERVAL) {
            lastBarrelSpawnTime = currentTime

            val dkPlatform = state.platforms[5]
            val spawnX = state.enemy.position.x + state.enemy.size
            val spawnY = dkPlatform.getYAt(spawnX) - GameConstants.BARREL_SIZE

            println("DEBUG SPAWN: Spawning barrel at x=$spawnX, y=$spawnY, platformIndex=${dkPlatform.index}")

            val newBarrel = Barrel(
                position = Offset(spawnX, spawnY),
                velocity = Offset(GameConstants.BARREL_SPEED, 0f),
                currentPlatformIndex = dkPlatform.index,
                size = GameConstants.BARREL_SIZE
            )

            return state.copy(
                barrels = state.barrels + newBarrel,
                particles = state.particles + createSpawnParticles(Offset(spawnX, spawnY))
            )
        }
        return state
    }

    private fun checkPlayerBarrelCollisions(): GameState {
        if (state.player.isInvincible) return state

        val playerHitbox = state.player.hitbox
        for (barrel in state.barrels) {
            if (didPlayerJumpOverBarrel(playerHitbox, barrel)) {
                return handlePlayerJumpOverBarrel(barrel)
            }
            if (playerHitbox.overlaps(barrel.hitbox)) {
                return handlePlayerDeathAndRespawn()
            }
        }
        return state
    }

    private fun didPlayerJumpOverBarrel(playerHitbox: GameRect, barrel: Barrel): Boolean {
        return !barrel.hasBeenJumped &&
                state.player.isJumping &&
                state.player.velocity.y >= 0 &&
                playerHitbox.bottom >= barrel.hitbox.top - GameConstants.BARREL_JUMP_TOLERANCE_TOP &&
                playerHitbox.bottom <= barrel.hitbox.top + GameConstants.BARREL_JUMP_TOLERANCE_BOTTOM &&
                playerHitbox.right > barrel.hitbox.left &&
                playerHitbox.left < barrel.hitbox.right
    }

    private fun handlePlayerJumpOverBarrel(barrel: Barrel): GameState {
        return state.copy(
            score = state.score + GameConstants.POINTS_JUMP_BARREL,
            barrels = state.barrels.map { if (it === barrel) it.copy(hasBeenJumped = true) else it },
            particles = state.particles + createScoreParticles(barrel.position),
            lastScorePopup = ScorePopup(barrel.position, GameConstants.POINTS_JUMP_BARREL)
        )
    }

    private fun handlePlayerDeathAndRespawn(): GameState {
        val newLives = state.lives - 1
        val deathParticles = createDeathParticles(state.player.position)

        if (newLives <= 0) {
            return state.copy(
                isGameOver = true,
                lives = 0,
                highScore = maxOf(state.highScore, state.score),
                particles = state.particles + deathParticles,
                player = state.player.copy(state = PlayerState.DEAD, animationFrame = 0)
            )
        }

        return state.copy(
            player = state.player.copy(state = PlayerState.DEAD, animationFrame = 0),
            playerDeathTimestamp = System.currentTimeMillis(),
            particles = state.particles + deathParticles
        )
    }

    private fun checkWinCondition(): GameState {
        val playerHitbox = state.player.hitbox
        val princessHitbox = state.winObjetive.hitbox

        if (playerHitbox.overlaps(princessHitbox)) {
            val finalScore = state.score + GameConstants.POINTS_WIN
            return state.copy(
                isWon = true,
                score = finalScore,
                highScore = maxOf(state.highScore, finalScore),
                particles = state.particles + createWinParticles(state.winObjetive.position)
            )
        }
        return state
    }

    private fun findNearbyLadder(player: Player, ladders: List<Ladder>): Ladder? {
        val playerCenterX = player.position.x + player.size / 2
        val playerTop = player.position.y
        val playerBottom = player.position.y + player.size

        return ladders.find { ladder ->
            val horizontalClose = abs(playerCenterX - ladder.centerX) < GameConstants.LADDER_HORIZONTAL_TOLERANCE
            // Permitir un margen extra arriba y abajo para no perder la escalera al llegar al final
            val verticalMargin = player.size * 0.5f
            val onLadderVertical = playerBottom > ladder.top - verticalMargin && playerTop < ladder.bottom + verticalMargin
            horizontalClose && onLadderVertical
        }
    }

    private fun updateDonkeyKong(): GameState {
        val time = System.currentTimeMillis()
        val isThrowing = time - lastBarrelSpawnTime < GameConstants.ENEMY_ANIMATION_INTERVAL_MS
        return state.copy(
            enemy = state.enemy.copy(
                animationFrame = ((time / GameConstants.ENEMY_ANIMATION_INTERVAL_MS) % 2).toInt(),
                isThrowingBarrel = isThrowing
            )
        )
    }

    private fun updateParticles(deltaTime: Float): GameState {
        val updated = state.particles.mapNotNull { p ->
            val newAge = p.age + deltaTime
            if (newAge >= p.lifetime) null
            else p.copy(
                position = p.position.plus(p.velocity.times(deltaTime)),
                velocity = p.velocity.copy(y = p.velocity.y + GameConstants.PARTICLE_GRAVITY * deltaTime),
                age = newAge
            )
        }
        return state.copy(particles = updated)
    }

    private fun checkAndRespawnPlayer(): GameState {
        if (state.playerDeathTimestamp > 0 && System.currentTimeMillis() - state.playerDeathTimestamp > 1500) { // 1.5s death animation
            val newLives = state.lives - 1
            if (newLives <= 0) {
                return state.copy(isGameOver = true, lives = 0, highScore = maxOf(state.highScore, state.score))
            }

            val startPlatform = state.platforms.first()
            return state.copy(
                lives = newLives,
                player = Player(
                    position = Offset(50f, startPlatform.getYAt(50f) - GameConstants.PLAYER_SIZE),
                    size = GameConstants.PLAYER_SIZE,
                    isOnGround = true,
                    invincibleUntil = System.currentTimeMillis() + GameConstants.INVINCIBILITY_TIME
                ),
                barrels = emptyList(),
                playerDeathTimestamp = 0
            )
        }
        return state
    }

    private fun cleanupPopups(): GameState {
        return if (state.lastScorePopup?.isExpired == true) state.copy(lastScorePopup = null) else state
    }

    private fun createParticles(
        position: Offset,
        count: Int,
        xRange: Float,
        yRange: Float,
        sizeRange: Float,
        lifetime: Float,
        color: Long
    ): List<Particle> {
        return List(count) {
            val velX = (Random.nextFloat() * 2 - 1) * xRange
            val velY = Random.nextFloat() * yRange
            val size = Random.nextFloat() * sizeRange + 2f
            Particle(position, Offset(velX, velY), color, size, lifetime)
        }
    }

    private fun createSpawnParticles(pos: Offset) = createParticles(
        pos,
        GameConstants.SPAWN_PARTICLE_COUNT,
        GameConstants.SPAWN_PARTICLE_VEL_X_RANGE,
        GameConstants.SPAWN_PARTICLE_VEL_Y_RANGE,
        GameConstants.SPAWN_PARTICLE_SIZE_RANGE,
        GameConstants.SPAWN_PARTICLE_LIFETIME,
        0xFF8B4513
    )

    private fun createScoreParticles(pos: Offset) = createParticles(
        pos,
        GameConstants.SCORE_PARTICLE_COUNT,
        GameConstants.SCORE_PARTICLE_VEL_X_RANGE,
        GameConstants.SCORE_PARTICLE_VEL_Y_RANGE,
        GameConstants.SCORE_PARTICLE_SIZE_RANGE,
        GameConstants.SCORE_PARTICLE_LIFETIME,
        0xFFFFD700
    )

    private fun createDeathParticles(pos: Offset) = createParticles(
        pos,
        GameConstants.DEATH_PARTICLE_COUNT,
        GameConstants.DEATH_PARTICLE_VEL_X_RANGE,
        GameConstants.DEATH_PARTICLE_VEL_Y_RANGE,
        GameConstants.DEATH_PARTICLE_SIZE_RANGE,
        GameConstants.DEATH_PARTICLE_LIFETIME,
        0xFFFF4444
    )

    private fun createWinParticles(pos: Offset) = createParticles(
        pos,
        GameConstants.WIN_PARTICLE_COUNT,
        GameConstants.WIN_PARTICLE_VEL_X_RANGE,
        GameConstants.WIN_PARTICLE_VEL_Y_RANGE,
        GameConstants.WIN_PARTICLE_SIZE_RANGE,
        GameConstants.WIN_PARTICLE_LIFETIME,
        listOf(0xFFFFD700L, 0xFFFF69B4L, 0xFF00FF00L, 0xFF00BFFFL).random()
    )

    fun reset() {
        val highScore = state.highScore
        state = GameState.initial(state.screenSize).copy(highScore = highScore)
        lastBarrelSpawnTime = System.currentTimeMillis()
        animationTimer = 0f
    }

    fun togglePause() {
        state = state.copy(isPaused = !state.isPaused)
    }
}
