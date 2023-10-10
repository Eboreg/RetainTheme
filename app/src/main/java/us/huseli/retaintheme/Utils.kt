@file:Suppress("unused")

package us.huseli.retaintheme

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun <T : Any> Collection<T>.skipEveryX(x: Int) = filterIndexed { index, _ -> (index + 1) % x != 0 }


fun <T : Any> Collection<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }


fun <T : Any> Collection<T>.prune(maxLength: Int) =
    if (maxLength < size / 2) includeEveryX((size.toFloat() / maxLength).roundToInt())
    else skipEveryX((size.toFloat() / (size - maxLength)).roundToInt())


fun Duration.sensibleFormat() = toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}


fun Double.formatDuration() = seconds.sensibleFormat()


@Suppress("BooleanMethodIsAlwaysInverted")
@Composable
fun isInLandscapeMode() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE


/**
 * Will always return a list of `length` elements. If the list is shorter than `offset + length`, elements will be
 * added by starting over from the beginning again.
 */
fun <T> List<T>.circular(offset: Int, length: Int): List<T> {
    val realOffset = offset % size
    if (realOffset + length > size)
        return subList(realOffset, size) + circular(0, length - size + realOffset)
    return subList(realOffset, realOffset + length)
}


/**
 * Combines two lists based on a per-element comparison function.
 */
inline fun <T, O> List<T>.zipBy(other: List<O>, predicate: (a: T, b: O) -> Boolean): List<Pair<T, O>> =
    mapNotNull { item ->
        other.find { predicate(item, it) }?.let { Pair(item, it) }
    }


/**
 * Turns list into a list of lists, where each sublist contains element that are considered "equal" based on the
 * predicate function.
 */
inline fun <T> List<T>.combineEquals(predicate: (a: T, b: T) -> Boolean): List<List<T>> {
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


/**
 * Parses a time string that can either be in ISO-8601 or "HH:mm:ss" format.
 */
fun String.toDuration(): Duration {
    try {
        return Duration.parseIsoString(this)
    } catch (_: IllegalArgumentException) {
    }

    val regex = Regex("(?:(?<hours>\\d+):)?(?<minutes>\\d+):(?<seconds>\\d+)$")
    val groups = regex.find(this)?.groups
    var duration = Duration.ZERO

    groups?.get("hours")?.value?.toInt()?.hours?.let { duration += it }
    groups?.get("minutes")?.value?.toInt()?.minutes?.let { duration += it }
    groups?.get("seconds")?.value?.toInt()?.seconds?.let { duration += it }
    return duration
}


/**
 * Replaces potentially invalid characters with "-".
 */
fun String.sanitizeFilename(): String =
    replace(Regex("[/\\\\?%*:|\"<>\\x7F\\x00-\\x1F]"), "-")


fun List<String>.leadingChars(): List<Char> =
    mapNotNull { string ->
        string.replace(Regex("[^\\w&&[^0-9]]"), "#").getOrNull(0)?.uppercaseChar()
    }.distinct().sorted()


@RequiresApi(Build.VERSION_CODES.O)
fun Instant.isoDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    return formatter.format(this)
}
