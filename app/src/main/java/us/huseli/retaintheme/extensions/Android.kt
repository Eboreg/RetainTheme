@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.scale
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun Color.distance(other: Color): Float {
    val drp2 = (red - other.red).pow(2)
    val dgp2 = (green - other.green).pow(2)
    val dbp2 = (blue - other.blue).pow(2)
    val t = (red + other.red) / 2

    return sqrt(2 * drp2 + 4 * dgp2 + 3 * dbp2 + t * (drp2 - dbp2) / 256)
}

fun Context.dpToPx(dp: Number): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

fun Context.getActivity(): ComponentActivity? {
    var currentContext = this

    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

fun Bitmap.scaleToMaxSize(maxSizePx: Int): Bitmap =
    if (width > maxSizePx || height > maxSizePx) {
        val scaleBy = maxSizePx.toDouble() / max(width, height)
        scale((width * scaleBy).toInt(), (height * scaleBy).toInt())
    } else this

fun Bitmap.scaleToMaxSize(maxSizeDp: Dp, context: Context): Bitmap =
    scaleToMaxSize(context.dpToPx(maxSizeDp.value.toInt()))

fun Bitmap.square(): Bitmap {
    val length = min(width, height)

    return if (width == height) this
    else Bitmap.createBitmap(this, (width - length) / 2, (height - length) / 2, length, length)
}

fun File.toBitmap(): Bitmap? = takeIf { it.isFile }?.inputStream().use { BitmapFactory.decodeStream(it) }
