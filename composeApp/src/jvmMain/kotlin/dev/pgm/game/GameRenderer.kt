package dev.pgm.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catjumpsbarrels.composeapp.generated.resources.Res
import catjumpsbarrels.composeapp.generated.resources.background_industrial
import catjumpsbarrels.composeapp.generated.resources.barrel_fish
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import dev.pgm.game.model.utils.Particle
import dev.pgm.game.model.utils.ScorePopup
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sin

object GameColors {
    val backgroundGradientTop = Color(0xFF1B2838)
    val backgroundGradientBottom = Color(0xFF0D1B2A)
    val platformMain = Color(0xFFD84315)
    val platformDark = Color(0xFFBF360C)
    val ladderMain = Color(0xFFFFC107)
    val ladderDark = Color(0xFFFFA000)
    val playerBody = Color(0xFF1E88E5)
    val playerBodyDark = Color(0xFF1565C0)
    val playerFace = Color(0xFFFFE0B2)
    val playerHair = Color(0xFF5D4037)
    val dkBody = Color(0xFF6D4C41)
    val dkFace = Color(0xFF8D6E63)
    val barrelMain = Color(0xFF5D4037)
    val barrelDark = Color(0xFF3E2723)
    val barrelHighlight = Color(0xFF795548)
    val textPrimary = Color.White
    val textScore = Color(0xFFFFD700)
    val textLives = Color(0xFFFF5252)
    val textGameOver = Color(0xFFFF1744)
    val textWin = Color(0xFF00E676)
}

@Composable
fun GameRenderer(state: GameState, modifier: Modifier = Modifier) {
    val barrelFishPainter = painterResource(Res.drawable.barrel_fish)

    Box(modifier = modifier) {
        // Fondo de imagen
        Image(
            painter = painterResource(Res.drawable.background_industrial),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Capa semi-transparente para oscurecer un poco el fondo
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val levelWidth = GameConstants.LEVEL_WIDTH
            val offsetX = (screenWidth - levelWidth) / 2f

            withTransform({ translate(left = offsetX, top = 0f) }) {
                state.platforms.forEach { drawPlatform(it) }
                state.ladders.forEach { drawLadder(it) }
                drawDonkeyKong(state.enemy)
                drawPrincess(state.winObjetive, barrelFishPainter)
                state.barrels.forEach { drawBarrel(it) }
                drawPlayer(state.player)
                state.particles.forEach { drawParticle(it) }
                state.lastScorePopup?.let { drawScorePopup(it) }
            }
        }
        HUD(state)
        if (state.isGameOver) GameOverScreen(state)
        if (state.isWon) WinScreen(state)
        if (state.isPaused) PauseScreen()
    }
}

@Composable
private fun BoxScope.HUD(state: GameState) {
    val hudStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    Column(modifier = Modifier.align(Alignment.TopStart).padding(GameConstants.HUD_PADDING.dp)) {
        Text("SCORE", style = hudStyle.copy(color = GameColors.textPrimary.copy(alpha = 0.7f), fontSize = GameConstants.HUD_SCORE_TITLE_SIZE.sp))
        Text("${state.score}".padStart(6, '0'), style = hudStyle.copy(color = GameColors.textScore, fontSize = GameConstants.HUD_SCORE_VALUE_SIZE.sp))
        Spacer(modifier = Modifier.height(GameConstants.HUD_SPACER_HEIGHT.dp))
        Text("HIGH", style = hudStyle.copy(color = GameColors.textPrimary.copy(alpha = 0.7f), fontSize = GameConstants.HUD_SCORE_TITLE_SIZE.sp))
        Text("${state.highScore}".padStart(6, '0'), style = hudStyle.copy(color = GameColors.textPrimary, fontSize = GameConstants.HUD_HIGH_SCORE_VALUE_SIZE.sp))
    }
    Column(modifier = Modifier.align(Alignment.TopEnd).padding(GameConstants.HUD_PADDING.dp), horizontalAlignment = Alignment.End) {
        Text("LEVEL ${state.level}", style = hudStyle.copy(color = GameColors.textPrimary, fontSize = GameConstants.HUD_LEVEL_TEXT_SIZE.sp))
        Spacer(modifier = Modifier.height(GameConstants.HUD_SPACER_HEIGHT.dp))
        Row {
            repeat(state.lives) {
                Text("♥", style = TextStyle(color = GameColors.textLives, fontSize = GameConstants.HUD_LIVES_TEXT_SIZE.sp))
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
    // Controles en la parte inferior
    Column(
        modifier = Modifier.align(Alignment.BottomCenter).padding(GameConstants.HUD_PADDING.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "WASD/Arrows: Move | SPACE: Jump | P: Pause",
            style = hudStyle.copy(color = GameColors.textPrimary.copy(alpha = 0.5f), fontSize = 10.sp)
        )
    }
}

@Composable
private fun BoxScope.GameOverScreen(state: GameState) {
    val screenStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("GAME OVER", style = screenStyle.copy(color = GameColors.textGameOver, fontSize = GameConstants.GAMEOVER_TITLE_SIZE.sp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Final Score: ${state.score}", style = screenStyle.copy(color = GameColors.textScore, fontSize = GameConstants.GAMEOVER_SCORE_SIZE.sp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Press R to Restart", style = screenStyle.copy(color = GameColors.textPrimary.copy(alpha = 0.8f), fontSize = GameConstants.GAMEOVER_RESTART_SIZE.sp))
    }
}

@Composable
private fun BoxScope.WinScreen(state: GameState) {
    val screenStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("YOU WIN!", style = screenStyle.copy(color = GameColors.textWin, fontSize = GameConstants.WIN_TITLE_SIZE.sp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Score: ${state.score}", style = screenStyle.copy(color = GameColors.textScore, fontSize = GameConstants.WIN_SCORE_SIZE.sp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Press R to Play Again", style = screenStyle.copy(color = GameColors.textPrimary.copy(alpha = 0.8f), fontSize = 18.sp))
    }
}

@Composable
private fun BoxScope.PauseScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", style = TextStyle(color = GameColors.textPrimary, fontSize = GameConstants.PAUSE_TITLE_SIZE.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Press P to Resume", style = TextStyle(color = GameColors.textPrimary.copy(alpha = 0.8f), fontSize = 18.sp, fontFamily = FontFamily.Monospace))
        }
    }
}

private fun DrawScope.drawPlayer(player: Player) {
    val alpha = calculatePlayerAlpha(player)

    drawPlayerBody(player, alpha)
    drawPlayerHead(player, alpha)
    drawPlayerHair(player, alpha)
    drawPlayerEyes(player, alpha)

    if (player.state == PlayerState.WALKING) {
        drawPlayerLegs(player, alpha)
    }
}

private fun calculatePlayerAlpha(player: Player): Float {
    return if (player.isInvincible && (System.currentTimeMillis() / GameConstants.INVINCIBILITY_BLINK_INTERVAL) % 2 == 0L) {
        0.5f
    } else {
        1f
    }
}

private fun DrawScope.drawPlayerBody(player: Player, alpha: Float) {
    val bodyTopLeft = Offset(
        player.position.x + 4f,
        player.position.y + player.size * GameConstants.PLAYER_BODY_V_POS_SCALE
    )
    val bodySize = Size(player.size - 8f, player.size * GameConstants.PLAYER_BODY_H_SCALE)
    val cornerRadius = CornerRadius(6f)

    drawRoundRect(
        color = GameColors.playerBody.copy(alpha = alpha),
        topLeft = bodyTopLeft,
        size = bodySize,
        cornerRadius = cornerRadius
    )
    drawRoundRect(
        color = GameColors.playerBodyDark.copy(alpha = alpha),
        topLeft = bodyTopLeft,
        size = bodySize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawPlayerHead(player: Player, alpha: Float) {
    val headCenter = Offset(
        player.position.x + player.size / 2,
        player.position.y + player.size * GameConstants.PLAYER_HEAD_V_POS_SCALE
    )
    drawCircle(
        color = GameColors.playerFace.copy(alpha = alpha),
        radius = player.size * GameConstants.PLAYER_HEAD_RADIUS_SCALE,
        center = headCenter
    )
}

private fun DrawScope.drawPlayerHair(player: Player, alpha: Float) {
    val centerX = player.position.x + player.size / 2
    val centerY = player.position.y + player.size * GameConstants.PLAYER_HAIR_V_POS_SCALE

    val hairPath = Path().apply {
        moveTo(centerX - player.size * 0.25f, centerY + 5f)
        quadraticTo(centerX, centerY - player.size * 0.15f, centerX + player.size * 0.25f, centerY + 5f)
    }
    drawPath(
        path = hairPath,
        color = GameColors.playerHair.copy(alpha = alpha),
        style = Stroke(width = 6f)
    )
}

private fun DrawScope.drawPlayerEyes(player: Player, alpha: Float) {
    val eyeOffsetX = if (player.direction == Direction.RIGHT) {
        GameConstants.PLAYER_EYE_OFFSET_X
    } else {
        -GameConstants.PLAYER_EYE_OFFSET_X
    }
    val eyeY = player.position.y + player.size * GameConstants.PLAYER_HEAD_V_POS_SCALE
    val centerX = player.position.x + player.size / 2

    // Left eye
    drawCircle(color = Color.White.copy(alpha = alpha), radius = 4f, center = Offset(centerX - 6f + eyeOffsetX, eyeY))
    drawCircle(color = Color.Black.copy(alpha = alpha), radius = 2f, center = Offset(centerX - 6f + eyeOffsetX + 1f, eyeY))

    // Right eye
    drawCircle(color = Color.White.copy(alpha = alpha), radius = 4f, center = Offset(centerX + 6f + eyeOffsetX, eyeY))
    drawCircle(color = Color.Black.copy(alpha = alpha), radius = 2f, center = Offset(centerX + 6f + eyeOffsetX + 1f, eyeY))
}

private fun DrawScope.drawPlayerLegs(player: Player, alpha: Float) {
    val legOffset = sin(player.animationFrame * GameConstants.PLAYER_LEG_ANIM_SCALE) * 4f

    // Left leg
    drawRoundRect(
        color = GameColors.playerBodyDark.copy(alpha = alpha),
        topLeft = Offset(player.position.x + 8f, player.position.y + player.size - 8f + legOffset),
        size = Size(8f, 8f),
        cornerRadius = CornerRadius(2f)
    )

    // Right leg
    drawRoundRect(
        color = GameColors.playerBodyDark.copy(alpha = alpha),
        topLeft = Offset(player.position.x + player.size - 16f, player.position.y + player.size - 8f - legOffset),
        size = Size(8f, 8f),
        cornerRadius = CornerRadius(2f)
    )
}

private fun DrawScope.drawDonkeyKong(dk: Boss) {
    val pos = dk.position
    val size = dk.size
    drawOval(color = GameColors.dkBody, topLeft = Offset(pos.x + 5f, pos.y + size * GameConstants.ENEMY_BODY_V_POS_SCALE), size = Size(size - 10f, size * GameConstants.ENEMY_BODY_H_SCALE))
    drawOval(color = GameColors.dkFace, topLeft = Offset(pos.x + size * GameConstants.ENEMY_CHEST_V_POS_SCALE, pos.y + size * GameConstants.ENEMY_CHEST_H_SCALE), size = Size(size * GameConstants.ENEMY_CHEST_H_SCALE, size * GameConstants.ENEMY_CHEST_H_SCALE))
    drawCircle(color = GameColors.dkBody, radius = size * GameConstants.ENEMY_HEAD_RADIUS_SCALE, center = Offset(pos.x + size / 2, pos.y + size * GameConstants.ENEMY_HEAD_V_POS_SCALE))
    drawOval(color = GameColors.dkFace, topLeft = Offset(pos.x + size * 0.3f, pos.y + size * GameConstants.ENEMY_FACE_V_POS_SCALE), size = Size(size * GameConstants.ENEMY_FACE_H_SCALE, size * 0.25f))
    drawCircle(color = Color.White, radius = 7f, center = Offset(pos.x + size / 2 - 10f, pos.y + size * 0.18f))
    drawCircle(color = Color.White, radius = 7f, center = Offset(pos.x + size / 2 + 10f, pos.y + size * 0.18f))
    drawCircle(color = Color.Black, radius = 4f, center = Offset(pos.x + size / 2 - 8f, pos.y + size * 0.18f))
    drawCircle(color = Color.Black, radius = 4f, center = Offset(pos.x + size / 2 + 12f, pos.y + size * 0.18f))
    drawLine(color = Color.Black, start = Offset(pos.x + size / 2 - 18f, pos.y + size * 0.1f), end = Offset(pos.x + size / 2 - 5f, pos.y + size * 0.08f), strokeWidth = 3f)
    drawLine(color = Color.Black, start = Offset(pos.x + size / 2 + 5f, pos.y + size * 0.08f), end = Offset(pos.x + size / 2 + 18f, pos.y + size * 0.1f), strokeWidth = 3f)
    drawArc(color = Color.Black, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(pos.x + size * 0.35f, pos.y + size * 0.28f), size = Size(size * 0.3f, size * 0.1f), style = Stroke(width = 2f))
    val armOffset = if (dk.isThrowingBarrel) GameConstants.ENEMY_ARM_THROW_OFFSET else 0f
    drawOval(color = GameColors.dkBody, topLeft = Offset(pos.x - 10f, pos.y + size * 0.4f + armOffset), size = Size(20f, 35f))
    drawOval(color = GameColors.dkBody, topLeft = Offset(pos.x + size - 10f, pos.y + size * 0.4f + armOffset), size = Size(20f, 35f))
}

private fun DrawScope.drawPrincess(winObjetive: WinObjetive, painter: Painter) {
    withTransform({
        translate(
            left = winObjetive.position.x,
            top = winObjetive.position.y
        )
    }) {
        with(painter) {
            draw(Size(winObjetive.size, winObjetive.size))
        }
    }
}

private fun DrawScope.drawBarrel(barrel: Barrel) {
    withTransform({
        rotate(degrees = barrel.rotation * 57.3f, pivot = Offset(barrel.position.x + barrel.size / 2, barrel.position.y + barrel.size / 2))
    }) {
        val pos = barrel.position
        val size = GameConstants.BARREL_SIZE
        drawOval(color = GameColors.barrelMain, topLeft = pos, size = Size(size, size))
        drawOval(color = GameColors.barrelDark, topLeft = pos, size = Size(size, size), style = Stroke(width = 2f))
        drawLine(color = GameColors.barrelHighlight, start = Offset(pos.x + 3f, pos.y + size * 0.3f), end = Offset(pos.x + size - 3f, pos.y + size * 0.3f), strokeWidth = 2f)
        drawLine(color = GameColors.barrelHighlight, start = Offset(pos.x + 3f, pos.y + size * 0.7f), end = Offset(pos.x + size - 3f, pos.y + size * 0.7f), strokeWidth = 2f)
        drawCircle(color = GameColors.barrelDark, radius = 4f, center = Offset(pos.x + size / 2, pos.y + size / 2))
    }
}

private fun DrawScope.drawPlatform(platform: Platform) {
    val startY = platform.getYAt(platform.left)
    val endY = platform.getYAt(platform.right)
    val platformPath = Path().apply {
        moveTo(platform.left, startY)
        lineTo(platform.right, endY)
        lineTo(platform.right, endY + platform.height)
        lineTo(platform.left, startY + platform.height)
        close()
    }
    drawPath(platformPath, color = GameColors.platformMain)
    drawPath(platformPath, color = GameColors.platformDark, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawLadder(ladder: Ladder) {
    val pos = ladder.position
    val w = ladder.width
    val h = ladder.height
    val leftX = pos.x + 4f
    val rightX = pos.x + w - 4f
    drawLine(color = GameColors.ladderMain, start = Offset(leftX, pos.y), end = Offset(leftX, pos.y + h), strokeWidth = 5f)
    drawLine(color = GameColors.ladderDark, start = Offset(leftX + 2f, pos.y), end = Offset(leftX + 2f, pos.y + h), strokeWidth = 1f)
    drawLine(color = GameColors.ladderMain, start = Offset(rightX, pos.y), end = Offset(rightX, pos.y + h), strokeWidth = 5f)
    drawLine(color = GameColors.ladderDark, start = Offset(rightX - 2f, pos.y), end = Offset(rightX - 2f, pos.y + h), strokeWidth = 1f)
    var y = pos.y + 15f
    while (y < pos.y + h - 5f) {
        drawRoundRect(color = GameColors.ladderMain, topLeft = Offset(leftX, y - 2f), size = Size(rightX - leftX, 5f), cornerRadius = CornerRadius(1f))
        drawLine(color = GameColors.ladderDark, start = Offset(leftX, y + 3f), end = Offset(rightX, y + 3f), strokeWidth = 1f)
        y += 18f
    }
}

private fun DrawScope.drawParticle(particle: Particle) {
    drawCircle(color = Color(particle.color).copy(alpha = particle.alpha), radius = particle.size * particle.alpha, center = particle.position)
}

private fun DrawScope.drawScorePopup(popup: ScorePopup) {
    val elapsed = System.currentTimeMillis() - popup.createdAt
    val yOffset = elapsed * GameConstants.SCORE_POPUP_Y_SPEED
    val alpha = 1f - (elapsed / GameConstants.SCORE_POPUP_LIFETIME_MS)
    if (alpha > 0) {
        drawCircle(color = Color.Black.copy(alpha = alpha * 0.5f), radius = 20f, center = Offset(popup.position.x + 2f, popup.position.y - yOffset + 2f))
    }
}
