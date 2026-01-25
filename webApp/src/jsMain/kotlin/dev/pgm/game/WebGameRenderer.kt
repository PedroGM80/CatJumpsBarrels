package dev.pgm.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.*
import dev.pgm.game.model.utils.Particle

object WebGameColors {
    val background = Color(0xFF1A1A2E)
    val platform = Color(0xFF8B4513)
    val platformHighlight = Color(0xFFA0522D)
    val ladder = Color(0xFF4A4A4A)
    val ladderRung = Color(0xFF696969)
    val player = Color(0xFFFF9800)
    val boss = Color(0xFF8B0000)
    val barrel = Color(0xFF5D4037)
    val barrelDark = Color(0xFF3E2723)
    val barrelHighlight = Color(0xFF795548)
    val winObjective = Color(0xFFFFD700)
    val textPrimary = Color.White
    val textScore = Color(0xFFFFD700)
    val textLives = Color(0xFFFF5252)
    val textGameOver = Color(0xFFFF1744)
    val textWin = Color(0xFF00E676)
}

@Composable
fun WebGameRenderer(state: GameState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(WebGameColors.background)) {
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
                state.platforms.forEach { drawPlatformSimple(it) }
                state.ladders.forEach { drawLadderSimple(it) }
                drawBossSimple(state.enemy)
                drawWinObjectiveSimple(state.winObjetive)
                state.barrels.forEach { drawBarrelSimple(it) }
                drawPlayerSimple(state.player)
                state.particles.forEach { drawParticleSimple(it) }
            }
        }
        WebHUD(state)
        if (state.isGameOver) WebGameOverScreen(state)
        if (state.isWon) WebWinScreen(state)
        if (state.isPaused) WebPauseScreen()
    }
}

@Composable
private fun BoxScope.WebHUD(state: GameState) {
    Column(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
        Text("SCORE", color = WebGameColors.textPrimary.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${state.score}".padStart(6, '0'), color = WebGameColors.textScore, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("HIGH", color = WebGameColors.textPrimary.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${state.highScore}".padStart(6, '0'), color = WebGameColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
    Column(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp), horizontalAlignment = Alignment.End) {
        Text("LEVEL ${state.level}", color = WebGameColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            repeat(state.lives) {
                Text("\u2764", color = WebGameColors.textLives, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
    Text("WASD/Arrows: Move | SPACE: Jump | P: Pause | ESC: Menu",
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        color = WebGameColors.textPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
}

@Composable
private fun BoxScope.WebGameOverScreen(state: GameState) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", color = WebGameColors.textGameOver, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Final Score: ${state.score}", color = WebGameColors.textScore, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Press R to Restart", color = WebGameColors.textPrimary.copy(alpha = 0.8f), fontSize = 16.sp)
        }
    }
}

@Composable
private fun BoxScope.WebWinScreen(state: GameState) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LEVEL COMPLETE!", color = WebGameColors.textWin, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Score: ${state.score}", color = WebGameColors.textScore, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Press R to Continue", color = WebGameColors.textPrimary.copy(alpha = 0.8f), fontSize = 16.sp)
        }
    }
}

@Composable
private fun BoxScope.WebPauseScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", color = WebGameColors.textPrimary, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Press P to Resume", color = WebGameColors.textPrimary.copy(alpha = 0.8f), fontSize = 16.sp)
        }
    }
}

private fun DrawScope.drawPlayerSimple(player: Player) {
    val alpha = if (player.isInvincible && (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() / 150) % 2 == 0L) 0.5f else 1f
    withTransform({
        translate(left = player.position.x, top = player.position.y)
        if (player.direction == Direction.LEFT) {
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(player.size / 2, player.size / 2))
        }
    }) {
        drawRoundRect(color = WebGameColors.player.copy(alpha = alpha), topLeft = Offset(player.size * 0.15f, player.size * 0.3f),
            size = Size(player.size * 0.7f, player.size * 0.6f), cornerRadius = CornerRadius(8f, 8f))
        drawCircle(color = WebGameColors.player.copy(alpha = alpha), radius = player.size * 0.25f,
            center = Offset(player.size * 0.5f, player.size * 0.25f))
        val earPath = Path().apply {
            moveTo(player.size * 0.25f, player.size * 0.15f)
            lineTo(player.size * 0.35f, player.size * 0.0f)
            lineTo(player.size * 0.45f, player.size * 0.15f)
            close()
        }
        drawPath(earPath, WebGameColors.player.copy(alpha = alpha))
        val ear2Path = Path().apply {
            moveTo(player.size * 0.55f, player.size * 0.15f)
            lineTo(player.size * 0.65f, player.size * 0.0f)
            lineTo(player.size * 0.75f, player.size * 0.15f)
            close()
        }
        drawPath(ear2Path, WebGameColors.player.copy(alpha = alpha))
        drawCircle(color = Color.White.copy(alpha = alpha), radius = 3f, center = Offset(player.size * 0.4f, player.size * 0.22f))
        drawCircle(color = Color.White.copy(alpha = alpha), radius = 3f, center = Offset(player.size * 0.6f, player.size * 0.22f))
        drawCircle(color = Color.Black.copy(alpha = alpha), radius = 1.5f, center = Offset(player.size * 0.4f, player.size * 0.22f))
        drawCircle(color = Color.Black.copy(alpha = alpha), radius = 1.5f, center = Offset(player.size * 0.6f, player.size * 0.22f))
    }
}

private fun DrawScope.drawBossSimple(boss: Boss) {
    withTransform({ translate(left = boss.position.x, top = boss.position.y) }) {
        drawRoundRect(color = WebGameColors.boss, topLeft = Offset(boss.size * 0.1f, boss.size * 0.3f),
            size = Size(boss.size * 0.8f, boss.size * 0.65f), cornerRadius = CornerRadius(12f, 12f))
        drawCircle(color = WebGameColors.boss, radius = boss.size * 0.3f, center = Offset(boss.size * 0.5f, boss.size * 0.25f))
        drawCircle(color = Color.White, radius = 6f, center = Offset(boss.size * 0.35f, boss.size * 0.2f))
        drawCircle(color = Color.White, radius = 6f, center = Offset(boss.size * 0.65f, boss.size * 0.2f))
        drawCircle(color = Color.Red, radius = 3f, center = Offset(boss.size * 0.35f, boss.size * 0.2f))
        drawCircle(color = Color.Red, radius = 3f, center = Offset(boss.size * 0.65f, boss.size * 0.2f))
    }
}

private fun DrawScope.drawBarrelSimple(barrel: Barrel) {
    withTransform({
        rotate(degrees = barrel.rotation * 57.3f, pivot = Offset(barrel.position.x + barrel.size / 2, barrel.position.y + barrel.size / 2))
    }) {
        val pos = barrel.position
        val size = barrel.size
        drawOval(color = WebGameColors.barrel, topLeft = pos, size = Size(size, size))
        drawOval(color = WebGameColors.barrelDark, topLeft = pos, size = Size(size, size), style = Stroke(width = 2f))
        drawLine(color = WebGameColors.barrelHighlight, start = Offset(pos.x + 3f, pos.y + size * 0.3f),
            end = Offset(pos.x + size - 3f, pos.y + size * 0.3f), strokeWidth = 2f)
        drawLine(color = WebGameColors.barrelHighlight, start = Offset(pos.x + 3f, pos.y + size * 0.7f),
            end = Offset(pos.x + size - 3f, pos.y + size * 0.7f), strokeWidth = 2f)
        drawCircle(color = WebGameColors.barrelDark, radius = 4f, center = Offset(pos.x + size / 2, pos.y + size / 2))
    }
}

private fun DrawScope.drawPlatformSimple(platform: Platform) {
    val startY = platform.getYAt(platform.left)
    val angleRad = kotlin.math.atan(platform.slope)
    val angleDeg = angleRad * 180f / kotlin.math.PI.toFloat()
    withTransform({
        translate(left = platform.left, top = startY)
        rotate(degrees = angleDeg, pivot = Offset(0f, 0f))
    }) {
        drawRoundRect(color = WebGameColors.platform, topLeft = Offset.Zero,
            size = Size(platform.width, platform.height), cornerRadius = CornerRadius(4f, 4f))
        drawLine(color = WebGameColors.platformHighlight, start = Offset(2f, 2f),
            end = Offset(platform.width - 2f, 2f), strokeWidth = 2f)
    }
}

private fun DrawScope.drawLadderSimple(ladder: Ladder) {
    val pos = ladder.position
    val w = ladder.width
    val h = ladder.height
    val leftX = pos.x + 4f
    val rightX = pos.x + w - 4f
    drawRect(color = WebGameColors.ladder, topLeft = Offset(leftX, pos.y), size = Size(5f, h))
    drawRect(color = WebGameColors.ladder, topLeft = Offset(rightX, pos.y), size = Size(5f, h))
    var y = pos.y + 15f
    while (y < pos.y + h - 5f) {
        drawRect(color = WebGameColors.ladderRung, topLeft = Offset(leftX, y - 2f), size = Size(rightX - leftX + 5f, 4f))
        y += 18f
    }
}

private fun DrawScope.drawWinObjectiveSimple(winObjective: WinObjetive) {
    withTransform({ translate(left = winObjective.position.x, top = winObjective.position.y) }) {
        drawCircle(color = WebGameColors.winObjective, radius = winObjective.size / 2,
            center = Offset(winObjective.size / 2, winObjective.size / 2))
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = winObjective.size / 3,
            center = Offset(winObjective.size / 2, winObjective.size / 2))
        val starPath = Path().apply {
            val cx = winObjective.size / 2
            val cy = winObjective.size / 2
            val outerRadius = winObjective.size * 0.4f
            val innerRadius = winObjective.size * 0.2f
            for (i in 0 until 5) {
                val outerAngle = (i * 72 - 90) * kotlin.math.PI / 180
                val innerAngle = ((i * 72) + 36 - 90) * kotlin.math.PI / 180
                val outerX = cx + (outerRadius * kotlin.math.cos(outerAngle)).toFloat()
                val outerY = cy + (outerRadius * kotlin.math.sin(outerAngle)).toFloat()
                val innerX = cx + (innerRadius * kotlin.math.cos(innerAngle)).toFloat()
                val innerY = cy + (innerRadius * kotlin.math.sin(innerAngle)).toFloat()
                if (i == 0) moveTo(outerX, outerY) else lineTo(outerX, outerY)
                lineTo(innerX, innerY)
            }
            close()
        }
        drawPath(starPath, Color.White)
    }
}

private fun DrawScope.drawParticleSimple(particle: Particle) {
    val baseColor = Color(particle.color)
    val combinedAlpha = baseColor.alpha * particle.alpha
    drawCircle(color = baseColor.copy(alpha = combinedAlpha), radius = particle.size * particle.alpha, center = particle.position)
}
