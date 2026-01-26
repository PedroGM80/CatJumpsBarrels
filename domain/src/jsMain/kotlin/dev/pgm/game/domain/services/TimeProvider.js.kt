package dev.pgm.game.domain.services

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = js("Date.now()").unsafeCast<Double>().toLong()
}

actual fun systemTimeProvider(): TimeProvider = SystemTimeProvider()
