@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.scale
import java.io.File
import kotlin.math.max

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

fun File.toBitmap(): Bitmap? = takeIf { it.isFile }?.inputStream().use { BitmapFactory.decodeStream(it) }
