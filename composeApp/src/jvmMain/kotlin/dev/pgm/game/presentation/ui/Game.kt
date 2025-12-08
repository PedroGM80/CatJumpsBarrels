package dev.pgm.game.presentation.ui

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import dev.pgm.game.di.initKoin
import dev.pgm.game.input.GameInput
import dev.pgm.game.input.InputHandler
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.presentation.renderer.GameRenderer
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import org.koin.compose.koinInject

/**
 * Punto de entrada de la aplicación con Clean Architecture + Koin.
 * Refactored: Input handling extracted to InputHandler (SRP)
 */
fun main() {
    initKoin()

    application {
        var input by remember { mutableStateOf(GameInput()) }
        var shouldRestart by remember { mutableStateOf(false) }
        val inputHandler = remember { InputHandler() }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Cat Jump Barrels - Clean Architecture + Koin",
            state = WindowState(size = DpSize(850.dp, 750.dp)),
            resizable = true,
            onKeyEvent = { event ->
                val (newInput, consumed) = inputHandler.handleKeyEvent(
                    event = event,
                    currentInput = input,
                    onRestart = {
                        input = GameInput()
                        shouldRestart = true
                    },
                    onPause = {
                        // Pause functionality can be added here
                    }
                )
                input = newInput
                consumed
            }
        ) {
            val viewModel: GameViewModelComplete = koinInject()

            // Manejar reinicio cuando se presiona R
            LaunchedEffect(shouldRestart) {
                if (shouldRestart) {
                    viewModel.restart()
                    shouldRestart = false
                }
            }
            val gameState by viewModel.gameState.collectAsState()

            // Game loop
            LaunchedEffect(input) {  // Añadido input como key para forzar recomposición
                var lastFrameTime = System.currentTimeMillis()
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastFrameTime) / 1000f
                    lastFrameTime = currentTime

                    viewModel.tick(deltaTime, input)

                    delay(GameConstants.FRAME_DELAY_MS)
                }
            }

            MaterialTheme(colors = darkColors()) {
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
        }
    }
}
