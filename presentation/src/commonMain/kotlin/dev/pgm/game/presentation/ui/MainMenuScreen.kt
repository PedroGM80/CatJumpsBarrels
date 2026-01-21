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
                icon = "▶",
                onClick = onNavigateToGame
            )

            MenuButton(
                text = "HIGH SCORES",
                icon = "★",
                onClick = onNavigateToHighScores
            )

            MenuButton(
                text = "CREDITS",
                icon = "ℹ",
                onClick = onNavigateToCredits
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "EXIT",
                icon = "✕",
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
                        icon = "✓",
                        onClick = onExit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuButton(
                        text = "NO, STAY",
                        icon = "✕",
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
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    // Animación de escala al hover
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .width(300.dp)
            .height(65.dp)
            .scale(scale)
            .border(
                width = 4.dp,
                brush = when {
                    isPressed -> Brush.linearGradient(
                        colors = listOf(Color(0xFFFF6B00), Color(0xFFFF0000))
                    )
                    isHovered -> Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                    )
                    else -> Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFCCCCCC))
                    )
                },
                shape = RoundedCornerShape(12.dp)
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
                shape = RoundedCornerShape(12.dp)
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
            // Icono
            Text(
                text = icon,
                style = TextStyle(
                    fontSize = 24.sp,
                    color = when {
                        isPressed -> Color(0xFFFF6B00)
                        isHovered -> Color(0xFFFFD700)
                        else -> Color(0xFF00FFFF)
                    },
                    shadow = if (isHovered) Shadow(
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        blurRadius = 15f
                    ) else null
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Texto
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 20.sp,
                    color = when {
                        isPressed -> Color(0xFFFF6B00)
                        isHovered -> Color(0xFFFFD700)
                        else -> Color.White
                    },
                    fontWeight = FontWeight.Bold,
                    shadow = if (isHovered) Shadow(
                        color = Color(0xFFFFD700).copy(alpha = 0.7f),
                        blurRadius = 12f
                    ) else null
                )
            )
        }
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
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de pulso de brillo
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de escala
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título principal con múltiples sombras para efecto 3D
        Box(contentAlignment = Alignment.Center) {
            // Sombra trasera (efecto 3D)
            Text(
                text = "CAT JUMP",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 72.sp,
                    color = Color(0xFF8B4513),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.offset(x = 4.dp, y = 4.dp)
            )
            // Texto principal con glow
            Text(
                text = "CAT JUMP",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 72.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                        blurRadius = 30f,
                        offset = Offset.Zero
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(contentAlignment = Alignment.Center) {
            // Sombra trasera
            Text(
                text = "BARRELS",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 72.sp,
                    color = Color(0xFF8B4513),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.offset(x = 4.dp, y = 4.dp)
            )
            // Texto principal
            Text(
                text = "BARRELS",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 72.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                        blurRadius = 30f,
                        offset = Offset.Zero
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtítulo
        Text(
            text = "~ A Retro Arcade Adventure ~",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 14.sp,
                color = Color(0xFF00FFFF),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xFF00FFFF).copy(alpha = 0.5f),
                    blurRadius = 10f,
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
        val alpha: Float,
        val colorHue: Int // 0=gold, 1=cyan, 2=magenta
    )

    val particles = remember {
        List(50) { // Más partículas
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1f,
                speed = Random.nextFloat() * 0.8f + 0.3f,
                alpha = Random.nextFloat() * 0.4f + 0.3f,
                colorHue = Random.nextInt(3)
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Partículas
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val x = size.width * particle.x
                val y = (size.height * particle.y + time * particle.speed * 100f) % size.height

                val color = when (particle.colorHue) {
                    0 -> Color(0xFFFFD700) // Dorado
                    1 -> Color(0xFF00FFFF) // Cian
                    else -> Color(0xFFFF00FF) // Magenta
                }.copy(alpha = particle.alpha * (0.7f + 0.3f * sin(time * 2f + particle.x * 10f)))

                drawCircle(
                    color = color,
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }

        // Efecto scanlines CRT
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineHeight = 4f
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.15f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f
                )
                y += lineHeight
            }
        }

        // Vignette en bordes para profundidad
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f)
                    ),
                    center = center,
                    radius = size.maxDimension * 0.9f
                )
            )
        }
    }
}
