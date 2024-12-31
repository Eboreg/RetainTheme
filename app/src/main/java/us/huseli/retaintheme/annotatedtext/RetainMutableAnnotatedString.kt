@file:Suppress("unused")

package us.huseli.retaintheme.annotatedtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.text.AnnotatedString.Range
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import us.huseli.retaintheme.Difference
import java.lang.reflect.Type

class RetainMutableAnnotatedString(achars: List<AnnotatedChar>? = null, startStyle: RetainSpanStyle? = null) :
    CharSequence, IRetainAnnotatedString {
    constructor(string: String) : this(achars = string.map { AnnotatedChar(char = it, style = RetainSpanStyle()) })

    private val achars: SnapshotStateList<AnnotatedChar> = achars?.toMutableStateList() ?: SnapshotStateList()
    internal var startStyle: RetainSpanStyle by mutableStateOf(startStyle ?: RetainSpanStyle())

    private val collapsedSpanStyles: List<Range<RetainSpanStyle>>
        get() {
            return if (this.achars.isEmpty()) emptyList()
            else {
                var idx = 0
                val ranges = mutableListOf<Range<RetainSpanStyle>>()

                while (idx < this.achars.size) {
                    val style = this.achars[idx].style
                    val length = this.achars.drop(idx).takeWhile { it.style == style }.size

                    ranges.add(Range(item = style, start = idx, end = idx + length))
                    idx += length
                }
                this.achars.lastOrNull()?.nextCharStyle?.also {
                    ranges.add(Range(item = it, start = this.achars.size, end = this.achars.size))
                }
                ranges
            }
        }

    override val text: String
        get() = achars.map { it.char }.joinToString("")

    override val length: Int
        get() = achars.size

    fun applyDiff(revised: String): Boolean {
        val diff = StringDiff(text, revised)

        diff.removed.forEach { removeRange(it) }
        diff.added.forEach { insertRange(it) }

        return diff.wordsChanged
    }

    fun copy(achars: List<AnnotatedChar>? = null, startStyle: RetainSpanStyle? = null) = RetainMutableAnnotatedString(
        achars = achars ?: this.achars,
        startStyle = startStyle ?: this.startStyle,
    )

    fun difference(other: RetainMutableAnnotatedString): Difference {
        if (achars.size != other.achars.size) return Difference.Significant

        val acharsDiff = achars.zip(other.achars).fold(Difference.None) { acc, (a1, a2) -> acc + a1.difference(a2) }
        val startStyleDiff = if (startStyle == other.startStyle) Difference.None else Difference.Small

        return acharsDiff + startStyleDiff
    }

    override fun get(index: Int): Char = achars[index].char

    fun getCharStyle(position: Int): RetainSpanStyle {
        if (position == 0 && achars.isEmpty()) return startStyle
        return achars.getOrNull(position)?.style ?: RetainSpanStyle()
    }

    fun getFutureCharStyle(position: Int): RetainSpanStyle {
        if (position == 0) return startStyle
        return achars.getOrNull(position - 1)?.let { it.nextCharStyle ?: it.style } ?: RetainSpanStyle()
    }

    fun serialize(): String = gson.toJson(this)

    fun setStyle(start: Int, end: Int, value: IRetainSpanStyle) {
        if (start == end) {
            if (start == 0) startStyle = startStyle.merge(value)
            else achars.getOrNull(start - 1)?.also { achar ->
                achar.nextCharStyle = (achar.nextCharStyle ?: achar.style).merge(value)
            }
        } else {
            for (idx in start until end) {
                achars.getOrNull(idx)?.also { achar ->
                    achar.style = achar.style.merge(value)
                }
            }
        }
    }

    fun split(position: Int): Pair<RetainMutableAnnotatedString, RetainMutableAnnotatedString> =
        Pair(subSequence(0, position), subSequence(position, text.length))

    override fun subSequence(startIndex: Int, endIndex: Int): RetainMutableAnnotatedString {
        return RetainMutableAnnotatedString(
            achars.subList(startIndex, endIndex).apply { lastOrNull()?.nextCharStyle = null }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun toImmutable(): RetainAnnotatedString = RetainAnnotatedString(
        text = text,
        spanStyles = collapsedSpanStyles as List<Range<RetainSpanStyle?>>,
    )

    override fun toMutable(): RetainMutableAnnotatedString = this

    override fun toString(): String = """
    RetainMutableAnnotatedString@${System.identityHashCode(this)}(
        startStyle=$startStyle,
        text=$text,
        achars=$achars,
    )        
    """.trimIndent()

    private fun insertRange(range: Range<String>) {
        val style = getFutureCharStyle(range.start)
        val newAchars = range.item.map { AnnotatedChar(it, style) }

        achars.addAll(range.start, newAchars)
    }

    private fun removeRange(range: Range<String>) {
        achars.removeRange(range.start, range.end)
        if (range.start == 0) startStyle = RetainSpanStyle()
    }

    class TypeAdapter : JsonSerializer<RetainMutableAnnotatedString>, JsonDeserializer<RetainMutableAnnotatedString> {
        override fun serialize(
            src: RetainMutableAnnotatedString?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?,
        ): JsonElement? {
            val acharAdapter = AnnotatedChar.TypeAdapter()
            val styleAdapter = RetainSpanStyle.TypeAdapter()

            return JsonObject().apply {
                add("achars", JsonArray().apply {
                    src?.achars?.forEach { achar ->
                        add(acharAdapter.serialize(achar, AnnotatedChar::class.java, context))
                    }
                })
                add("startStyle", styleAdapter.serialize(src?.startStyle, RetainSpanStyle::class.java, context))
            }
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?,
        ): RetainMutableAnnotatedString? {
            val acharAdapter = AnnotatedChar.TypeAdapter()
            val styleAdapter = RetainSpanStyle.TypeAdapter()

            return json?.asJsonObject?.let { obj ->
                RetainMutableAnnotatedString(
                    achars = obj.getAsJsonArray("achars").map { element ->
                        acharAdapter.deserialize(element, AnnotatedChar::class.java, context) as AnnotatedChar
                    },
                    startStyle = styleAdapter.deserialize(
                        obj.getAsJsonObject("startStyle"),
                        RetainSpanStyle::class.java,
                        context,
                    ),
                )
            }
        }
    }

    companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(RetainSpanStyle::class.java, RetainSpanStyle.TypeAdapter())
            .registerTypeAdapter(AnnotatedChar::class.java, AnnotatedChar.TypeAdapter())
            .registerTypeAdapter(RetainMutableAnnotatedString::class.java, TypeAdapter())
            .create()

        fun deserialize(source: String): RetainMutableAnnotatedString {
            return deserializeOrNull(source)
                ?: RetainAnnotatedString.deserializeOrNull(source)?.toMutable()
                ?: RetainMutableAnnotatedString(source)
        }

        fun deserializeOrNull(source: String): RetainMutableAnnotatedString? {
            return try {
                gson.fromJson(source, RetainMutableAnnotatedString::class.java)
            } catch (_: Throwable) {
                null
            }
        }
    }
}
