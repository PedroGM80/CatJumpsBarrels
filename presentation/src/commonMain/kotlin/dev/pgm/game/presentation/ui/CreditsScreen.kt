package dev.pgm.game.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Pantalla de créditos con estilo arcade retro.
 */
@Composable
fun CreditsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Grid de fondo
        ArcadeGrid()

        // Scanlines
        CRTScanlines()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título animado
            AnimatedScreenTitle(text = "CREDITS", icon = "ℹ")

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secciones de créditos en formato compacto
                CreditCard(
                    icon = "👨‍💻",
                    title = "DEVELOPER",
                    items = listOf("Pedro GM"),
                    accentColor = Color(0xFF00FF88)
                )

                CreditCard(
                    icon = "⚙",
                    title = "TECHNOLOGIES",
                    items = listOf(
                        "Kotlin Multiplatform • Compose Desktop",
                        "Clean Architecture • Koin DI"
                    ),
                    accentColor = Color(0xFF00BFFF)
                )

                CreditCard(
                    icon = "🎮",
                    title = "INSPIRATION",
                    items = listOf(
                        "Donkey Kong (1981) by Nintendo",
                        "Atari 2600 Port by Coleco"
                    ),
                    accentColor = Color(0xFFFFD700)
                )

                CreditCard(
                    icon = "📝",
                    title = "ASSETS",
                    items = listOf(
                        "Font: Press Start 2P (Google Fonts)",
                        "Sound: Procedural Audio"
                    ),
                    accentColor = Color(0xFFFF6B6B)
                )

                CreditCard(
                    icon = "🔗",
                    title = "LINKS",
                    items = listOf(
                        "github.com/pgm/catjumpbarrels",
                        "License: MIT"
                    ),
                    accentColor = Color(0xFFAA88FF)
                )

                // Versión y agradecimiento
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "v1.0.0 • Thanks for playing!",
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 10.sp,
                        color = Color(0xFF00FFFF).copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón de retorno
            ArcadeButton(
                text = "BACK",
                icon = "◀",
                color = Color(0xFF00BFFF),
                onClick = onNavigateBack
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
        val lineColor = Color(0xFF00BFFF).copy(alpha = 0.03f)

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
 * Efecto scanlines CRT.
 */
@Composable
private fun CRTScanlines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 3f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineHeight
        }
    }
}

/**
 * Título de pantalla animado.
 */
@Composable
private fun AnimatedScreenTitle(text: String, icon: String) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            color = Color(0xFF00BFFF)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                shadow = Shadow(
                    color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                    blurRadius = 15f,
                    offset = Offset.Zero
                )
            )
        )
    }
}

/**
 * Tarjeta de créditos compacta.
 */
@Composable
private fun CreditCard(
    icon: String,
    title: String,
    items: List<String>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono
        Text(
            text = icon,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Título
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Items
            items.forEach { item ->
                Text(
                    text = item,
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

/**
 * Botón estilo arcade.
 */
@Composable
private fun ArcadeButton(
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
            .width(180.dp)
            .height(44.dp)
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
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.4f),
                        currentColor,
                        currentColor.copy(alpha = 0.4f)
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
                    fontSize = 14.sp,
                    color = currentColor
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
