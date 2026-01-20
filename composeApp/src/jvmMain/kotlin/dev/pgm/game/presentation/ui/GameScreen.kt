package dev.pgm.game.presentation.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.pgm.game.input.GameInput
import dev.pgm.game.input.InputHandler
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.presentation.renderer.GameRenderer
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.presentation.ui.components.HighScoreDialog
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject

/**
 * Pantalla del juego que envuelve el GameRenderer actual.
 * Gestiona el game loop, input handling y diálogo de high score.
 */
@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHighScores: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GameViewModelComplete = koinInject()
    val gameState by viewModel.gameState.collectAsState()
    val showHighScoreDialog by viewModel.showHighScoreDialog.collectAsState()

    var input by remember { mutableStateOf(GameInput()) }
    var shouldRestart by remember { mutableStateOf(false) }
    var shouldTogglePause by remember { mutableStateOf(false) }
    val inputHandler = remember { InputHandler() }
    val focusRequester = remember { FocusRequester() }

    // Request focus when the screen is first composed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Detectar fin de juego y verificar high score
    LaunchedEffect(gameState.isGameOver, gameState.isWon) {
        if (gameState.isGameOver || gameState.isWon) {
            viewModel.checkIfHighScore()
        }
    }

    // Manejar reinicio
    LaunchedEffect(shouldRestart) {
        if (shouldRestart) {
            viewModel.restart()
            shouldRestart = false
        }
    }

    // Manejar pausa
    LaunchedEffect(shouldTogglePause) {
        if (shouldTogglePause) {
            viewModel.togglePause()
        }
    }

    // Keep a reference to the current input that can be read from the game loop
    val currentInput by rememberUpdatedState(input)

    // Game loop
    LaunchedEffect(Unit) {
        var lastFrameTime = System.currentTimeMillis()
        while (isActive) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastFrameTime) / 1000f
            lastFrameTime = currentTime

            viewModel.tick(deltaTime, currentInput)

            delay(GameConstants.FRAME_DELAY_MS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val (newInput, consumed) = inputHandler.handleKeyEvent(
                    event = event,
                    currentInput = input,
                    onRestart = { shouldRestart = true },
                    onPause = { shouldTogglePause = !shouldTogglePause }
                )
                input = newInput
                consumed
            }
    ) {
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

        // Botón de retorno al menú (visible cuando termina el juego)
        if (gameState.isGameOver || gameState.isWon) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "RETURN TO MENU",
                    fontFamily = GameFonts.GameFont,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Diálogo de High Score
    if (showHighScoreDialog) {
        HighScoreDialog(
            score = gameState.score,
            onSave = { playerName ->
                viewModel.saveHighScore(playerName) {
                    onNavigateToHighScores()
                }
            },
            onDismiss = {
                viewModel.dismissHighScoreDialog {
                    onNavigateBack()
                }
            }
        )
    }
}
