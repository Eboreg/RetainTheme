@file:Suppress("unused")

package us.huseli.retaintheme.extensions

fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
