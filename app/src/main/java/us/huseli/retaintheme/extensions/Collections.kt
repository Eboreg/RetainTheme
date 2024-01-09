@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import kotlin.math.roundToInt

fun <T> List<T>.circular(offset: Int, length: Int): List<T> {
    /**
     * Will always return a list of `length` elements. If the list is shorter
     * than `offset + length`, elements will be added by starting over from
     * the beginning again.
     */
    val realOffset = offset % size
    if (realOffset + length > size)
        return subList(realOffset, size) + circular(0, length - size + realOffset)
    return subList(realOffset, realOffset + length)
}

fun <T> Iterable<T>.clone(): MutableList<T> = mutableListOf<T>().also { it.addAll(this) }

inline fun <T> Iterable<T>.combineEquals(predicate: (a: T, b: T) -> Boolean): List<List<T>> {
    /**
     * Turns list into a list of lists, where each sublist contains element
     * that are considered "equal" based on the predicate function.
     */
    val result = mutableListOf<List<T>>()
    val usedIndices = mutableListOf<Int>()

    forEachIndexed { leftIdx, left ->
        if (!usedIndices.contains(leftIdx)) {
            val list = mutableListOf(left)
            usedIndices.add(leftIdx)
            forEachIndexed { rightIdx, right ->
                if (!usedIndices.contains(rightIdx) && predicate(left, right)) {
                    list.add(right)
                    usedIndices.add(rightIdx)
                }
            }
            result.add(list)
        }
    }
    return result
}

fun <T : Any> Iterable<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }

fun <K, V> Iterable<Map<K, V>>.join(): Map<K, V> =
    mutableMapOf<K, V>().also { map { map -> it.putAll(map) } }.toMap()

fun <T> Collection<T>.padEnd(length: Int, value: T? = null): Collection<T?> =
    if (size < length) plus(List(length - size) { value })
    else this

fun <T> Collection<T>.padStart(length: Int, value: T? = null): Collection<T?> =
    if (size < length) List(length - size) { value }.plus(this)
    else this

fun <T : Any> Collection<T>.prune(maxLength: Int) =
    if (maxLength < size / 2) includeEveryX((size.toFloat() / maxLength).roundToInt())
    else skipEveryX((size.toFloat() / (size - maxLength)).roundToInt())

fun <T : Any> Iterable<T>.skipEveryX(x: Int) = filterIndexed { index, _ -> (index + 1) % x != 0 }

inline fun <T> Iterable<T>.sumOfOrNull(selector: (T) -> Long?): Long? {
    /**
     * Variation of sumOf(), which returns null if there are no elements in the
     * iterable for which `selector` returns non-null.
     */
    var sum: Long? = null
    for (element in this) {
        selector(element)?.also {
            sum = sum?.plus(it) ?: it
        }
    }
    return sum
}

inline fun <T, O> Iterable<T>.zipBy(other: Iterable<O>, predicate: (a: T, b: O) -> Boolean): List<Pair<T, O>> =
    /** Combines two lists based on a per-element comparison function. */
    mapNotNull { item ->
        other.find { predicate(item, it) }?.let { Pair(item, it) }
    }
