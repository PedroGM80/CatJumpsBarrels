package dev.pgm.game.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.audio.RetroSoundGenerator
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
    var showQuitMenu by remember { mutableStateOf(false) }
    val inputHandler = remember { InputHandler() }
    val focusRequester = remember { FocusRequester() }

    // Conectar sonidos al ViewModel
    LaunchedEffect(viewModel) {
        viewModel.onPlaySound = { event ->
            when (event) {
                GameViewModelComplete.SoundEvent.JUMP -> RetroSoundGenerator.playJump()
                GameViewModelComplete.SoundEvent.SCORE -> RetroSoundGenerator.playScore()
                GameViewModelComplete.SoundEvent.DEATH -> RetroSoundGenerator.playDeath()
                GameViewModelComplete.SoundEvent.WIN -> RetroSoundGenerator.playWin()
                GameViewModelComplete.SoundEvent.PAUSE -> RetroSoundGenerator.playPause()
                GameViewModelComplete.SoundEvent.BARREL_THROW -> RetroSoundGenerator.playBarrelThrow()
            }
        }
    }

    // Reset game state and request focus when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.resetForNewGame()
        focusRequester.requestFocus()
    }

    // Detectar fin de juego y verificar high score
    LaunchedEffect(gameState.isGameOver, gameState.isWon) {
        if (gameState.isGameOver || gameState.isWon) {
            viewModel.checkIfHighScore()
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
                    onRestart = { viewModel.restart() },
                    onPause = {
                        if (!showQuitMenu) {
                            viewModel.togglePause()
                        }
                    },
                    onEscape = {
                        val willShowMenu = !showQuitMenu
                        showQuitMenu = willShowMenu
                        if (willShowMenu && !gameState.isPaused) {
                            viewModel.togglePause()
                        } else if (!willShowMenu && gameState.isPaused) {
                            viewModel.togglePause()
                        }
                    }
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

        // Menú de salida (Escape)
        if (showQuitMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "QUIT GAME?",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GameFonts.GameFont
                        )
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            showQuitMenu = false
                            if (gameState.isPaused) {
                                viewModel.togglePause()
                            }
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "CONTINUE",
                            fontFamily = GameFonts.GameFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "QUIT TO MENU",
                            fontFamily = GameFonts.GameFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
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
