package dev.pgm.game.common

actual fun currentTimeMillis(): Long = js("Date.now()").unsafeCast<Double>().toLong()