package us.huseli.retaintheme.annotatedtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import us.huseli.retaintheme.Difference
import java.lang.reflect.Type

class AnnotatedChar(val char: Char, style: RetainSpanStyle, nextCharStyle: RetainSpanStyle? = null) {
    var style: RetainSpanStyle by mutableStateOf(style)
    var nextCharStyle: RetainSpanStyle? by mutableStateOf(nextCharStyle)

    fun difference(other: AnnotatedChar): Difference {
        return when {
            other.char != char || other.style != style -> Difference.Significant
            other.nextCharStyle != nextCharStyle -> Difference.Small
            else -> Difference.None
        }
    }

    override fun toString(): String = """
    AnnotatedChar@${System.identityHashCode(this)}(char=$char, style=$style, nextCharStyle=$nextCharStyle)
    """.trimIndent()

    class TypeAdapter : JsonSerializer<AnnotatedChar>, JsonDeserializer<AnnotatedChar> {
        override fun serialize(
            src: AnnotatedChar?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement? {
            val styleAdapter = RetainSpanStyle.TypeAdapter()

            return JsonObject().apply {
                src?.also { achar ->
                    addProperty("char", achar.char)
                    add("style", styleAdapter.serialize(achar.style, RetainSpanStyle::class.java, context))
                    add(
                        "nextCharStyle",
                        styleAdapter.serialize(achar.nextCharStyle, RetainSpanStyle::class.java, context),
                    )
                }
            }
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?,
        ): AnnotatedChar? {
            val styleAdapter = RetainSpanStyle.TypeAdapter()

            return json?.asJsonObject?.let { obj ->
                AnnotatedChar(
                    char = obj.get("char").asString[0],
                    style = styleAdapter.deserialize(
                        obj.getAsJsonObject("style"),
                        RetainSpanStyle::class.java,
                        context,
                    )!!,
                    nextCharStyle = obj.getAsJsonObject("nextCharStyle")?.let {
                        styleAdapter.deserialize(it, RetainSpanStyle::class.java, context)
                    },
                )
            }
        }
    }
}
