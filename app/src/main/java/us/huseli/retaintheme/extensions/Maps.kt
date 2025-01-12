@file:Suppress("unused")

package us.huseli.retaintheme.extensions

@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> = filterValues { it != null } as Map<K, V>

fun <K, V> Map<out K, V>.mergeWith(other: Map<out K, V>): Map<K, *> {
    /**
     * Merges with `other` recursively. Example:
     * val map1 = mapOf("key1" to mapOf("key1.1" to "value1.1", "key1.2" to "value1.2"), "key2" to "value2")
     * val map2 = mapOf("key1" to mapOf("key1.1" to "valueA"), "key3" to "value3")
     * map1.mergeWith(map2) = {"key1": {"key1.1": "valueA", "key1.2": "value1.2"}, "key2": "value2", "key3": "value3"}
     */
    val result: MutableMap<K, Any?> = this.toMutableMap()

    other.forEach { (key, value) ->
        val ownValue = this[key]

        if (ownValue is Map<*, *> && value is Map<*, *>) {
            result[key] = ownValue.mergeWith(value)
        } else result[key] = value
    }

    return result.toMap()
}

fun <K, V> MutableMap<K, V>.setOrMerge(key: K, value: V, mergeFunc: (oldValue: V, newValue: V) -> V): V {
    val oldValue = get(key)
    val newValue = oldValue?.let { mergeFunc(it, value) } ?: value

    this[key] = newValue
    return newValue
}

fun <K, V> Map<K, V>.takeIfNotEmpty(): Map<K, V>? = takeIf { it.isNotEmpty() }

