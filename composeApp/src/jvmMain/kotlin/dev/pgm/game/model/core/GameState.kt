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
    val lastScorePopup: ScorePopup? = null
) {
    companion object {
        fun initial(screenSize: IntSize): GameState {
            val platforms = createPlatforms(screenSize.height.toFloat())
            val princessPlatform = createPrincessPlatform(platforms[5])
            val allPlatforms = platforms + princessPlatform
            val ladders = createLadders(allPlatforms, princessPlatform)

            return GameState(
                player = createPlayer(allPlatforms[0]),
                enemy = createBoos(allPlatforms[5]),
                winObjetive = createWinObjetive(princessPlatform),
                platforms = allPlatforms,
                ladders = ladders,
                barrels = emptyList(),
                screenSize = screenSize
            )
        }

        private fun createPlatforms(screenHeight: Float): List<Platform> {
            return (0 until GameConstants.PLATFORM_COUNT).map { i ->
                createPlatform(i, screenHeight)
            }
        }

        private fun createPlatform(index: Int, screenHeight: Float): Platform {
            val y = screenHeight - GameConstants.PLATFORM_BOTTOM_OFFSET - (index * GameConstants.PLATFORM_GAP)
            val isEvenIndex = index % 2 == 0
            // Plataforma 5 (DK) es recta, las demas tienen pendiente
            val currentSlope = if (index == 5) 0f else if (isEvenIndex) GameConstants.PLATFORM_SLOPE else -GameConstants.PLATFORM_SLOPE
            val startX = if (isEvenIndex) 0f else GameConstants.PLATFORM_START_OFFSET
            val platformWidth = if (index == GameConstants.PLATFORM_COUNT - 1) {
                GameConstants.LEVEL_WIDTH * GameConstants.DK_PLATFORM_WIDTH_SCALE
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

        private fun createPrincessPlatform(dkPlatform: Platform): Platform {
            val x = dkPlatform.right - GameConstants.PRINCESS_PLATFORM_OFFSET_X
            val y = dkPlatform.getYAt(x) - GameConstants.PRINCESS_PLATFORM_OFFSET_Y

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
                // Excepto para i=4 (plataforma 4->5) que se maneja aparte con las dos escaleras de DK
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
            
            // Dos escaleras desde la plataforma de DK (plataforma 5) hacia abajo (plataforma 4)
            // Escalera izquierda
            val dkPlatform = platforms[5]
            val belowDkPlatform = platforms[4]
            val leftDkLadderX = dkPlatform.left + 30f
            val leftDkLadderTop = dkPlatform.getYAt(leftDkLadderX) - 20f
            val leftDkLadderBottom = belowDkPlatform.getYAt(leftDkLadderX)
            allLadders.add(Ladder(
                position = Offset(leftDkLadderX, leftDkLadderTop),
                height = leftDkLadderBottom - leftDkLadderTop,
                topPlatformIndex = 5,
                bottomPlatformIndex = 4
            ))
            
            // Escalera derecha
            val rightDkLadderX = dkPlatform.right - 30f
            val rightDkLadderTop = dkPlatform.getYAt(rightDkLadderX) - 20f
            val rightDkLadderBottom = belowDkPlatform.getYAt(rightDkLadderX)
            allLadders.add(Ladder(
                position = Offset(rightDkLadderX, rightDkLadderTop),
                height = rightDkLadderBottom - rightDkLadderTop,
                topPlatformIndex = 5,
                bottomPlatformIndex = 4
            ))
            
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
            // Extender la escalera un poco por encima de la plataforma superior
            // para que el jugador pueda completar la subida
            val ladderTop = topPlatform.getYAt(ladderX) - 20f
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
            
            val ladderTop = topPlatform.getYAt(clampedX) - 20f
            val ladderBottom = bottomPlatform.getYAt(clampedX)
            val ladderHeight = ladderBottom - ladderTop

            return Ladder(
                position = Offset(clampedX, ladderTop),
                height = ladderHeight,
                topPlatformIndex = index + 1,
                bottomPlatformIndex = index
            )
        }

        private fun createPrincessLadder(dkPlatform: Platform, princessPlatform: Platform): Ladder {
            val ladderX = princessPlatform.left + GameConstants.LADDER_OFFSET_FROM_PRINCESS
            // Extender la escalera un poco por encima
            val ladderTop = princessPlatform.top - 20f
            val ladderHeight = dkPlatform.getYAt(ladderX) - ladderTop

            return Ladder(
                position = Offset(ladderX, ladderTop),
                height = ladderHeight,
                topPlatformIndex = 6,
                bottomPlatformIndex = 5
            )
        }

        private fun createPlayer(startPlatform: Platform): Player {
            val x = GameConstants.PLAYER_START_X
            val y = startPlatform.getYAt(x) - GameConstants.PLAYER_SIZE

            return Player(
                position = Offset(x, y),
                size = GameConstants.PLAYER_SIZE,
                isOnGround = true
            )
        }

        private fun createBoos(platform: Platform): Boss {
            val x = GameConstants.DK_START_X + 40f  // Un poco mas a la derecha
            val y = platform.getYAt(x) - GameConstants.ENEMY_SIZE

            return Boss(
                position = Offset(x, y),
                size = GameConstants.ENEMY_SIZE
            )
        }

        private fun createWinObjetive(princessPlatform: Platform): WinObjetive {
            val x = princessPlatform.position.x + GameConstants.PRINCESS_OFFSET_X
            val y = princessPlatform.top - GameConstants.PRINCESS_SIZE

            return WinObjetive(
                position = Offset(x, y),
                size = GameConstants.PRINCESS_SIZE
            )
        }
    }
}
