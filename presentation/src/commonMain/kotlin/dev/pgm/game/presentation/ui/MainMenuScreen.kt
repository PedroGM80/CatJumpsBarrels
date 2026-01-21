package dev.pgm.game.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pantalla del menú principal.
 * Muestra tres opciones: Nuevo Juego, Records y Créditos.
 */
@Composable
fun MainMenuScreen(
    onNavigateToGame: () -> Unit,
    onNavigateToHighScores: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitConfirmation by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo con gradiente sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color(0xFF1A1A1A),
                            Color(0xFF0F0F0F)
                        )
                    )
                )
        )

        // Partículas flotantes de fondo
        FloatingParticles()

        // Contenido del menú
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título del juego con animación
            AnimatedLogo()

            Spacer(modifier = Modifier.height(32.dp))

            // Botones del menú
            MenuButton(
                text = "NEW GAME",
                onClick = onNavigateToGame
            )

            MenuButton(
                text = "HIGH SCORES",
                onClick = onNavigateToHighScores
            )

            MenuButton(
                text = "CREDITS",
                onClick = onNavigateToCredits
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "EXIT",
                onClick = { showExitConfirmation = true }
            )
        }

        // Versión en la esquina
        Text(
            text = "v1.0.0",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Diálogo de confirmación de salida
        if (showExitConfirmation) {
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
                        text = "ARE YOU SURE\nYOU WANT TO EXIT?",
                        style = TextStyle(
                            fontFamily = GameFonts.GameFont,
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    MenuButton(
                        text = "YES, EXIT",
                        onClick = onExit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuButton(
                        text = "NO, STAY",
                        onClick = { showExitConfirmation = false }
                    )
                }
            }
        }
    }
}

/**
 * Botón del menú con estilo retro arcade.
 */
@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    // Animación de escala al hover
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .width(280.dp)
            .height(60.dp)
            .scale(scale)
            .border(
                width = 3.dp,
                color = when {
                    isPressed -> Color(0xFFFF6B00)
                    isHovered -> Color(0xFFFFD700)
                    else -> Color.White
                },
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                brush = when {
                    isPressed -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                    )
                    isHovered -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF333333), Color(0xFF1A1A1A))
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))
                    )
                },
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
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 18.sp,
                color = when {
                    isPressed -> Color(0xFFFF6B00)
                    isHovered -> Color(0xFFFFD700)
                    else -> Color.White
                },
                fontWeight = FontWeight.Bold,
                shadow = if (isHovered) Shadow(
                    color = Color(0xFFFFD700).copy(alpha = 0.5f),
                    blurRadius = 8f
                ) else null
            )
        )
    }
}

/**
 * Logo animado con efecto de brillo y bounce sutil
 */
@Composable
private fun AnimatedLogo() {
    // Animación de bounce vertical
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de pulso de brillo
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.offset(y = offsetY.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CAT JUMP",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 48.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                    blurRadius = 20f,
                    offset = Offset.Zero
                )
            )
        )
        Text(
            text = "BARRELS",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 48.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                    blurRadius = 20f,
                    offset = Offset.Zero
                )
            )
        )
    }
}

/**
 * Partículas flotantes decorativas en el fondo
 */
@Composable
private fun FloatingParticles() {
    data class Particle(
        val x: Float,
        val y: Float,
        val size: Float,
        val speed: Float,
        val alpha: Float
    )

    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 2f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                alpha = Random.nextFloat() * 0.3f + 0.2f
            )
        }
    }

    var time by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            time += 0.016f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = size.width * particle.x
            val y = (size.height * particle.y + time * particle.speed * 100f) % size.height

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}
