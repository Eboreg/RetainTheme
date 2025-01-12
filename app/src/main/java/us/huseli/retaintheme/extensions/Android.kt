@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

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

fun File.toBitmap(): Bitmap? = takeIf { it.isFile }?.inputStream().use { BitmapFactory.decodeStream(it) }

fun Modifier.clickableIfNotNull(onClick: (() -> Unit)?) = onClick?.let { clickable(onClick = it) } ?: this

fun <T> Modifier.clickableIfNotNull(onClick: ((T) -> Unit)?, arg: T) =
    onClick?.let { clickable { onClick(arg) } } ?: this

fun ViewModel.launchOnIOThread(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(Dispatchers.IO, block = block)

fun ViewModel.launchOnMainThread(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(Dispatchers.Main, block = block)
