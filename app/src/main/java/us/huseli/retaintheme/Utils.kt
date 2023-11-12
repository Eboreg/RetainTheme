@file:Suppress("unused")

package us.huseli.retaintheme

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Build
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.scale
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow
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


/** Combines two lists based on a per-element comparison function. */
inline fun <T, O> List<T>.zipBy(other: List<O>, predicate: (a: T, b: O) -> Boolean): List<Pair<T, O>> =
    mapNotNull { item ->
        other.find { predicate(item, it) }?.let { Pair(item, it) }
    }


inline fun <T> List<T>.combineEquals(predicate: (a: T, b: T) -> Boolean): List<List<T>> {
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


fun String.toDuration(): Duration {
    /**
     * Parses a time string that can either be in ISO-8601 or "HH:mm:ss"
     * format.
     */
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


/** Replaces potentially invalid characters with "-". */
fun String.sanitizeFilename(): String =
    replace(Regex("[/\\\\?%*:|\"<>\\x7F\\x00-\\x1F]"), "-")


/**
 * Alphabetical list of all leading characters in the list, converted to
 * uppercase, and with non-alphabet characters lumped together as "#".
 */
fun List<String>.leadingChars(): List<Char> =
    mapNotNull { string ->
        string.replace(Regex("[^\\w&&[^0-9]]"), "#").getOrNull(0)?.uppercaseChar()
    }.distinct().sorted()


@RequiresApi(Build.VERSION_CODES.O)
fun Instant.isoDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    return formatter.format(this)
}


fun File.toBitmap(): Bitmap? = takeIf { it.isFile }?.inputStream().use { BitmapFactory.decodeStream(it) }


fun Int.sqrt() = kotlin.math.sqrt(toDouble())


fun Double.roundUp() = toInt() + (if (this % 1 > 0) 1 else 0)


fun Int.roundUpSqrt() = sqrt().roundUp()


fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
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


/**
 * String.substring() variation that will stop at the end of the string if it's
 * too short, instead of throwing an exception.
 */
fun String.substringMax(startIndex: Int, endIndex: Int) = substring(startIndex, kotlin.math.min(endIndex, length))


fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()


fun Bitmap.scaleToMaxSize(maxSizePx: Int): Bitmap =
    if (width > maxSizePx || height > maxSizePx) {
        val scaleBy = maxSizePx.toDouble() / max(width, height)
        scale((width * scaleBy).toInt(), (height * scaleBy).toInt())
    } else this


fun Bitmap.scaleToMaxSize(maxSizeDp: Dp, context: Context): Bitmap =
    scaleToMaxSize(context.dpToPx(maxSizeDp.value.toInt()))


@RequiresApi(Build.VERSION_CODES.N)
fun Double.formattedString(maxDecimals: Int, locale: Locale = Locale.getDefault()): String {
    /**
     * Formats a double with max `maxDecimals` decimals. As long as the least
     * significant decimal positions are 0, they will be removed from the
     * output. So `23.4567.formattedString(3)` outputs "23.457", but
     * `23.0.formattedString(3)` outputs "23".
     */
    val pattern =
        if (maxDecimals > 0) "0." + "0".repeat(maxDecimals)
        else "0"
    val symbols = DecimalFormatSymbols(locale)
    val decimalFormat = DecimalFormat(pattern, symbols)
    decimalFormat.isDecimalSeparatorAlwaysShown = false
    return decimalFormat.format(this).trimEnd('0').trimEnd(symbols.decimalSeparator)
}


@RequiresApi(Build.VERSION_CODES.N)
fun Long.bytesToString(): String {
    /** "Human readable" file sizes etc. */
    if (this < 2.0.pow(10)) return "$this B"
    if (this < 2.0.pow(20)) return "${this.div(2.0.pow(10)).formattedString(1)} KB"
    if (this < 2.0.pow(30)) return "${this.div(2.0.pow(20)).formattedString(1)} MB"
    if (this < 2.0.pow(40)) return "${this.div(2.0.pow(30)).formattedString(1)} GB"
    return "${this.div(2.0.pow(40)).formattedString(1)} TB"
}


fun List<Duration>.sum(): Duration = this.plus(Duration.ZERO).reduce { d1, d2 -> d1 + d2 }
