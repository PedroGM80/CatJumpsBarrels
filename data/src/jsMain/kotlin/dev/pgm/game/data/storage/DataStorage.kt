package dev.pgm.game.data.storage

import kotlinx.browser.localStorage

actual object DataStorage {
    actual fun read(key: String): String? {
        return localStorage.getItem(key)
    }

    actual fun write(key: String, data: String) {
        localStorage.setItem(key, data)
    }

    actual fun delete(key: String) {
        localStorage.removeItem(key)
    }

    actual fun exists(key: String): Boolean {
        return localStorage.getItem(key) != null
    }
}
