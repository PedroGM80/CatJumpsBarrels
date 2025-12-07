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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import dev.pgm.game.di.initKoin
import dev.pgm.game.input.GameInput
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import org.koin.compose.koinInject

/**
 * Punto de entrada de la aplicación con Clean Architecture + Koin.
 */
fun main() {
    initKoin()

    application {
        var input by remember { mutableStateOf(GameInput()) }
        var shouldRestart by remember { mutableStateOf(false) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Cat Jump Barrels - Clean Architecture + Koin",
            state = WindowState(size = DpSize(850.dp, 750.dp)),
            resizable = true,
            onKeyEvent = { event ->
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        when (event.key) {
                            Key.R -> {
                                input = GameInput()
                                shouldRestart = true
                                true
                            }
                            Key.P, Key.Escape -> true
                            Key.DirectionLeft, Key.A -> { input = input.copy(left = true); true }
                            Key.DirectionRight, Key.D -> { input = input.copy(right = true); true }
                            Key.DirectionUp, Key.W -> { input = input.copy(up = true); true }
                            Key.DirectionDown, Key.S -> { input = input.copy(down = true); true }
                            Key.Spacebar -> { input = input.copy(jump = true); true }
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

                    delay(16)
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
