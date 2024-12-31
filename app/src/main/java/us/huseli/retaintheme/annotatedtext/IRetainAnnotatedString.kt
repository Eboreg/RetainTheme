package us.huseli.retaintheme.annotatedtext

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import us.huseli.retaintheme.Difference
import us.huseli.retaintheme.extensions.diff

interface IRetainAnnotatedString : CharSequence {
    val text: String

    fun toImmutable(): RetainAnnotatedString
    fun toMutable(): RetainMutableAnnotatedString

    fun diff(other: IRetainAnnotatedString): Difference {
        val immutableThis = toImmutable()
        val immutableOther = other.toImmutable()
        val styleDiff = when {
            immutableThis.nonEmptySpanStyles != immutableOther.nonEmptySpanStyles -> Difference.Significant
            immutableThis.spanStyles != immutableOther.spanStyles -> Difference.Small
            else -> Difference.None
        }
        val textDiff = text.diff(other.text)

        return textDiff + styleDiff
    }

    fun toNative(baseFontSize: TextUnit): AnnotatedString = AnnotatedString(
        text = text,
        spanStyles = toImmutable().spanStyles.toNative(baseFontSize),
    )
}
