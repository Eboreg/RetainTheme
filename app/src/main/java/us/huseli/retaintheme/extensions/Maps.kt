@file:Suppress("unused")

package us.huseli.retaintheme.extensions

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Map<*, *>.filterKeysOfType(): Map<T, *> = filterKeys { it is T } as Map<T, *>

@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> = filterValues { it != null } as Map<K, V>

inline fun <reified T> Map<*, *>.getAll(vararg paths: Any): Sequence<T> = sequence {
    /**
     * `paths` is typically strings or lists of strings.
     *
     * Example maps:
     * A = {"bar": 123}
     * B = {"foo": {"bar": 123}}
     * C = {"foo": {"baz": {"bar": 123}}}
     *
     * Example 1: getAll("bar")
     * Only matches for maps with a "bar" key on level 0, i.e. only map A.
     *
     * Example 2: getAll(listOf("*", "bar"))
     * Matches for maps with any key on level 0 and "bar" on level 1, i.e. only map B.
     *
     * Example 3: getAll(listOf("**", "bar"))
     * "**" traverses any number of levels, so this will match for all maps with a "bar"
     * key on any level, i.e. all 3 maps above.
     */
    for (pathCandidate in paths) {
        val maps: MutableList<Map<*, *>> = mutableListOf(this@getAll)
        var lastWasGlob = false
        val path: Collection<*> = pathCandidate as? Collection<*> ?: listOf(pathCandidate)

        for ((index, key) in path.withIndex()) {
            if (key == "**") {
                lastWasGlob = true
                continue
            }

            val values = when {
                key == "*" -> maps.flatMap { it.values }
                lastWasGlob -> maps.flatMap { it.getAllRecursive(key) }
                else -> maps.mapNotNull { it[key] }
            }

            maps.clear()
            lastWasGlob = false

            for (value in values) {
                if (value != null) {
                    if (index == path.size - 1 && value is T) yield(value)
                    if (value is Map<*, *>) maps.add(value)
                }
            }
        }
    }
}

inline fun <reified T> Iterable<Map<*, *>>.getAll(vararg paths: Any): List<T> = flatMap { it.getAll<T>(*paths) }

fun Map<*, *>.getAllRecursive(key: Any?): List<Any> {
    val found = mutableListOf<Any>()

    this[key]?.also { found.add(it) }
    for (value in values) {
        if (value is Map<*, *>) {
            found.addAll(value.getAllRecursive(key))
        }
    }

    return found.toList()
}

inline fun <reified T> Map<*, *>.getFirst(vararg paths: Any): T? = getAll<T>(*paths).firstOrNull()

inline fun <reified T> Iterable<Map<*, *>>.getFirst(vararg paths: Any): T? =
    firstNotNullOfOrNull { it.getFirst<T>(*paths) }

inline fun <reified T> Array<out Map<*, *>>.getFirst(vararg paths: Any): T? =
    firstNotNullOfOrNull { it.getFirst<T>(*paths) }

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

fun Map<*, *>.recursiveUpdate(other: Map<*, *>): Map<*, *> {
    val result = toMutableMap()

    for ((key, otherValue) in other) {
        val thisValue = this[key]

        if (otherValue is Map<*, *> && thisValue is Map<*, *>) {
            result[key] = thisValue.recursiveUpdate(otherValue)
        } else if (otherValue != null) result[key] = otherValue
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
