package dev.pgm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.* // Adding Material imports
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
import androidx.compose.ui.unit.dp // Added for dp unit
import dev.pgm.game.audio.MusicController
import dev.pgm.game.audio.SoundPlayer
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import dev.pgm.game.presentation.renderer.GameRenderer
import dev.pgm.game.resources.AnimationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject

/**
 * Aplicación web principal - versión simplificada sin navegación compleja.
 */
@Composable
fun WebApp() {
    var currentScreen by remember { mutableStateOf(Screen.MENU) }
    
    when (currentScreen) {
        Screen.MENU -> WebMainMenu(
            onStartGame = { currentScreen = Screen.GAME }
        )
        Screen.GAME -> WebGameScreen(
            onBack = { currentScreen = Screen.MENU }
        )
    }
}

enum class Screen {
    MENU, GAME
}

@Composable
fun WebMainMenu(onStartGame: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        MusicController.playMenuMusic()
        focusRequester.requestFocus()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            MusicController.stopMusic()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Enter, Key.Spacebar -> {
                            SoundPlayer.playMenuConfirm()
                            MusicController.stopMusic()
                            onStartGame()
                            true
                        }
                        else -> false
                    }
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CAT JUMP",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            )
            Text(
                text = "BARRELS",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Press ENTER or SPACE to start",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF00FFFF)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Controls: Arrow Keys / WASD to move, SPACE to jump",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun WebGameScreen(onBack: () -> Unit) {
    val viewModel: GameViewModelComplete = koinInject()
    val gameState by viewModel.gameState.collectAsState()
    
    var input by remember { mutableStateOf(GameInput()) }
    val focusRequester = remember { FocusRequester() }
    var animationsLoaded by remember { mutableStateOf(AnimationProvider.isLoaded()) }
    
    // Cargar animaciones si no están cargadas
    LaunchedEffect(Unit) {
        if (!AnimationProvider.isLoaded()) {
            AnimationProvider.loadAllAnimations()
            animationsLoaded = true
        }
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
    
    LaunchedEffect(Unit) {
        viewModel.resetForNewGame()
        focusRequester.requestFocus()
    }
    
    // Game loop
    val currentInput by rememberUpdatedState(input)
    LaunchedEffect(Unit) {
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
                        if (event.key == Key.Escape) {
                            onBack()
                        }
                        if (event.key == Key.R && (gameState.isGameOver || gameState.isWon)) {
                            viewModel.restart()
                        }
                        if (event.key == Key.P) {
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
        // Game renderer
        GameRenderer(
            state = gameState,
            modifier = Modifier.fillMaxSize()
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
