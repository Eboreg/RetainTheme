package us.huseli.retaintheme.annotatedtext

import androidx.compose.ui.text.AnnotatedString.Range
import com.github.difflib.DiffUtils
import com.github.difflib.patch.Chunk
import com.github.difflib.patch.DeltaType
import com.github.difflib.patch.Patch

class StringDiff(oldString: String, newString: String) {
    // A word is considered added if new wordlist != old wordlist and target delta contains a non-word char.
    // A word is considered deleted if new wordlist is shorter than old.
    val wordsChanged: Boolean
    val added: List<Range<String>>
    val removed: List<Range<String>>

    init {
        val deltas = (DiffUtils.diffInline(oldString, newString)?.deltas ?: Patch<String>().deltas).filterNotNull()
        val oldWords = oldString.splitWords()
        val oldFinishedWords = oldString.splitFinishedWords()
        val newWords = newString.splitWords()
        val newFinishedWords = newString.splitFinishedWords()
        val whiteSpaceAdded =
            deltas.any { delta -> delta.target?.lines?.any { it.contains(Regex("\\s")) } == true }

        wordsChanged = (whiteSpaceAdded && oldFinishedWords != newFinishedWords) || oldWords.size > newWords.size
        added = deltas
            .filter { it.type == DeltaType.CHANGE || it.type == DeltaType.INSERT }
            .mapNotNull { it.target?.toRange() }
        removed = deltas
            .filter { it.type == DeltaType.CHANGE || it.type == DeltaType.DELETE }
            .mapNotNull { it.source?.toRange() }
    }
}

fun String.splitWords() = split(Regex("\\s+"))

fun String.splitFinishedWords() = splitWords().let { if (matches(Regex("\\w$"))) it.dropLast(1) else it }

fun Chunk<String>.toRange() = Range(
    item = lines.joinToString(),
    start = position,
    end = position + lines.sumOf { it.length },
)
