@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import kotlin.math.max
import kotlin.math.min
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

@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> = filterValues { it != null } as Map<K, V>

fun <T : Any> Iterable<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }

fun <K, V> Iterable<Map<K, V>>.join(): Map<K, V> =
    mutableMapOf<K, V>().also { map { map -> it.putAll(map) } }.toMap()

fun <T> List<T>.listItemsBetween(item1: T, item2: T): List<T> {
    /** from & to are both exclusive */
    val item1Index = indexOf(item1)
    val item2Index = indexOf(item2)
    val fromIndex = min(item1Index, item2Index)
    val toIndex = max(item1Index, item2Index)

    return when {
        fromIndex == -1 || toIndex == -1 -> emptyList()
        toIndex - fromIndex < 2 -> emptyList()
        else -> subList(fromIndex + 1, toIndex)
    }
}

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

fun <T> List<T>.slice(start: Int, maxCount: Int) =
    if (start >= size) emptyList()
    else subList(start, min(start + maxCount, size))

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

fun <T> Collection<T>.zipPadded(other: Collection<T>, default: T): List<Pair<T, T>> {
    val paddedThis =
        if (size < other.size) this.plus(List(other.size - size) { default })
        else this
    val paddedOther =
        if (size > other.size) other.plus(List(size - other.size) { default })
        else other
    return paddedThis.zip(paddedOther)
}
