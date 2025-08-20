@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import us.huseli.retaintheme.Difference
import java.security.MessageDigest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun CharSequence.capitalized() = replace(Regex("((^\\p{L})|(?<=\\P{L})(\\p{L}))")) { it.value.uppercase() }

fun CharSequence.containsAny(terms: Iterable<CharSequence>): Boolean = terms.any { contains(it) }

fun String.diff(other: String?): Difference {
    val thisFinishedWords = finishedWords()
    val otherFinishedWords = other?.finishedWords() ?: emptyList()

    return when {
        this == other -> Difference.None
        other == null -> Difference.Significant
        thisFinishedWords != otherFinishedWords -> Difference.Significant
        else -> Difference.Small
    }
}

fun String.finishedWords() = replace(Regex("^\\s*"), "")
    .split(Regex("\\s+"))
    .let { if (matches(Regex(".*\\w$"))) it.dropLast(1) else it }
    .filter { it.isNotEmpty() }

fun Iterable<CharSequence>.leadingChars(): List<Char> =
    /**
     * Alphabetical list of all leading characters in the list, converted to
     * uppercase, and with non-alphabet characters lumped together as "#".
     */
    mapNotNull { string ->
        string.replace(Regex("[^\\w&&[^0-9]]"), "#").getOrNull(0)?.uppercaseChar()
    }.distinct().sorted()

fun String.md5(): ByteArray = MessageDigest.getInstance("MD5").digest(toByteArray(Charsets.UTF_8))

fun CharSequence.nullIfBlank(): String? = takeIf { it.isNotBlank() }?.toString()

fun CharSequence.nullIfEmpty(): String? = takeIf { it.isNotEmpty() }?.toString()

fun CharSequence.parseUrlQuery(): Map<String, String> {
    return split('&')
        .mapNotNull { param -> param.split('=', limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] } }
        .toMap()
}

fun CharSequence.sanitizeFilename(): String =
    /** Replaces potentially invalid characters with "-". */
    replace(Regex("[/\\\\?%*:|\"<>\\x7F\\x00-\\x1F]"), "-")

fun Collection<CharSequence>.stripCommonFixes(): Collection<CharSequence> {
    /** Strip prefixes and suffixes that are shared among all the strings. */
    if (size < 2) return this

    val firstString = first()
    val firstReversed = first().reversed()
    var prefix = ""
    var suffix = ""

    for (charPos in firstString.indices) {
        if (all { it.getOrNull(charPos) == firstString[charPos] }) prefix += firstString[charPos]
        else break
    }
    for (charPos in firstReversed.indices) {
        if (all { it.reversed().getOrNull(charPos) == firstReversed[charPos] }) suffix = firstReversed[charPos] + suffix
        else break
    }
    return map { it.removePrefix(prefix).removeSuffix(suffix) }
}

fun CharSequence.substringMax(startIndex: Int, endIndex: Int) =
    /**
     * String.substring() variation that will stop at the end of the string if it's
     * too short, instead of throwing an exception.
     */
    substring(startIndex, kotlin.math.min(endIndex, length))

fun CharSequence.takeIfNotBlank() = takeIf { it.isNotBlank() }

fun CharSequence.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

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

    groups?.get(1)?.value?.toInt()?.hours?.let { duration += it }
    groups?.get(2)?.value?.toInt()?.minutes?.let { duration += it }
    groups?.get(3)?.value?.toInt()?.seconds?.let { duration += it }

    return duration
}
