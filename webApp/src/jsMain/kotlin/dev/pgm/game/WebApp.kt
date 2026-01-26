package dev.pgm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import dev.pgm.game.audio.MusicController
import dev.pgm.game.audio.SoundPlayer
import dev.pgm.game.input.GameInput
import dev.pgm.game.presentation.renderer.GameRenderer
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.presentation.ui.MainMenuScreen
import dev.pgm.game.presentation.ui.HighScoresScreen
import dev.pgm.game.presentation.ui.CreditsScreen
import dev.pgm.game.presentation.ui.components.HighScoreDialog
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import dev.pgm.game.resources.AnimationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject

/**
 * Aplicación web principal con navegación completa.
 */
@Composable
fun WebApp() {
    var currentScreen by remember { mutableStateOf(Screen.MENU) }
    
    when (currentScreen) {
        Screen.MENU -> {
            // Iniciar música del menú
            LaunchedEffect(Unit) {
                MusicController.playMenuMusic()
            }
            DisposableEffect(Unit) {
                onDispose {
                    MusicController.stopMusic()
                }
            }
            
            MainMenuScreen(
                onNavigateToGame = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    currentScreen = Screen.GAME
                },
                onNavigateToHighScores = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    currentScreen = Screen.HIGH_SCORES
                },
                onNavigateToCredits = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    currentScreen = Screen.CREDITS
                }
                // onExit no se pasa en web - el botón EXIT no se mostrará
            )
        }
        
        Screen.GAME -> WebGameScreen(
            onBack = { 
                currentScreen = Screen.MENU 
            },
            onNavigateToHighScores = {
                currentScreen = Screen.HIGH_SCORES
            }
        )
        
        Screen.HIGH_SCORES -> {
            HighScoresScreen(
                onNavigateBack = {
                    SoundPlayer.playMenuSelect()
                    currentScreen = Screen.MENU
                }
            )
        }
        
        Screen.CREDITS -> {
            CreditsScreen(
                onNavigateBack = {
                    SoundPlayer.playMenuSelect()
                    currentScreen = Screen.MENU
                }
            )
        }
    }
}

enum class Screen {
    MENU, GAME, HIGH_SCORES, CREDITS
}

@Composable
fun WebGameScreen(
    onBack: () -> Unit,
    onNavigateToHighScores: () -> Unit
) {
    val viewModel: GameViewModelComplete = koinInject()
    val gameState by viewModel.gameState.collectAsState()
    val showHighScoreDialog by viewModel.showHighScoreDialog.collectAsState()
    
    var input by remember { mutableStateOf(GameInput()) }
    var showQuitMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var animationsLoaded by remember { mutableStateOf(AnimationProvider.isLoaded()) }
    
    // Cargar animaciones primero
    LaunchedEffect(Unit) {
        if (!AnimationProvider.isLoaded()) {
            AnimationProvider.loadAllAnimations()
            animationsLoaded = true
        }
    }
    
    // Mostrar pantalla de carga mientras no estén listas las animaciones
    if (!animationsLoaded) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Loading assets...",
                    style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
        return
    }
    
    // Conectar sonidos al ViewModel
    LaunchedEffect(viewModel) {
        viewModel.onPlaySound = { event ->
            when (event) {
                GameViewModelComplete.SoundEvent.JUMP -> SoundPlayer.playJump()
                GameViewModelComplete.SoundEvent.SCORE -> SoundPlayer.playScore()
                GameViewModelComplete.SoundEvent.DEATH -> SoundPlayer.playDeath()
                GameViewModelComplete.SoundEvent.WIN -> SoundPlayer.playWin()
                GameViewModelComplete.SoundEvent.PAUSE -> SoundPlayer.playPause()
                GameViewModelComplete.SoundEvent.BARREL_THROW -> SoundPlayer.playBarrelThrow()
            }
        }
    }
    
    LaunchedEffect(animationsLoaded) {
        if (animationsLoaded) {
            viewModel.resetForNewGame()
            focusRequester.requestFocus()
        }
    }
    
    // Detectar fin de juego y verificar high score
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            viewModel.checkIfHighScore()
        }
    }
    
    // Game loop - solo inicia cuando las animaciones están cargadas
    val currentInput by rememberUpdatedState(input)
    LaunchedEffect(animationsLoaded) {
        if (!animationsLoaded) return@LaunchedEffect
        var lastTime = 0L
        while (isActive) {
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            if (lastTime == 0L) lastTime = currentTime
            val deltaTime = (currentTime - lastTime) / 1000f
            lastTime = currentTime
            
            viewModel.tick(deltaTime.coerceAtMost(0.05f), currentInput)
            delay(16)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        input = handleKeyDown(event.key, input)
                        
                        // Escape: mostrar/ocultar menú de salida
                        if (event.key == Key.Escape) {
                            val willShowMenu = !showQuitMenu
                            showQuitMenu = willShowMenu
                            if (willShowMenu && !gameState.isPaused) {
                                viewModel.togglePause()
                            } else if (!willShowMenu && gameState.isPaused) {
                                viewModel.togglePause()
                            }
                        }
                        
                        // R para reiniciar cuando game over
                        if (event.key == Key.R && gameState.isGameOver) {
                            viewModel.restart()
                        }
                        
                        // P para pausar (solo si no hay menú de salida)
                        if (event.key == Key.P && !showQuitMenu) {
                            viewModel.togglePause()
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        input = handleKeyUp(event.key, input)
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Game renderer con sprites
        GameRenderer(
            state = gameState,
            modifier = Modifier.fillMaxSize()
        )
        
        // Botón de retorno al menú (visible cuando termina el juego)
        if (gameState.isGameOver && !showQuitMenu) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1A1A1A),
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
                    .background(Color.Black.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PAUSED",
                        style = TextStyle(
                            color = Color(0xFFFFD700),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GameFonts.GameFont
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Botón Continuar
                    Button(
                        onClick = {
                            showQuitMenu = false
                            if (gameState.isPaused) {
                                viewModel.togglePause()
                            }
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.width(220.dp).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4CAF50),
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
                    
                    // Botón Reiniciar
                    Button(
                        onClick = {
                            showQuitMenu = false
                            viewModel.restart()
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.width(220.dp).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "RESTART",
                            fontFamily = GameFonts.GameFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón Salir al menú
                    Button(
                        onClick = onBack,
                        modifier = Modifier.width(220.dp).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFFF5252),
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        "Press ESC to resume",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = GameFonts.GameFont
                        )
                    )
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
                    onBack()
                }
            }
        )
    }
}

private fun handleKeyDown(key: Key, input: GameInput): GameInput {
    return when (key) {
        Key.W, Key.DirectionUp -> input.copy(up = true)
        Key.S, Key.DirectionDown -> input.copy(down = true)
        Key.A, Key.DirectionLeft -> input.copy(left = true)
        Key.D, Key.DirectionRight -> input.copy(right = true)
        Key.Spacebar -> input.copy(jump = true)
        else -> input
    }
}

private fun handleKeyUp(key: Key, input: GameInput): GameInput {
    return when (key) {
        Key.W, Key.DirectionUp -> input.copy(up = false)
        Key.S, Key.DirectionDown -> input.copy(down = false)
        Key.A, Key.DirectionLeft -> input.copy(left = false)
        Key.D, Key.DirectionRight -> input.copy(right = false)
        Key.Spacebar -> input.copy(jump = false)
        else -> input
    }
}
