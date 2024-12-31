@file:Suppress("unused")

package us.huseli.retaintheme.annotatedtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import us.huseli.retaintheme.Difference

class RetainAnnotatedStringState(
    val mutableString: RetainMutableAnnotatedString,
    val baseFontSize: TextUnit,
    initialSelection: TextRange? = null,
    initialComposition: TextRange? = null,
) {
    private var composition: TextRange? by mutableStateOf(initialComposition)

    val nativeAnnotatedString: AnnotatedString
        get() = mutableString.toNative(baseFontSize)

    var selection: TextRange by mutableStateOf(initialSelection ?: TextRange(mutableString.text.length))
        private set

    val selectionStartStyle: RetainSpanStyle
        get() =
            if (selection.length == 0) mutableString.getFutureCharStyle(selection.start)
            else mutableString.getCharStyle(selection.start)

    val text: String
        get() = mutableString.text

    val textFieldValue: TextFieldValue
        get() = TextFieldValue(text = text, selection = selection, composition = composition)

    fun copy(
        mutableString: RetainMutableAnnotatedString? = null,
        selection: TextRange? = null,
        composition: TextRange? = null,
    ) =
        RetainAnnotatedStringState(
            mutableString = mutableString ?: this.mutableString,
            baseFontSize = baseFontSize,
            initialSelection = selection ?: this.selection,
            initialComposition = composition ?: this.composition,
        )

    fun diff(other: RetainAnnotatedStringState): Difference {
        val stringDiff = mutableString.diff(other.mutableString)
        val selectionDiff = if (selection == other.selection) Difference.None else Difference.Small

        return stringDiff + selectionDiff
    }

    fun onTextFieldValueChange(value: TextFieldValue) {
        if (value.text != mutableString.text) {
            mutableString.applyDiff(value.text)
        }
        if (value.selection != selection) {
            selection = value.selection
        }
        if (value.composition != composition) {
            composition = value.composition
        }
    }

    fun setStyle(value: NullableRetainSpanStyle) {
        mutableString.setStyle(selection.start, selection.end, value)
    }

    fun splitAtSelectionStart(): Pair<RetainMutableAnnotatedString, RetainMutableAnnotatedString> {
        return mutableString.split(selection.start)
    }

    override fun toString(): String = """
    RetainAnnotatedStringState2@${System.identityHashCode(this)}(
        mutableString=$mutableString,
        selection=$selection,
    )        
    """.trimIndent()

    companion object {
        val Saver: Saver<RetainAnnotatedStringState, *> = mapSaver(
            save = { value ->
                mapOf(
                    "serializedValue" to value.mutableString.serialize(),
                    "textFieldValue" to with(TextFieldValue.Saver) { this@mapSaver.save(value.textFieldValue) },
                    "baseFontSize" to value.baseFontSize.value,
                )
            },
            restore = { value ->
                val textFieldValue = value["textFieldValue"]?.let { with(TextFieldValue.Saver) { restore(it) } }

                RetainAnnotatedStringState(
                    mutableString = RetainMutableAnnotatedString.deserialize(value["serializedValue"] as String),
                    baseFontSize = (value["baseFontSize"] as Float).sp,
                    initialSelection = textFieldValue?.selection,
                )
            },
        )
    }
}
