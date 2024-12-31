package us.huseli.retaintheme.annotatedtext

import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.Expose
import us.huseli.retaintheme.annotatedtext.RetainSpanStyle.Size
import java.lang.reflect.Type

interface IRetainSpanStyle {
    val size: Size?
    val isBold: Boolean?
    val isItalic: Boolean?
    val isUnderlined: Boolean?
}

fun Collection<IRetainSpanStyle>.nonNullableMerge(): RetainSpanStyle {
    val merged = fold(NullableRetainSpanStyle()) { acc, style -> acc.merge(style) }

    return RetainSpanStyle(
        size = merged.size ?: Size.Normal,
        isBold = merged.isBold == true,
        isUnderlined = merged.isUnderlined == true,
        isItalic = merged.isItalic == true,
    )
}

data class NullableRetainSpanStyle(
    override var size: Size? = null,
    override val isBold: Boolean? = null,
    override val isItalic: Boolean? = null,
    override val isUnderlined: Boolean? = null,
) : IRetainSpanStyle {
    fun merge(other: IRetainSpanStyle) = copy(
        size = other.size ?: size,
        isBold = other.isBold ?: isBold,
        isUnderlined = other.isUnderlined ?: isUnderlined,
        isItalic = other.isItalic ?: isItalic,
    )
}

data class RetainSpanStyle(
    @Expose override val size: Size,
    @Expose override val isBold: Boolean,
    @Expose override val isItalic: Boolean,
    @Expose override val isUnderlined: Boolean,
) : IRetainSpanStyle {
    enum class Size { Small, Normal, Large }

    constructor() : this(size = Size.Normal, isBold = false, isItalic = false, isUnderlined = false)

    val fontStyle: FontStyle?
        get() = if (isItalic) FontStyle.Italic else null

    val fontWeight: FontWeight?
        get() = if (isBold) FontWeight.Bold else null

    val textDecoration: TextDecoration?
        get() = if (isUnderlined) TextDecoration.Underline else null

    override fun toString(): String {
        val result = mutableListOf<String>(size.name)

        if (isBold) result.add("bold")
        if (isItalic) result.add("italic")
        if (isUnderlined) result.add("underlined")

        return result.joinToString(", ")
    }

    fun merge(other: IRetainSpanStyle) = copy(
        size = other.size ?: size,
        isBold = other.isBold ?: isBold,
        isUnderlined = other.isUnderlined ?: isUnderlined,
        isItalic = other.isItalic ?: isItalic,
    )

    fun toNative(baseFontSize: TextUnit) = SpanStyle(
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textDecoration = textDecoration,
        fontSize = when (size) {
            Size.Small -> baseFontSize * 0.75
            Size.Normal -> baseFontSize
            Size.Large -> baseFontSize * 1.25
        },
    )

    class TypeAdapter : JsonSerializer<RetainSpanStyle>, JsonDeserializer<RetainSpanStyle> {
        override fun serialize(
            src: RetainSpanStyle?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?,
        ): JsonElement? {
            return JsonObject().apply {
                src?.also { style ->
                    if (style.size != Size.Normal) addProperty("size", style.size.name)
                    if (style.isBold) addProperty("isBold", true)
                    if (style.isUnderlined) addProperty("isUnderlined", true)
                    if (style.isItalic) addProperty("isItalic", true)
                }
            }
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?,
        ): RetainSpanStyle? {
            return json?.asJsonObject?.let { obj ->
                RetainSpanStyle(
                    size = obj.get("size")?.asString?.let { Size.valueOf(it) } ?: Size.Normal,
                    isBold = obj.get("isBold")?.asBoolean == true,
                    isItalic = obj.get("isItalic")?.asBoolean == true,
                    isUnderlined = obj.get("isUnderlined")?.asBoolean == true,
                )
            }
        }
    }
}

fun Range<RetainSpanStyle>.toNative(baseFontSize: TextUnit) =
    Range(item = item.toNative(baseFontSize), start = start, end = end)

fun Collection<Range<RetainSpanStyle>>.toNative(baseFontSize: TextUnit) = map { it.toNative(baseFontSize) }
