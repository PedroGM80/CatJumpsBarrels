package dev.pgm.game.domain.services

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

actual fun systemTimeProvider(): TimeProvider = SystemTimeProvider()
