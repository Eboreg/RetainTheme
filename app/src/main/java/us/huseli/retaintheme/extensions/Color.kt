@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.os.Build
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

fun Color.distance(other: Color): Float {
    val drp2 = (red - other.red).pow(2)
    val dgp2 = (green - other.green).pow(2)
    val dbp2 = (blue - other.blue).pow(2)
    val t = (red + other.red) / 2

    return sqrt(2 * drp2 + 4 * dgp2 + 3 * dbp2 + t * (drp2 - dbp2) / 256)
}

@OptIn(ExperimentalStdlibApi::class)
fun Color.toHexString(withHash: Boolean = false, withAlpha: Boolean = true): String {
    val hexString = (value shr 32).toInt().toHexString()
    return (if (withHash) "#" else "") + if (withAlpha) hexString else hexString.substring(2)
}

fun String.hexCodeToColor(): Color? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
    return try {
        Color(toColorInt())
    } catch (e: Throwable) {
        null
    }
}

val ColorSaver: Saver<Color, *> = listSaver(
    save = { listOf(it.red, it.green, it.blue, it.alpha) },
    restore = { Color(red = it[0], green = it[1], blue = it[2], alpha = it[3]) },
)

fun randomColor() = Color(
    red = Random.nextInt(0, 256),
    green = Random.nextInt(0, 256),
    blue = Random.nextInt(0, 256),
    alpha = 255,
)
