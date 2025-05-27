@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

fun InputStream.copyTo(out: OutputStream): Long {
    var transferred = 0L
    val buffer = ByteArray(8192)
    var length: Int

    while (read(buffer, 0, 8192).also { length = it } >= 0) {
        out.write(buffer, 0, length)
        transferred += length
    }
    return transferred
}

fun InputStream.md5(): String {
    val buffer = ByteArray(8192)
    var length: Int
    val digest = MessageDigest.getInstance("MD5")

    while (read(buffer).also { length = it } > 0) {
        digest.update(buffer, 0, length)
    }
    return String.format("%32s", BigInteger(1, digest.digest()).toString(16).replace(" ", "0"))
}
