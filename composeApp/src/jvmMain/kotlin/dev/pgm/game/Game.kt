package dev.pgm.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.CatAnimation
import dev.pgm.game.model.entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

fun main() = application {
    var gameState by remember { mutableStateOf(GameState.initial(IntSize(800, 700))) }
    var input by remember { mutableStateOf(GameInput()) }
    var isPaused by remember { mutableStateOf(false) }
    var lastBarrelSpawn by remember { mutableStateOf(System.currentTimeMillis()) }
    var animationTimer by remember { mutableStateOf(0f) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Cat Jump Barrels",
        state = WindowState(size = DpSize(850.dp, 750.dp)),
        resizable = true,
        onKeyEvent = { event ->
            when (event.type) {
                KeyEventType.KeyDown -> {
                    when (event.key) {
                        Key.R -> {
                            if (gameState.isGameOver || gameState.isWon) {
                                val highScore = gameState.highScore
                                gameState = GameState.initial(gameState.screenSize).copy(highScore = highScore)
                                input = GameInput()
                                isPaused = false
                                lastBarrelSpawn = System.currentTimeMillis()
                            }
                            true
                        }
                        Key.P, Key.Escape -> {
                            if (!gameState.isGameOver && !gameState.isWon) {
                                isPaused = !isPaused
                            }
                            true
                        }
                        Key.DirectionLeft, Key.A -> {
                            input = input.copy(left = true)
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            input = input.copy(right = true)
                            true
                        }
                        Key.DirectionUp, Key.W -> {
                            input = input.copy(up = true)
                            true
                        }
                        Key.DirectionDown, Key.S -> {
                            input = input.copy(down = true)
                            true
                        }
                        Key.Spacebar -> {
                            input = input.copy(jump = true)
                            true
                        }
                        else -> false
                    }
                }
                KeyEventType.KeyUp -> {
                    when (event.key) {
                        Key.DirectionLeft, Key.A -> { input = input.copy(left = false); true }
                        Key.DirectionRight, Key.D -> { input = input.copy(right = false); true }
                        Key.DirectionUp, Key.W -> { input = input.copy(up = false); true }
                        Key.DirectionDown, Key.S -> { input = input.copy(down = false); true }
                        Key.Spacebar -> { input = input.copy(jump = false); true }
                        else -> false
                    }
                }
                else -> false
            }
        }
    ) {
        // Game loop
        LaunchedEffect(Unit) {
            var lastFrameTime = System.currentTimeMillis()
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastFrameTime) / 1000f
                lastFrameTime = currentTime

                if (!gameState.isGameOver && !gameState.isWon && !isPaused) {
                    var newState = gameState

                    newState = checkAndRespawnPlayer(newState)

                    if (newState.player.state == PlayerState.DEAD) {
                        val (p, t) = updatePlayerAnimation(newState.player, animationTimer + deltaTime)
                        animationTimer = t
                        newState = newState.copy(player = p)
                    } else {
                        val (p, t) = updatePlayerAnimation(newState.player, animationTimer + deltaTime)
                        animationTimer = t
                        newState = newState.copy(player = p)

                        newState = updatePlayer(newState, input)
                        newState = updateBarrels(newState)

                        val now = System.currentTimeMillis()
                        if (now - lastBarrelSpawn > GameConstants.BARREL_SPAWN_INTERVAL) {
                            lastBarrelSpawn = now
                            newState = spawnBarrel(newState)
                        }

                        newState = checkCollisions(newState)
                        newState = checkWin(newState)
                    }
                    gameState = newState
                }

                delay(16)
            }
        }

        MaterialTheme(colors = darkColors()) {
            GameRenderer(
                state = gameState.copy(isPaused = isPaused),
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        if (size.width > 0 && size.height > 0 &&
                            (size.width != gameState.screenSize.width || size.height != gameState.screenSize.height)) {
                            val highScore = gameState.highScore
                            gameState = GameState.initial(size).copy(highScore = highScore)
                        }
                    }
            )
        }
    }
}

// Necesitamos el composable vacío para compatibilidad
@Composable
fun CatJumpBarrels() {
    // Este composable ya no se usa, toda la lógica está en main()
}

// ========== PLAYER ==========

private fun updatePlayer(state: GameState, input: GameInput): GameState {
    var player = state.player

    val nearLadder = findNearbyLadder(player, state.ladders)

    // Entrar en modo climbing solo si hay escalera Y se presiona arriba/abajo
    // O si ya está climbing y sigue en una escalera
    val shouldClimb = if (player.isClimbing) {
        nearLadder != null  // Continuar climbing mientras haya escalera
    } else {
        nearLadder != null && (input.up || input.down)  // Iniciar climbing
    }

    player = if (shouldClimb) {
        handleClimbing(player, input, nearLadder, state.platforms)
    } else {
        // Si estaba climbing pero ya no hay escalera o no presiona teclas, salir del modo
        if (player.isClimbing) {
            player = player.copy(isClimbing = false, velocity = Offset(0f, 0f))
        }
        var p = handleHorizontalMovement(player, input)
        p = handleJumpAndGravity(p, input)
        p
    }

    player = applyPlatformCollision(player, state.platforms)

    var newState = state.copy(player = player)
    if (player.position.y > state.screenSize.height) {
        newState = handleDeath(newState)
    }

    return newState
}

private fun findNearbyLadder(player: Player, ladders: List<Ladder>): Ladder? {
    val playerCenterX = player.position.x + player.size / 2
    val playerTop = player.position.y
    val playerBottom = player.position.y + player.size

    return ladders.find { ladder ->
        // Verificar si el jugador está horizontalmente cerca de la escalera
        val horizontalClose = abs(playerCenterX - ladder.centerX) < GameConstants.LADDER_HORIZONTAL_TOLERANCE

        // Verificar si el jugador está verticalmente cerca de la escalera
        // Debe poder usar la escalera si:
        // 1. Está sobre la escalera (entre top y bottom)
        // 2. Está parado en la plataforma inferior (cerca del bottom) - PARA SUBIR
        // 3. Está parado en la plataforma superior (cerca del top) - PARA BAJAR

        val onLadder = playerBottom > ladder.top && playerTop < ladder.bottom
        val nearBottom = abs(playerBottom - ladder.bottom) < 30f  // Tolerancia para plataforma inferior
        val nearTop = abs(playerBottom - ladder.top) < 30f  // Tolerancia para plataforma superior

        horizontalClose && (onLadder || nearBottom || nearTop)
    }
}

private fun handleClimbing(player: Player, input: GameInput, ladder: Ladder?, platforms: List<Platform>): Player {
    if (ladder == null) return player.copy(isClimbing = false, velocity = Offset.Zero)

    // Permitir saltar desde la escalera
    if (input.jump) {
        return player.copy(
            isClimbing = false,
            velocity = Offset(0f, GameConstants.JUMP_STRENGTH),
            isJumping = true,
            isOnGround = false,
            state = PlayerState.JUMPING
        )
    }

    // PERMITIR MOVIMIENTO HORIZONTAL en la escalera (estilo Donkey Kong)
    var newX = player.position.x
    if (input.left) {
        newX -= GameConstants.MOVE_SPEED
    }
    if (input.right) {
        newX += GameConstants.MOVE_SPEED
    }

    // Si el jugador se aleja demasiado horizontalmente de la escalera, salir del modo climbing
    val playerCenterX = newX + player.size / 2
    if (abs(playerCenterX - ladder.centerX) > GameConstants.LADDER_HORIZONTAL_TOLERANCE + 5f) {
        return player.copy(
            isClimbing = false,
            velocity = Offset(0f, 0f),
            isOnGround = false,
            state = PlayerState.JUMPING
        )
    }

    var newY = player.position.y

    // Si el jugador acaba de entrar en modo climbing desde el suelo
    if (!player.isClimbing && player.isOnGround) {
        val playerBottom = player.position.y + player.size
        // Si está cerca del bottom de la escalera (plataforma inferior) y presiona UP
        if (input.up && abs(playerBottom - ladder.bottom) < 30f) {
            newY = ladder.bottom - player.size - 5f
        }
        // Si está cerca del top de la escalera (plataforma superior) y presiona DOWN
        else if (input.down && abs(playerBottom - ladder.top) < 30f) {
            newY = ladder.top + 5f
        }
    }

    // Obtener plataformas superior e inferior ANTES de modificar newY
    val topPlatform = platforms.getOrNull(ladder.topPlatformIndex)
    val bottomPlatform = platforms.getOrNull(ladder.bottomPlatformIndex)

    // VERIFICAR PRIMERO si está llegando a una plataforma ANTES de mover
    // Esto es CRÍTICO para que funcione correctamente

    // Verificar plataforma SUPERIOR (cuando sube)
    if (topPlatform != null && input.up) {
        val topPlatformY = topPlatform.getYAt(playerCenterX)
        val currentBottom = player.position.y + player.size

        // Si está dentro del rango horizontal de la plataforma
        if (playerCenterX >= topPlatform.left && playerCenterX <= topPlatform.right) {
            // Si el bottom del jugador está CERCA de la superficie de la plataforma
            // Tolerancia amplia: de 20px abajo a 5px arriba de la plataforma
            if (currentBottom >= topPlatformY - 20f && currentBottom <= topPlatformY + 5f) {
                // SUBIR A LA PLATAFORMA
                return player.copy(
                    position = Offset(newX, topPlatformY - player.size),
                    isClimbing = false,
                    velocity = Offset.Zero,
                    isOnGround = true,
                    state = PlayerState.IDLE
                )
            }
        }
    }

    // Verificar plataforma INFERIOR (cuando baja)
    if (bottomPlatform != null && input.down) {
        val bottomPlatformY = bottomPlatform.getYAt(playerCenterX)
        val currentBottom = player.position.y + player.size

        // Si está dentro del rango horizontal de la plataforma
        if (playerCenterX >= bottomPlatform.left && playerCenterX <= bottomPlatform.right) {
            // Si el bottom del jugador está CERCA de la superficie de la plataforma
            if (currentBottom >= bottomPlatformY - 5f && currentBottom <= bottomPlatformY + 20f) {
                // BAJAR A LA PLATAFORMA
                return player.copy(
                    position = Offset(newX, bottomPlatformY - player.size),
                    isClimbing = false,
                    velocity = Offset.Zero,
                    isOnGround = true,
                    state = PlayerState.IDLE
                )
            }
        }
    }

    // Si NO llegó a ninguna plataforma, continuar con el movimiento vertical
    if (input.up) {
        newY -= GameConstants.CLIMB_SPEED
    } else if (input.down) {
        newY += GameConstants.CLIMB_SPEED
    }

    // Limitar movimiento dentro de los límites de la escalera
    newY = newY.coerceIn(ladder.top - 5f, ladder.bottom - player.size + 5f)
    newX = newX.coerceIn(0f, GameConstants.LEVEL_WIDTH - player.size)

    return player.copy(
        position = Offset(newX, newY),
        isClimbing = true,
        velocity = Offset.Zero,
        isOnGround = false,
        state = if (input.up || input.down) PlayerState.CLIMBING else PlayerState.IDLE
    )
}

private fun handleHorizontalMovement(player: Player, input: GameInput): Player {
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

    newX = newX.coerceIn(0f, GameConstants.LEVEL_WIDTH - player.size)
    return player.copy(position = Offset(newX, player.position.y), direction = direction, state = playerState)
}

private fun handleJumpAndGravity(player: Player, input: GameInput): Player {
    var velocity = player.velocity
    var p = player

    if (input.jump && player.isOnGround && !player.isJumping) {
        velocity = Offset(velocity.x, GameConstants.JUMP_STRENGTH)
        p = p.copy(isJumping = true, isOnGround = false, state = PlayerState.JUMPING)
    }

    if (!player.isOnGround) {
        velocity = Offset(velocity.x, velocity.y + GameConstants.GRAVITY)
        if (!player.isClimbing) {
            p = if (velocity.y > 0) {
                p.copy(state = PlayerState.FALLING)
            } else {
                p.copy(state = PlayerState.JUMPING)
            }
        }
    }

    val newY = player.position.y + velocity.y
    return p.copy(position = Offset(player.position.x, newY), velocity = velocity)
}

private fun applyPlatformCollision(player: Player, platforms: List<Platform>): Player {
    var newPlayer = player

    if (newPlayer.velocity.y >= 0) {
        for (platform in platforms) {
            val playerCenterX = newPlayer.position.x + newPlayer.size / 2
            if (playerCenterX >= platform.left && playerCenterX <= platform.right) {
                val platformY = platform.getYAt(playerCenterX)
                val playerBottom = newPlayer.position.y + newPlayer.size

                if (playerBottom >= platformY - 5f && playerBottom <= platformY + 15f) {
                    return newPlayer.copy(
                        position = Offset(newPlayer.position.x, platformY - newPlayer.size),
                        velocity = Offset(newPlayer.velocity.x, 0f),
                        isOnGround = true,
                        isJumping = false,
                        state = if (newPlayer.state == PlayerState.JUMPING || newPlayer.state == PlayerState.FALLING) PlayerState.IDLE else newPlayer.state
                    )
                }
            }
        }
    }

    if (newPlayer.isOnGround && !newPlayer.isClimbing) {
        val playerCenterX = newPlayer.position.x + newPlayer.size / 2
        val onPlatform = platforms.any { platform ->
            playerCenterX >= platform.left && playerCenterX <= platform.right &&
                    abs((newPlayer.position.y + newPlayer.size) - platform.getYAt(playerCenterX)) < 5f
        }
        if (!onPlatform) {
            newPlayer = newPlayer.copy(isOnGround = false)
        }
    }

    return newPlayer
}

private fun handleDeath(state: GameState): GameState {
    val newLives = state.lives - 1

    if (newLives <= 0) {
        return state.copy(
            isGameOver = true,
            lives = 0,
            highScore = maxOf(state.highScore, state.score),
            player = state.player.copy(state = PlayerState.DEAD, animationFrame = 0)
        )
    }

    return state.copy(
        player = state.player.copy(state = PlayerState.DEAD, animationFrame = 0, velocity = Offset.Zero),
        playerDeathTimestamp = System.currentTimeMillis()
    )
}

private fun checkAndRespawnPlayer(state: GameState): GameState {
    if (state.playerDeathTimestamp > 0 && System.currentTimeMillis() - state.playerDeathTimestamp > 1500) { // 1.5s death animation
        val newLives = state.lives - 1
        if (newLives < 0) {
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
            playerDeathTimestamp = 0L
        )
    }
    return state
}

private fun updatePlayerAnimation(player: Player, animationTimer: Float): Pair<Player, Float> {
    var newTimer = animationTimer
    if (animationTimer > GameConstants.ANIMATION_FRAME_DURATION) {
        newTimer = 0f
        val animation = CatAnimation.animations[player.state] ?: CatAnimation.animations[PlayerState.IDLE]!!
        if (animation.isEmpty()) {
            return Pair(player, newTimer) // Evitar división por cero
        }
        val nextFrame = (player.animationFrame + 1) % animation.size
        return Pair(player.copy(animationFrame = nextFrame), newTimer)
    }
    return Pair(player, newTimer)
}

// ========== BARRELS ==========

private fun updateBarrels(state: GameState): GameState {
    val updatedBarrels = state.barrels.mapNotNull { barrel ->
        updateSingleBarrel(barrel, state.platforms, state.ladders, state.screenSize.height)
    }
    return state.copy(barrels = updatedBarrels)
}

private fun updateSingleBarrel(barrel: Barrel, platforms: List<Platform>, ladders: List<Ladder>, screenHeight: Int): Barrel? {
    if (barrel.position.y > screenHeight + 50) return null

    // Rotar según la dirección: positivo = derecha, negativo = izquierda
    val rotationSpeed = if (barrel.velocity.x != 0f) barrel.velocity.x * 0.1f else 0.3f
    val newRotation = barrel.rotation + rotationSpeed

    return when {
        barrel.isOnLadder -> moveBarrelDownLadder(barrel, platforms, newRotation)
        barrel.isFalling -> moveBarrelFalling(barrel, platforms, newRotation)
        else -> moveBarrelOnPlatform(barrel, platforms, ladders, newRotation)
    }
}

// ========== FUNCIONES AUXILIARES ESTILO ATARI 2600 ==========

/**
 * Comprueba si hay plataforma sólida justo debajo del barril (1-2px)
 * Chequea tanto el borde inferior izquierdo como el derecho
 */
private fun hasPlatformBelow(barrel: Barrel, platforms: List<Platform>, checkDistance: Float = 2f): Platform? {
    val barrelBottom = barrel.position.y + barrel.size
    val leftX = barrel.position.x + 2f  // Borde inferior izquierdo
    val rightX = barrel.position.x + barrel.size - 2f  // Borde inferior derecho

    for (platform in platforms) {
        // Comprobar si alguno de los dos puntos está sobre la plataforma
        for (checkX in listOf(leftX, rightX)) {
            if (checkX >= platform.left && checkX <= platform.right) {
                val platformY = platform.getYAt(checkX)
                // Verificar si está justo encima (dentro de checkDistance píxeles)
                if (barrelBottom >= platformY - checkDistance && barrelBottom <= platformY + checkDistance) {
                    return platform
                }
            }
        }
    }
    return null
}

/**
 * Ajusta el barril verticalmente para que se apoye en la rampa/plataforma
 */
private fun adjustToRampSurface(barrel: Barrel, platform: Platform): Barrel {
    val barrelCenterX = barrel.position.x + barrel.size / 2
    val platformY = platform.getYAt(barrelCenterX)
    val correctY = platformY - barrel.size

    return barrel.copy(position = Offset(barrel.position.x, correctY))
}

private fun moveBarrelDownLadder(barrel: Barrel, platforms: List<Platform>, rotation: Float): Barrel {
    // MECÁNICA ATARI 2600: Caída vertical por escalera, comprobar colisión pixel a pixel
    val fallSpeed = GameConstants.BARREL_LADDER_FALL_SPEED
    val newY = barrel.position.y + fallSpeed

    // Si tenemos targetPlatformIndex, usarlo para saber exactamente dónde aterrizar
    val targetPlatformIndex = barrel.targetPlatformIndex ?: -1
    if (targetPlatformIndex >= 0 && targetPlatformIndex < platforms.size) {
        val targetPlatform = platforms[targetPlatformIndex]
        val barrelCenterX = barrel.position.x + barrel.size / 2

        // Verificar si el centro del barril está dentro del rango de la plataforma
        if (barrelCenterX >= targetPlatform.left && barrelCenterX <= targetPlatform.right) {
            val platformY = targetPlatform.getYAt(barrelCenterX)

            // Verificar si hemos llegado a la plataforma
            if (newY + barrel.size >= platformY) {
                // ¡Aterrizar en la plataforma!
                val newDirection = if (targetPlatform.index % 2 == 0) 1f else -1f

                return barrel.copy(
                    position = Offset(barrel.position.x, platformY - barrel.size),
                    velocity = Offset(GameConstants.BARREL_SPEED * newDirection, 0f),
                    isOnLadder = false,
                    isFalling = false,
                    currentPlatformIndex = targetPlatform.index,
                    rotation = rotation,
                    lastLadderChecked = -1,
                    targetPlatformIndex = null
                )
            }
        }
    }

    // No encontró plataforma, continuar bajando por la escalera VERTICALMENTE
    return barrel.copy(
        position = Offset(barrel.position.x, newY),
        velocity = Offset(0f, fallSpeed),
        rotation = rotation,
        isOnLadder = true
    )
}

private fun moveBarrelFalling(barrel: Barrel, platforms: List<Platform>, rotation: Float): Barrel {
    // MECÁNICA ATARI 2600: Caída VERTICAL, comprobar colisión pixel a pixel
    // Movemos en pasos pequeños (2px) para no atravesar plataformas

    val fallStep = 2f  // Caer solo 2px por iteración (evita atravesar)
    var currentY = barrel.position.y
    val targetY = barrel.position.y + GameConstants.BARREL_FREE_FALL_SPEED

    // Caer pixel a pixel (o cada 2px) hasta llegar al objetivo
    while (currentY < targetY) {
        currentY += fallStep

        // Crear barril temporal en esta posición para chequear
        val tempBarrel = barrel.copy(position = Offset(barrel.position.x, currentY))

        // COMPROBAR si 1-2px debajo hay plataforma
        val platformBelow = hasPlatformBelow(tempBarrel, platforms, checkDistance = 2f)

        if (platformBelow != null) {
            // ¡HAY PLATAFORMA! Aterrizar y cambiar a movimiento HORIZONTAL
            // Determinar dirección según índice de plataforma
            // Plataformas PARES (0, 2, 4) -> derecha (+1)
            // Plataformas IMPARES (1, 3, 5) -> izquierda (-1)
            val newDirection = if (platformBelow.index % 2 == 0) 1f else -1f

            // Ajustar a la superficie de la rampa
            val adjustedBarrel = adjustToRampSurface(
                barrel.copy(position = Offset(barrel.position.x, currentY)),
                platformBelow
            )

            return adjustedBarrel.copy(
                velocity = Offset(GameConstants.BARREL_SPEED * newDirection, 0f),
                isFalling = false,
                isOnLadder = false,
                currentPlatformIndex = platformBelow.index,
                rotation = rotation,
                lastLadderChecked = -1
            )
        }
    }

    // No encontró plataforma, continuar cayendo VERTICALMENTE
    return barrel.copy(
        position = Offset(barrel.position.x, currentY),
        velocity = Offset(0f, GameConstants.BARREL_FREE_FALL_SPEED),
        rotation = rotation
    )
}

private fun moveBarrelOnPlatform(barrel: Barrel, platforms: List<Platform>, ladders: List<Ladder>, rotation: Float): Barrel {
    val platform = platforms.getOrNull(barrel.currentPlatformIndex) ?: return barrel

    // PASO 1: ANTES de moverse, comprobar si tiene suelo debajo (mecánica Atari 2600)
    // Crear barril temporal con la nueva posición X
    val newX = barrel.position.x + barrel.velocity.x
    val tempBarrel = barrel.copy(position = Offset(newX, barrel.position.y))

    val groundBelow = hasPlatformBelow(tempBarrel, platforms, checkDistance = 3f)

    if (groundBelow == null) {
        // NO HAY SUELO DEBAJO -> El barril CAE VERTICALMENTE
        // Primero verificar si hay escalera en el borde
        val barrelCenterX = newX + barrel.size / 2
        val ladderAtEdge = ladders.find { ladder ->
            ladder.topPlatformIndex == platform.index &&
            abs(ladder.centerX - barrelCenterX) < 25f
        }

        if (ladderAtEdge != null) {
            // Hay escalera, bajar por ella
            return barrel.copy(
                position = Offset(ladderAtEdge.centerX - barrel.size / 2, barrel.position.y),
                velocity = Offset(0f, GameConstants.BARREL_LADDER_FALL_SPEED),
                isOnLadder = true,
                isFalling = false,
                rotation = rotation,
                lastLadderChecked = -1
            )
        } else {
            // No hay escalera, caída libre VERTICAL
            return barrel.copy(
                position = Offset(newX, barrel.position.y),
                velocity = Offset(0f, GameConstants.BARREL_FREE_FALL_SPEED),
                isFalling = true,
                isOnLadder = false,
                currentPlatformIndex = -1,
                rotation = rotation
            )
        }
    }

    // PASO 2: HAY SUELO -> Movimiento HORIZONTAL permitido
    // Verificar si hay escalera debajo mientras rueda (con probabilidad)
    val barrelCenterX = newX + barrel.size / 2
    val ladderBelow = ladders.find { ladder ->
        ladder.topPlatformIndex == platform.index &&
        abs(barrelCenterX - ladder.centerX) < 15f
    }

    if (ladderBelow != null && barrel.lastLadderChecked != ladderBelow.hashCode()) {
        // Probabilidad de bajar por la escalera
        if (kotlin.random.Random.nextFloat() < GameConstants.BARREL_LADDER_PROBABILITY) {
            return barrel.copy(
                position = Offset(ladderBelow.centerX - barrel.size / 2, barrel.position.y),
                velocity = Offset(0f, GameConstants.BARREL_LADDER_FALL_SPEED),
                isOnLadder = true,
                isFalling = false,
                rotation = rotation,
                lastLadderChecked = ladderBelow.hashCode(),
                targetPlatformIndex = ladderBelow.bottomPlatformIndex
            )
        } else {
            // Marcar que ya checamos esta escalera
            val adjusted = adjustToRampSurface(barrel.copy(position = Offset(newX, barrel.position.y)), groundBelow)
            return adjusted.copy(
                rotation = rotation,
                lastLadderChecked = ladderBelow.hashCode()
            )
        }
    }

    // PASO 3: Ajustar posición Y para seguir la rampa
    val adjustedBarrel = adjustToRampSurface(barrel.copy(position = Offset(newX, barrel.position.y)), groundBelow)

    return adjustedBarrel.copy(
        rotation = rotation,
        currentPlatformIndex = groundBelow.index
    )
}

private fun spawnBarrel(state: GameState): GameState {
    val dkPlatform = state.platforms[5]
    val spawnX = state.enemy.position.x + state.enemy.size
    val spawnY = dkPlatform.getYAt(spawnX) - GameConstants.BARREL_SIZE

    // Plataforma 5 es impar, los barriles van hacia la IZQUIERDA (dirección negativa)
    val direction = if (5 % 2 == 0) 1f else -1f

    val newBarrel = Barrel(
        position = Offset(spawnX, spawnY),
        velocity = Offset(GameConstants.BARREL_SPEED * direction, 0f),
        currentPlatformIndex = 5,
        size = GameConstants.BARREL_SIZE
    )

    return state.copy(barrels = state.barrels + newBarrel)
}

// ========== COLLISIONS ==========

private fun checkCollisions(state: GameState): GameState {
    if (state.player.isInvincible) return state

    val playerHitbox = state.player.hitbox
    for (barrel in state.barrels) {
        if (!barrel.hasBeenJumped && state.player.isJumping && state.player.velocity.y >= 0) {
            if (playerHitbox.bottom >= barrel.hitbox.top - 15f &&
                playerHitbox.bottom <= barrel.hitbox.top + 8f &&
                playerHitbox.right > barrel.hitbox.left &&
                playerHitbox.left < barrel.hitbox.right
            ) {
                return state.copy(
                    score = state.score + GameConstants.POINTS_JUMP_BARREL,
                    barrels = state.barrels.map { if (it === barrel) it.copy(hasBeenJumped = true) else it }
                )
            }
        }

        if (playerHitbox.overlaps(barrel.hitbox)) {
            return handleDeath(state)
        }
    }
    return state
}

private fun checkWin(state: GameState): GameState {
    if (state.player.hitbox.overlaps(state.winObjetive.hitbox)) {
        val finalScore = state.score + GameConstants.POINTS_WIN
        return state.copy(isWon = true, score = finalScore, highScore = maxOf(state.highScore, finalScore))
    }
    return state
}
