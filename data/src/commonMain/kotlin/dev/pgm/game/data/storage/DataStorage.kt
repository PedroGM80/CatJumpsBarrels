package dev.pgm.game.data.storage

/**
 * Interfaz multiplataforma para almacenamiento de datos.
 * JVM usa sistema de archivos, Web usa LocalStorage.
 */
expect object DataStorage {
    
    /**
     * Lee datos almacenados por clave.
     * @return contenido como String o null si no existe
     */
    fun read(key: String): String?
    
    /**
     * Guarda datos con una clave.
     */
    fun write(key: String, data: String)
    
    /**
     * Elimina datos por clave.
     */
    fun delete(key: String)
    
    /**
     * Verifica si existe una clave.
     */
    fun exists(key: String): Boolean
}
