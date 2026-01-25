package dev.pgm.game.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Dialog
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.presentation.utils.formatWithCommas

/**
 * Diálogo para ingresar el nombre del jugador cuando logra un high score.
 * Estilo arcade retro mejorado.
 */
@Composable
fun HighScoreDialog(
    score: Int,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playerName by remember { mutableStateOf("") }
    
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF0D0D1A),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFF6B00),
                            Color(0xFFFFD700)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono de trofeo
                Text(
                    text = "🏆",
                    fontSize = 48.sp
                )

                // Título con glow
                Text(
                    text = "NEW HIGH SCORE!",
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 22.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                            blurRadius = 15f,
                            offset = Offset.Zero
                        )
                    )
                )

                // Puntuación destacada
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00FF88).copy(alpha = 0.1f))
                        .border(
                            2.dp,
                            Color(0xFF00FF88).copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = score.formatWithCommas(),
                        style = TextStyle(
                            fontFamily = GameFonts.GameFont,
                            fontSize = 36.sp,
                            color = Color(0xFF00FF88),
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color(0xFF00FF88).copy(alpha = 0.5f),
                                blurRadius = 10f
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtítulo
                Text(
                    text = "Enter your name for the leaderboard",
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    ),
                    textAlign = TextAlign.Center
                )

                // Campo de texto estilizado
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { if (it.length <= 15) playerName = it.uppercase() },
                    placeholder = {
                        Text(
                            "YOUR NAME",
                            style = TextStyle(
                                fontFamily = GameFonts.GameFont,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF4A4A6A),
                        cursorColor = Color(0xFFFFD700),
                        focusedContainerColor = Color(0xFF2A2A4A).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF2A2A4A).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Contador de caracteres
                Text(
                    text = "${playerName.length}/15",
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DialogButton(
                        text = "SKIP",
                        color = Color(0xFF6A6A8A),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    DialogButton(
                        text = "SAVE",
                        color = Color(0xFFFFD700),
                        enabled = playerName.isNotBlank(),
                        onClick = {
                            if (playerName.isNotBlank()) {
                                onSave(playerName.trim())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Botón de diálogo estilizado.
 */
@Composable
private fun DialogButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val actualColor = if (enabled) color else color.copy(alpha = 0.3f)
    val textColor = if (text == "SAVE" && enabled) Color.Black else Color.White

    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (text == "SAVE" && enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500),
                            Color(0xFFFFD700)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            actualColor.copy(alpha = 0.3f),
                            actualColor.copy(alpha = 0.5f),
                            actualColor.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            .border(
                2.dp,
                actualColor.copy(alpha = if (enabled) 0.8f else 0.3f),
                RoundedCornerShape(8.dp)
            )
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 14.sp,
                color = if (enabled) textColor else Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        )
    }
}
