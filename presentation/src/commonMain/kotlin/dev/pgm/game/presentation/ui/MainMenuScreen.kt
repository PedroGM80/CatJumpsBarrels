package dev.pgm.game.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.presentation.theme.GameFonts

/**
 * Pantalla del menú principal con estilo arcade retro.
 * @param onExit Callback para salir del juego. Si es null, el botón EXIT no se muestra (útil para web).
 */
@Composable
fun MainMenuScreen(
    onNavigateToGame: () -> Unit,
    onNavigateToHighScores: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onExit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showExitConfirmation by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D1A),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0D0D1A)
                        )
                    )
                )
        )

        // Grid de fondo estilo arcade
        ArcadeGrid()

        // Efecto scanlines CRT sutil
        CRTScanlines()

        // Contenido del menú
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo animado
            AnimatedGameLogo()

            Spacer(modifier = Modifier.height(48.dp))

            // Botones del menú
            ArcadeMenuButton(
                text = "NEW GAME",
                icon = "▶",
                color = Color(0xFF00FF88),
                onClick = onNavigateToGame
            )

            Spacer(modifier = Modifier.height(16.dp))

            ArcadeMenuButton(
                text = "HIGH SCORES",
                icon = "★",
                color = Color(0xFFFFD700),
                onClick = onNavigateToHighScores
            )

            Spacer(modifier = Modifier.height(16.dp))

            ArcadeMenuButton(
                text = "CREDITS",
                icon = "ℹ",
                color = Color(0xFF00BFFF),
                onClick = onNavigateToCredits
            )

            // Solo mostrar botón EXIT si onExit no es null (desktop)
            if (onExit != null) {
                Spacer(modifier = Modifier.height(32.dp))

                ArcadeMenuButton(
                    text = "EXIT",
                    icon = "✕",
                    color = Color(0xFFFF6B6B),
                    onClick = { showExitConfirmation = true }
                )
            }
        }

        // Versión
        Text(
            text = "v1.0.0",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Instrucciones
        Text(
            text = "USE ARROW KEYS TO MOVE • SPACE TO JUMP",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 10.sp,
                color = Color(0xFF00FFFF).copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Diálogo de confirmación de salida (solo si onExit no es null)
        if (showExitConfirmation && onExit != null) {
            ExitConfirmationDialog(
                onConfirm = onExit,
                onDismiss = { showExitConfirmation = false }
            )
        }
    }
}

/**
 * Grid de fondo estilo arcade.
 */
@Composable
private fun ArcadeGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 40f
        val lineColor = Color(0xFF00FFFF).copy(alpha = 0.05f)

        // Líneas verticales
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Líneas horizontales
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }
}

/**
 * Efecto scanlines CRT sutil.
 */
@Composable
private fun CRTScanlines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 3f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineHeight
        }
    }
}

/**
 * Logo del juego animado.
 */
@Composable
private fun AnimatedGameLogo() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animación de glow pulsante
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de escala sutil
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        // CAT JUMP
        Text(
            text = "CAT JUMP",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                shadow = Shadow(
                    color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                    blurRadius = 20f,
                    offset = Offset.Zero
                )
            )
        )

        // BARRELS
        Text(
            text = "BARRELS",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                shadow = Shadow(
                    color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                    blurRadius = 20f,
                    offset = Offset.Zero
                )
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtítulo
        Text(
            text = "~ A Retro Arcade Adventure ~",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 12.sp,
                color = Color(0xFF00FFFF),
                shadow = Shadow(
                    color = Color(0xFF00FFFF).copy(alpha = 0.5f),
                    blurRadius = 8f
                )
            )
        )
    }
}

/**
 * Botón de menú estilo arcade.
 */
@Composable
private fun ArcadeMenuButton(
    text: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val currentColor = when {
        isPressed -> color.copy(alpha = 0.8f)
        isHovered -> color
        else -> color.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .width(280.dp)
            .height(56.dp)
            .scale(scale)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF2A2A4E),
                        Color(0xFF1A1A2E)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 3.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.5f),
                        currentColor,
                        currentColor.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        isHovered = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = TextStyle(
                    fontSize = 20.sp,
                    color = currentColor,
                    shadow = if (isHovered) Shadow(
                        color = currentColor,
                        blurRadius = 10f
                    ) else null
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 18.sp,
                    color = if (isHovered) Color.White else Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    shadow = if (isHovered) Shadow(
                        color = currentColor,
                        blurRadius = 12f
                    ) else null
                )
            )
        }
    }
}

/**
 * Diálogo de confirmación de salida.
 */
@Composable
private fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EXIT GAME?",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 36.sp,
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color(0xFFFF6B6B).copy(alpha = 0.5f),
                        blurRadius = 15f
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Are you sure you want to quit?",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            ArcadeMenuButton(
                text = "YES, EXIT",
                icon = "✓",
                color = Color(0xFFFF6B6B),
                onClick = onConfirm
            )

            Spacer(modifier = Modifier.height(16.dp))

            ArcadeMenuButton(
                text = "NO, STAY",
                icon = "✕",
                color = Color(0xFF00FF88),
                onClick = onDismiss
            )
        }
    }
}
