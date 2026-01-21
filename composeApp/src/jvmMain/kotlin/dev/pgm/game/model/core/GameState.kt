package dev.pgm.game.model.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import dev.pgm.game.model.entities.*
import dev.pgm.game.model.utils.Particle
import dev.pgm.game.model.utils.ScorePopup

data class GameState(
    val player: Player,
    val enemy: Boss,
    val winObjetive: WinObjetive,
    val platforms: List<Platform>,
    val ladders: List<Ladder>,
    val barrels: List<Barrel>,
    val particles: List<Particle> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val lives: Int = 3,
    val level: Int = 1,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val isPaused: Boolean = false,
    val screenSize: IntSize,
    val lastScorePopup: ScorePopup? = null,
    val playerDeathTimestamp: Long = 0L
) {
    companion object {
        fun initial(screenSize: IntSize): GameState {
            // Usar altura de diseño fija para posicionamiento consistente
            val designHeight = GameConstants.LEVEL_HEIGHT
            val platforms = createPlatforms(designHeight)
            val princessPlatform = createPrincessPlatform(platforms[5])
            val allPlatforms = platforms + princessPlatform
            val ladders = createLadders(allPlatforms, princessPlatform)

            return GameState(
                player = createPlayer(allPlatforms[0]),
                enemy = createBoss(allPlatforms[5]),
                winObjetive = createWinObjetive(princessPlatform),
                platforms = allPlatforms,
                ladders = ladders,
                barrels = emptyList(),
                screenSize = screenSize
            )
        }

        private fun createPlatforms(designHeight: Float): List<Platform> {
            return (0 until GameConstants.PLATFORM_COUNT).map { i ->
                createPlatform(i, designHeight)
            }
        }

        private fun createPlatform(index: Int, designHeight: Float): Platform {
            val y = designHeight - GameConstants.PLATFORM_BOTTOM_OFFSET - (index * GameConstants.PLATFORM_GAP)
            val isEvenIndex = index % 2 == 0
            // Plataforma 5 (del enemigo) es recta, las demas tienen pendiente
            val currentSlope = if (index == 5) 0f else if (isEvenIndex) GameConstants.PLATFORM_SLOPE else -GameConstants.PLATFORM_SLOPE
            val startX = if (isEvenIndex) 0f else GameConstants.PLATFORM_START_OFFSET
            val platformWidth = if (index == GameConstants.PLATFORM_COUNT - 1) {
                GameConstants.LEVEL_WIDTH * GameConstants.ENEMY_PLATFORM_WIDTH_SCALE
            } else {
                GameConstants.LEVEL_WIDTH - GameConstants.PLATFORM_START_OFFSET
            }

            return Platform(
                position = Offset(startX, y),
                width = platformWidth,
                height = GameConstants.PLATFORM_HEIGHT,
                index = index,
                slope = currentSlope
            )
        }

        private fun createPrincessPlatform(enemyPlatform: Platform): Platform {
            val x = enemyPlatform.right - GameConstants.PRINCESS_PLATFORM_OFFSET_X
            val y = enemyPlatform.getYAt(x) - GameConstants.PRINCESS_PLATFORM_OFFSET_Y

            return Platform(
                position = Offset(x, y),
                width = GameConstants.PRINCESS_PLATFORM_WIDTH,
                height = GameConstants.PLATFORM_HEIGHT,
                index = 6
            )
        }

        /**
         * Crea las escaleras del nivel:
         * - Escaleras principales en los extremos alternados de las plataformas
         * - Escaleras adicionales en posiciones intermedias para que los barriles puedan bajar
         */
        private fun createLadders(platforms: List<Platform>, princessPlatform: Platform): List<Ladder> {
            val allLadders = mutableListOf<Ladder>()
            
            // Crear escaleras principales entre plataformas (en los bordes alternados)
            for (i in 0..4) {
                val bottomPlatform = platforms[i]
                val topPlatform = platforms[i + 1]
                
                // Escalera principal en el borde (alterna izquierda/derecha)
                // Excepto para i=4 (plataforma 4->5) que se maneja aparte con las dos escaleras del enemigo
                if (i < 4) {
                    val mainLadder = createLadderBetweenPlatforms(bottomPlatform, topPlatform, i)
                    allLadders.add(mainLadder)
                }
                
                // Escalera adicional en posicion intermedia
                if (i < 4) {
                    val middleLadder = createMiddleLadder(bottomPlatform, topPlatform, i)
                    allLadders.add(middleLadder)
                }
            }
            
            // Dos escaleras desde la plataforma del enemigo (plataforma 5) hacia abajo (plataforma 4)
            val enemyPlatform = platforms[5]
            val belowEnemyPlatform = platforms[4]

            // Escalera izquierda
            val leftEnemyLadderX = enemyPlatform.left + 30f
            val leftEnemyLadderTop = enemyPlatform.getYAt(leftEnemyLadderX)
            val leftEnemyLadderBottom = belowEnemyPlatform.getYAt(leftEnemyLadderX)
            val leftLadder = Ladder(
                position = Offset(leftEnemyLadderX, leftEnemyLadderTop),
                height = leftEnemyLadderBottom - leftEnemyLadderTop,
                topPlatformIndex = 5,
                bottomPlatformIndex = 4
            )
            allLadders.add(leftLadder)

            // Escalera derecha
            val rightEnemyLadderX = enemyPlatform.right - 30f
            val rightEnemyLadderTop = enemyPlatform.getYAt(rightEnemyLadderX)
            val rightEnemyLadderBottom = belowEnemyPlatform.getYAt(rightEnemyLadderX)
            val rightLadder = Ladder(
                position = Offset(rightEnemyLadderX, rightEnemyLadderTop),
                height = rightEnemyLadderBottom - rightEnemyLadderTop,
                topPlatformIndex = 5,
                bottomPlatformIndex = 4
            )
            allLadders.add(rightLadder)

            // Escalera a la princesa
            val princessLadder = createPrincessLadder(platforms[5], princessPlatform)
            allLadders.add(princessLadder)
            
            return allLadders
        }

        private fun createLadderBetweenPlatforms(bottomPlatform: Platform, topPlatform: Platform, index: Int): Ladder {
            val isEvenIndex = index % 2 == 0
            val ladderX = if (isEvenIndex) {
                bottomPlatform.right - GameConstants.LADDER_OFFSET_FROM_EDGE
            } else {
                bottomPlatform.left + GameConstants.LADDER_OFFSET_FROM_EDGE
            }
            // Escalera llega exactamente al filo superior de la plataforma
            val ladderTop = topPlatform.getYAt(ladderX)
            val ladderBottom = bottomPlatform.getYAt(ladderX)
            val ladderHeight = ladderBottom - ladderTop

            return Ladder(
                position = Offset(ladderX, ladderTop),
                height = ladderHeight,
                topPlatformIndex = index + 1,
                bottomPlatformIndex = index
            )
        }


        /**
         * Crea una escalera en posición intermedia de la plataforma
         */
        private fun createMiddleLadder(bottomPlatform: Platform, topPlatform: Platform, index: Int): Ladder {
            val isEvenIndex = index % 2 == 0
            val platformCenter = (bottomPlatform.left + bottomPlatform.right) / 2
            val ladderX = if (isEvenIndex) {
                platformCenter - 80f
            } else {
                platformCenter + 80f
            }
            
            val clampedX = ladderX.coerceIn(
                maxOf(bottomPlatform.left, topPlatform.left) + 30f,
                minOf(bottomPlatform.right, topPlatform.right) - 30f
            )
            
            val ladderTop = topPlatform.getYAt(clampedX)
            val ladderBottom = bottomPlatform.getYAt(clampedX)
            val ladderHeight = ladderBottom - ladderTop

            return Ladder(
                position = Offset(clampedX, ladderTop),
                height = ladderHeight,
                topPlatformIndex = index + 1,
                bottomPlatformIndex = index
            )
        }

        private fun createPrincessLadder(enemyPlatform: Platform, princessPlatform: Platform): Ladder {
            val ladderX = princessPlatform.left + GameConstants.LADDER_OFFSET_FROM_PRINCESS
            // Escalera llega exactamente al filo superior de la plataforma
            val ladderTop = princessPlatform.top
            val ladderHeight = enemyPlatform.getYAt(ladderX) - ladderTop

            return Ladder(
                position = Offset(ladderX, ladderTop),
                height = ladderHeight,
                topPlatformIndex = 6,
                bottomPlatformIndex = 5
            )
        }

        private fun createPlayer(startPlatform: Platform): Player {
            val x = GameConstants.PLAYER_START_X
            val visualOffset = 2f  // Ajuste para que parezca estar sobre la superficie de la viga
            val y = startPlatform.getYAt(x) - GameConstants.PLAYER_SIZE + visualOffset

            return Player(
                position = Offset(x, y),
                size = GameConstants.PLAYER_SIZE,
                isOnGround = true
            )
        }

        private fun createBoss(platform: Platform): Boss {
            val x = GameConstants.ENEMY_START_X + 40f  // Un poco mas a la derecha
            val visualOffset = 2f  // Ajuste para que parezca estar sobre la superficie de la viga
            val y = platform.getYAt(x) - GameConstants.ENEMY_SIZE + visualOffset

            return Boss(
                position = Offset(x, y),
                size = GameConstants.ENEMY_SIZE
            )
        }

        private fun createWinObjetive(princessPlatform: Platform): WinObjetive {
            val x = princessPlatform.position.x + GameConstants.PRINCESS_OFFSET_X
            val visualOffset = 2f  // Ajuste para que parezca estar sobre la superficie de la viga
            val y = princessPlatform.top - GameConstants.PRINCESS_SIZE + visualOffset

            return WinObjetive(
                position = Offset(x, y),
                size = GameConstants.PRINCESS_SIZE
            )
        }
    }
}
