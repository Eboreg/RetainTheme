@file:Suppress("unused")

package us.huseli.retaintheme.annotatedtext

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString.Range
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import us.huseli.retaintheme.extensions.limit

@Immutable
class RetainAnnotatedString(
    override val text: String = "",
    spanStyles: List<Range<RetainSpanStyle?>>? = null,
) : CharSequence, IRetainAnnotatedString {
    override val length: Int = text.length

    val nonEmptySpanStyles: List<RetainSpanStyle>
        get() = this.spanStyles.filter { it.start != it.end }.map { it.item }

    @Suppress("UNCHECKED_CAST")
    val spanStyles: List<Range<RetainSpanStyle>> =
        spanStyles?.filter { it.item != null }?.map { it as Range<RetainSpanStyle> }?.limit(text.length) ?: emptyList()

    override fun get(index: Int): Char = text[index]

    fun serialize(): String = gson.toJson(this)

    override fun subSequence(startIndex: Int, endIndex: Int): RetainAnnotatedString {
        if (startIndex == 0 && endIndex == text.length) return this

        return RetainAnnotatedString(
            text = text.substring(startIndex, endIndex),
            spanStyles = spanStyles.filter { maxOf(startIndex, it.start) <= minOf(endIndex, it.end) }.map {
                Range(
                    item = it.item,
                    start = maxOf(startIndex, it.start) - startIndex,
                    end = minOf(endIndex, it.end) - startIndex,
                )
            },
        )
    }

    override fun toImmutable(): RetainAnnotatedString = this

    override fun toMutable(): RetainMutableAnnotatedString = RetainMutableAnnotatedString(
        text.mapIndexed { idx, char ->
            AnnotatedChar(
                char = char,
                style = spanStyles
                    .filter { it.start <= idx && it.end > idx }
                    .map { it.item }
                    .nonNullableMerge(),
            )
        }
    )

    override fun toString(): String = """
    RetainAnnotatedString@${System.identityHashCode(this)}(
        text=${text},
        spanStyles=$spanStyles,
    )
    """.trimIndent()

    companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(RetainSpanStyle::class.java, RetainSpanStyle.TypeAdapter())
            .create()

        fun deserialize(source: String): RetainAnnotatedString = deserializeOrNull(source)
            ?: RetainMutableAnnotatedString.deserializeOrNull(source)?.toImmutable()
            ?: RetainAnnotatedString(source)

        fun deserializeOrNull(source: String): RetainAnnotatedString? {
            return try {
                gson.fromJson(source, RetainAnnotatedString::class.java)
            } catch (_: Throwable) {
                null
            }
        }
    }
}
