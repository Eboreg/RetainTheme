@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.util.Locale
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.N)
fun Long.bytesToString(): String {
    /** "Human readable" file sizes etc. */
    if (this < 2.0.pow(10)) return "$this B"
    if (this < 2.0.pow(20)) return "${this.div(2.0.pow(10)).formattedString(1)} KB"
    if (this < 2.0.pow(30)) return "${this.div(2.0.pow(20)).formattedString(1)} MB"
    if (this < 2.0.pow(40)) return "${this.div(2.0.pow(30)).formattedString(1)} GB"
    return "${this.div(2.0.pow(40)).formattedString(1)} TB"
}

fun Double.formatDuration() = seconds.sensibleFormat()

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

fun Int.pow(n: Int): Int = toDouble().pow(n).toInt()

fun Double.roundUp() = toInt() + (if (this % 1 > 0) 1 else 0)

fun Int.roundUpSqrt() = sqrt().roundUp()

fun Int.sqrt() = kotlin.math.sqrt(toDouble())

@RequiresApi(Build.VERSION_CODES.O)
fun Long.toInstant(): Instant = Instant.ofEpochSecond(this)
