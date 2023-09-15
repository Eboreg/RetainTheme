package us.huseli.retaintheme

import kotlin.math.roundToInt

fun <T : Any> Collection<T>.skipEveryX(x: Int) = filterIndexed { index, _ -> (index + 1) % x != 0 }

fun <T : Any> Collection<T>.includeEveryX(x: Int) = filterIndexed { index, _ -> index % x == 0 }

fun <T : Any> Collection<T>.prune(maxLength: Int) =
    if (maxLength < size / 2) includeEveryX((size.toFloat() / maxLength).roundToInt())
    else skipEveryX((size.toFloat() / (size - maxLength)).roundToInt())
