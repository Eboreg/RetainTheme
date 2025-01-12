@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import androidx.compose.ui.text.AnnotatedString
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

fun Collection<String>.cleanDuplicates(ignoreCase: Boolean = true): Collection<String> =
    associateBy { if (ignoreCase) it.lowercase() else it }.values

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

inline fun <S, T : S, K> Iterable<T>.distinctWith(operation: (acc: S, T) -> S, selector: (T) -> K): List<S> {
    /** Produces distinct values selected by `selector` while also running a "reduce" operation. */
    val result = mutableMapOf<K, S>()
    forEach { item ->
        val key = selector(item)
        result[key] = result[key]?.let { operation(it, item) } ?: item
    }
    return result.values.toList()
}

fun <T : Any> Iterable<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }

fun <K, V> Iterable<Map<K, V>>.join(): Map<K, V> =
    mutableMapOf<K, V>().also { map { map -> it.putAll(map) } }.toMap()

fun <T> List<AnnotatedString.Range<T>>.limit(length: Int): List<AnnotatedString.Range<T>> =
    filter { it.start < length }.map { range ->
        if (range.end <= length) range
        else range.copy(end = length)
    }

fun <T> List<T>.listItemsBetween(item1: T, item2: T, key: (T) -> Any?): List<T> {
    /** from & to are both exclusive */
    val keyList = map { key(it) }
    val item1Index = keyList.indexOf(key(item1))
    val item2Index = keyList.indexOf(key(item2))
    val fromIndex = min(item1Index, item2Index)
    val toIndex = max(item1Index, item2Index)

    return when {
        fromIndex == -1 || toIndex == -1 -> emptyList()
        toIndex - fromIndex < 2 -> emptyList()
        else -> subList(fromIndex + 1, toIndex)
    }
}

fun <T> List<T>.listItemsBetween(item1: T, item2: T): List<T> =
    listItemsBetween(item1 = item1, item2 = item2, key = { it })

fun <T> Collection<T>.mostCommonValue(): T? {
    if (isEmpty()) return null

    val counts = mutableMapOf<T, Int>()

    forEach { value ->
        counts[value] = (counts[value] ?: 0) + 1
    }
    return counts.maxBy { it.value }.key
}

fun <T> List<T>.nextOrFirst(current: T): T {
    val currentIdx = indexOf(current)

    if (isEmpty()) throw Exception("nextOrFirst() needs at least 1 element")
    if (currentIdx == -1 || currentIdx == lastIndex) return this[0]
    return this[currentIdx + 1]
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

fun <T> List<T>.pruneOrPad(length: Int, default: T? = null, average: ((List<T>) -> T)? = null): List<T> {
    /**
     * Coerces the list to size 'length' by removing or inserting elements at even intervals as needed.
     * If 'average' is supplied, it will be used for calculating elements to insert. Otherwise, the previous element
     * will just be duplicated.
     *
     * @param default If supplied and the list is empty, a "length" size list filled with this value is returned.
     * Otherwise, an empty list will just return an empty list.
     */
    if (size == length) return this
    if (isEmpty()) {
        return if (default != null) List(length) { default }
        else this
    }

    val indexWindows = if (length > size) {
        val windowSize = (length.toDouble() / size).toInt()
        val indexCount = length + windowSize - 1
        val takeEvery = size.toDouble() / indexCount
        List(indexCount) { (it * takeEvery).toInt() }.windowed(windowSize)
    } else {
        val takeEvery = size.toDouble() / length
        val startIndices = List(length) { (it * takeEvery).toInt() }
        startIndices.mapIndexed { i, startIdx ->
            val windowSize = if (i < startIndices.lastIndex) startIndices[i + 1] - startIdx else size - startIdx
            List(windowSize) { it + startIdx }
        }
    }

    return indexWindows.map { window -> average?.invoke(window.map { get(it) }) ?: get(window.average().roundToInt()) }
}

fun List<Int>.pruneOrPad(length: Int, default: Int = 0) =
    pruneOrPad(length, default) { sublist -> sublist.average().roundToInt() }

fun <T> List<T>.replace(index: Int, other: Collection<T>): List<T> {
    if (index > size) throw Exception("Index ($index) is larger than size of list ($size)")

    return take(index).plus(other).let {
        if (index + other.size < size) it.plus(subList(index + other.size, size))
        else it
    }
}

fun <T> List<T>.replaceNullPadding(index: Int, other: Collection<T>): List<T?> {
    /**
     * Examples:
     * listOf(0, 1, 2).replaceNullPadding(4, listOf(4, 5, 6)) = [0, 1, 2, null, 4, 5, 6]
     * listOf(0, 0, 0, 0, 4).replaceNullPadding(1, listOf(1, 2, 3)) = [0, 1, 2, 3, 4]
     */
    val result: MutableList<T?> = take(index).toMutableList()
    if (index > result.size) result.addAll(List(index - result.size) { null })
    result.addAll(other)
    if (index + other.size < size) result.addAll(subList(index + other.size, size))
    return result.toList()
}

fun <T : Any> Iterable<T>.skipEveryX(x: Int) = filterIndexed { index, _ -> (index + 1) % x != 0 }

fun <T> List<T>.slice(start: Int, maxCount: Int) =
    if (start >= size || maxCount <= 0) emptyList()
    else subList(start, min(start + maxCount, size))

fun <T, K> Iterable<T>.sortedLike(other: Iterable<T>, key: (T) -> K): List<T> =
    sortedBy { item -> other.indexOfFirst { key(it) == key(item) } }

fun <T> Iterable<T>.sortedLike(other: Iterable<T>) = sortedLike(other, key = { it })

fun Collection<Int>.splitIntervals(descending: Boolean = false): List<Pair<Int, Int>> {
    /**
     * Splits collection into list of contiguous intervals and returns pairs of (<start value>, <end value>).
     *
     * Example:
     * listOf(2, 4, 3, 1, 6, 9, 7, 8, 12, 14, 15, 19).splitIntervals() ==
     *      [(1, 4), (6, 9), (12, 12), (14, 15), (19, 19)]
     */
    val intervals = mutableListOf<Pair<Int, Int>>()
    var currentStartValue: Int = minOrNull() ?: return emptyList()
    var lastValue: Int? = null

    for ((idx, value) in sorted().withIndex()) {
        if (lastValue != null && value != lastValue + 1) {
            intervals.add(Pair(currentStartValue, lastValue))
            currentStartValue = value
        }
        if (idx == size - 1) {
            intervals.add(Pair(currentStartValue, value))
        }
        lastValue = value
    }

    if (descending) return intervals.toList().sortedByDescending { it.first }
    return intervals.toList()
}

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

fun <T> Set<T>.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

fun <T> List<T>.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

fun <T> Collection<T>.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

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
