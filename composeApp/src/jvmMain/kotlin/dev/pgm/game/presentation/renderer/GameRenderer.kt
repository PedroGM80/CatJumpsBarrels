package dev.pgm.game.presentation.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catjumpsbarrels.composeapp.generated.resources.*
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import dev.pgm.game.model.utils.Particle
import dev.pgm.game.model.utils.ScorePopup
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.resources.AnimationProvider
import org.jetbrains.compose.resources.painterResource

object GameColors {
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
    val platformPainter = painterResource(Res.drawable.platform)
    val ironTexturePainter = painterResource(Res.drawable.iron_texture)

    // Get animations from provider
    val catAnimations = if (AnimationProvider.isLoaded()) AnimationProvider.getCatAnimations() else emptyMap()
    val bossAnimations = if (AnimationProvider.isLoaded()) AnimationProvider.getBossAnimations() else emptyMap()
    val barrelEmptyBitmap = if (AnimationProvider.isLoaded()) AnimationProvider.getBarrelEmptyBitmap() else null

    Box(modifier = modifier) {
        Image(
            painter = painterResource(Res.drawable.background_industrial),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height
            val levelWidth = GameConstants.LEVEL_WIDTH
            val levelHeight = GameConstants.LEVEL_HEIGHT

            val scaleX = screenWidth / levelWidth
            val scaleY = screenHeight / levelHeight
            val scale = minOf(scaleX, scaleY)

            val scaledWidth = levelWidth * scale
            val scaledHeight = levelHeight * scale
            val offsetX = (screenWidth - scaledWidth) / 2f
            val offsetY = (screenHeight - scaledHeight) / 2f

            withTransform({
                translate(left = offsetX, top = offsetY)
                scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
            }) {
                state.platforms.forEach { drawPlatform(it, platformPainter) }
                state.ladders.forEach { drawLadder(it, ironTexturePainter) }
                drawDonkeyKong(state.enemy, bossAnimations, barrelEmptyBitmap)
                drawPrincess(state.winObjetive, barrelFishPainter)
                state.barrels.forEach { drawBarrel(it) }
                drawPlayer(state.player, catAnimations)
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
    val hudStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = GameFonts.GameFont)
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
    Column(
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "CONTROLS",
            style = TextStyle(
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GameFonts.GameFont
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "WASD/Arrows: Move  •  SPACE: Jump  •  P: Pause",
            style = TextStyle(
                color = Color(0xFFFFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GameFonts.GameFont
            )
        )
    }
}

@Composable
private fun BoxScope.GameOverScreen(state: GameState) {
    val screenStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = GameFonts.GameFont)
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
    val screenStyle = TextStyle(fontWeight = FontWeight.Bold, fontFamily = GameFonts.GameFont)
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
            Text("PAUSED", style = TextStyle(color = GameColors.textPrimary, fontSize = GameConstants.PAUSE_TITLE_SIZE.sp, fontWeight = FontWeight.Bold, fontFamily = GameFonts.GameFont))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Press P to Resume", style = TextStyle(color = GameColors.textPrimary.copy(alpha = 0.8f), fontSize = 18.sp, fontFamily = GameFonts.GameFont))
        }
    }
}

private fun DrawScope.drawPlayer(
    player: Player,
    catAnimations: Map<PlayerState, List<ImageBitmap>>
) {
    val animation = catAnimations[player.state] ?: catAnimations[PlayerState.IDLE] ?: return
    if (animation.isEmpty()) return

    val frameIndex = player.animationFrame % animation.size
    val image = animation[frameIndex]

    val alpha = if (player.isInvincible && (System.currentTimeMillis() / 150) % 2 == 0L) 0.5f else 1f

    withTransform({
        translate(left = player.position.x, top = player.position.y)
        if (player.direction == Direction.LEFT) {
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(player.size / 2, player.size / 2))
        }
    }) {
        drawImage(
            image = image,
            dstSize = IntSize(player.size.toInt(), player.size.toInt()),
            alpha = alpha
        )
    }
}

private fun DrawScope.drawDonkeyKong(
    dk: Boss,
    bossAnimations: Map<BossState, List<ImageBitmap>>,
    barrelEmptyBitmap: ImageBitmap?
) {
    val animation = bossAnimations[dk.state] ?: bossAnimations[BossState.IDLE] ?: return
    if (animation.isEmpty()) return

    val frameIndex = dk.animationFrame % animation.size
    val image = animation[frameIndex]

    barrelEmptyBitmap?.let { drawBarrelStackBehindBoss(dk, it) }

    withTransform({
        translate(left = dk.position.x, top = dk.position.y)
    }) {
        drawImage(
            image = image,
            dstSize = IntSize(dk.size.toInt(), dk.size.toInt())
        )
    }
}

private fun DrawScope.drawBarrelStackBehindBoss(dk: Boss, barrelImage: ImageBitmap) {
    val barrelSize = 25f
    val barrelSpacing = 28f
    val verticalSpacing = 12f

    val stackY = dk.position.y + dk.size - barrelSize
    val startX = dk.position.x + barrelSize * 6

    for (row in 0..6) {
        val barrelsInRow = 7 - row
        val rowY = stackY - (row * verticalSpacing)
        val rowStartX = startX - (row * (barrelSpacing / 2f))

        for (i in 0 until barrelsInRow) {
            val barrelX = rowStartX - (i * barrelSpacing)

            withTransform({
                translate(left = barrelX, top = rowY)
            }) {
                drawImage(
                    image = barrelImage,
                    dstSize = IntSize(barrelSize.toInt(), barrelSize.toInt())
                )
            }
        }
    }
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

private fun DrawScope.drawPlatform(platform: Platform, painter: Painter) {
    val startY = platform.getYAt(platform.left)
    val angleRad = kotlin.math.atan(platform.slope)
    val angleDeg = angleRad * 180f / kotlin.math.PI.toFloat()

    withTransform({
        translate(left = platform.left, top = startY)
        rotate(degrees = angleDeg, pivot = Offset(0f, 0f))
    }) {
        with(painter) {
            draw(size = Size(platform.width, platform.height))
        }
    }
}

private fun DrawScope.drawLadder(ladder: Ladder, texturePainter: Painter) {
    val pos = ladder.position
    val w = ladder.width
    val h = ladder.height
    val leftX = pos.x + 4f
    val rightX = pos.x + w - 4f

    withTransform({
        translate(left = leftX, top = pos.y)
    }) {
        with(texturePainter) {
            draw(Size(5f, h))
        }
    }

    withTransform({
        translate(left = rightX, top = pos.y)
    }) {
        with(texturePainter) {
            draw(Size(5f, h))
        }
    }

    var y = pos.y + 15f
    while (y < pos.y + h - 5f) {
        withTransform({
            translate(left = leftX, top = y - 2f)
        }) {
            with(texturePainter) {
                draw(Size(rightX - leftX, 5f))
            }
        }
        y += 18f
    }
}

private fun DrawScope.drawParticle(particle: Particle) {
    val baseColor = Color(particle.color)
    val combinedAlpha = baseColor.alpha * particle.alpha
    drawCircle(color = baseColor.copy(alpha = combinedAlpha), radius = particle.size * particle.alpha, center = particle.position)
}

private fun DrawScope.drawScorePopup(popup: ScorePopup) {
    val elapsed = System.currentTimeMillis() - popup.createdAt
    val yOffset = elapsed * GameConstants.SCORE_POPUP_Y_SPEED
    val alpha = 1f - (elapsed / GameConstants.SCORE_POPUP_LIFETIME_MS)
    if (alpha > 0) {
        drawCircle(color = Color.Black.copy(alpha = alpha * 0.25f), radius = 8f, center = Offset(popup.position.x + 2f, popup.position.y - yOffset + 2f))
    }
}
