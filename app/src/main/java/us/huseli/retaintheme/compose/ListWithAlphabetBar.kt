@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.huseli.retaintheme.extensions.prune

@Composable
inline fun <T> ListWithAlphabetBar(
    modifier: Modifier = Modifier,
    barModifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    characters: Collection<Char>,
    listState: LazyListState,
    minItems: Int = 30,
    barWidth: Dp = 30.dp,
    items: List<T>,
    crossinline selector: (T) -> String,
    crossinline content: @Composable ColumnScope.() -> Unit,
) {
    var maxHeightDp by remember { mutableStateOf(0.dp) }

    BoxWithConstraints(modifier = modifier) {
        maxHeightDp = maxHeight
        Row {
            Column(modifier = Modifier.weight(1f)) {
                content()
            }

            if (characters.isNotEmpty() && items.size >= minItems) {
                val maxCharacters = (maxHeightDp / 30.dp).toInt()
                val displayedCharacters = characters.prune(maxCharacters)
                var selected by remember { mutableStateOf(displayedCharacters.first()) }

                Box(modifier = barModifier.width(barWidth).fillMaxHeight()) {
                    displayedCharacters.forEachIndexed { index, char ->
                        Surface(
                            shape = CircleShape,
                            color = if (char == selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            contentColor = if (char == selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .offset(0.dp, maxHeightDp * (index.toFloat() / displayedCharacters.size))
                                .padding(vertical = 1.dp)
                                .padding(start = 2.dp)
                                .size(min(barWidth, 30.dp) - 2.dp)
                                .clickable(
                                    onClick = {
                                        scope.launch {
                                            if (char == '#') listState.scrollToItem(0)
                                            else {
                                                items.indexOfFirst { selector(it).startsWith(char, true) }
                                                    .takeIf { it > -1 }
                                                    ?.also { pos -> listState.scrollToItem(pos) }
                                            }
                                            selected = char
                                        }
                                    },
                                    indication = rememberRipple(bounded = false, radius = (barWidth / 2) + 5.dp),
                                    interactionSource = remember { MutableInteractionSource() },
                                ),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text(
                                    text = char.toString(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
