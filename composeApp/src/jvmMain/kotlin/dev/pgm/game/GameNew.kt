package dev.pgm.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.pgm.game.di.initKoin
import dev.pgm.game.input.GameInput
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject

/**
 * Punto de entrada de la aplicación con Clean Architecture + Koin.
 */
fun main() {
    // Inicializar Koin
    initKoin()

    application {
        var input by remember { mutableStateOf(GameInput()) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Cat Jump Barrels - Clean Architecture + Koin",
            state = WindowState(size = DpSize(850.dp, 750.dp)),
            resizable = true,
            onKeyEvent = { event ->
                println("LOG 1: onKeyEvent - type=${event.type}, key=${event.key}")
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        when (event.key) {
                            Key.R -> {
                                println("LOG 2: R pressed")
                                input = GameInput(); true
                            }
                            Key.P, Key.Escape -> {
                                println("LOG 2: P/Escape pressed")
                                true
                            }
                            Key.DirectionLeft, Key.A -> {
                                println("LOG 2: Left/A pressed - BEFORE: $input")
                                input = input.copy(left = true)
                                println("LOG 2: Left/A pressed - AFTER: $input")
                                true
                            }
                            Key.DirectionRight, Key.D -> {
                                println("LOG 2: Right/D pressed")
                                input = input.copy(right = true); true
                            }
                            Key.DirectionUp, Key.W -> {
                                println("LOG 2: Up/W pressed")
                                input = input.copy(up = true); true
                            }
                            Key.DirectionDown, Key.S -> {
                                println("LOG 2: Down/S pressed")
                                input = input.copy(down = true); true
                            }
                            Key.Spacebar -> {
                                println("LOG 2: Spacebar pressed")
                                input = input.copy(jump = true); true
                            }
                            else -> false
                        }
                    }
                    KeyEventType.KeyUp -> {
                        when (event.key) {
                            Key.DirectionLeft, Key.A -> {
                                println("LOG 2: Left/A released")
                                input = input.copy(left = false); true
                            }
                            Key.DirectionRight, Key.D -> {
                                println("LOG 2: Right/D released")
                                input = input.copy(right = false); true
                            }
                            Key.DirectionUp, Key.W -> {
                                println("LOG 2: Up/W released")
                                input = input.copy(up = false); true
                            }
                            Key.DirectionDown, Key.S -> {
                                println("LOG 2: Down/S released")
                                input = input.copy(down = false); true
                            }
                            Key.Spacebar -> {
                                println("LOG 2: Spacebar released")
                                input = input.copy(jump = false); true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
        ) {
            MaterialTheme(colors = darkColors()) {
                GameContent(input)
            }
        }
    }
}

@Composable
private fun GameContent(input: GameInput) {
    val viewModel: GameViewModelComplete = koinInject()
    val gameState by viewModel.gameState.collectAsState()

    var frameCount by remember { mutableStateOf(0) }

    // Game Loop
    LaunchedEffect(Unit) {
        var lastFrameTime = System.currentTimeMillis()
        while (isActive) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastFrameTime) / 1000f
            lastFrameTime = currentTime

            frameCount++
            if (frameCount % 60 == 0 || input.left || input.right || input.up || input.down || input.jump) {
                println("LOG 3: GameContent - frame=$frameCount, input=$input, deltaTime=$deltaTime")
            }

            viewModel.tick(deltaTime, input)

            delay(16)
        }
    }

    GameRenderer(
        state = gameState,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    viewModel.setScreenSize(size.width, size.height)
                }
            }
    )
}
