package us.huseli.retaintheme

import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

fun <T : Any> Collection<T>.skipEveryX(x: Int) = filterIndexed { index, _ -> (index + 1) % x != 0 }

fun <T : Any> Collection<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }

fun <T : Any> Collection<T>.prune(maxLength: Int) =
    if (maxLength < size / 2) includeEveryX((size.toFloat() / maxLength).roundToInt())
    else skipEveryX((size.toFloat() / (size - maxLength)).roundToInt())

fun Double.formatDuration() = seconds.toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}
