package com.dutaeko.shinigamireader.data.source

class TimedMemoryCache<K, V>(
    private val ttlMillis: Long,
) {
    private data class Entry<V>(
        val value: V,
        val createdAt: Long,
    )

    private val store = LinkedHashMap<K, Entry<V>>()

    @Synchronized
    fun get(key: K): V? {
        val now = System.currentTimeMillis()
        val entry = store[key] ?: return null
        return if (now - entry.createdAt <= ttlMillis) {
            entry.value
        } else {
            store.remove(key)
            null
        }
    }

    @Synchronized
    fun put(key: K, value: V) {
        store[key] = Entry(value = value, createdAt = System.currentTimeMillis())
    }

    @Synchronized
    fun clear() {
        store.clear()
    }
}
