package us.huseli.retaintheme.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class BodyTextStyles(
    val primary: TextStyle = TextStyle.Default,
    val primaryBold: TextStyle = TextStyle.Default,
    val primarySmall: TextStyle = TextStyle.Default,
    val primarySmallBold: TextStyle = TextStyle.Default,
    val primaryExtraSmall: TextStyle = TextStyle.Default,
    val secondary: TextStyle = TextStyle.Default,
    val secondaryBold: TextStyle = TextStyle.Default,
    val secondarySmall: TextStyle = TextStyle.Default,
    val secondarySmallBold: TextStyle = TextStyle.Default,
    val secondaryExtraSmall: TextStyle = TextStyle.Default,
)

val LocalBodyTextStyles = staticCompositionLocalOf { BodyTextStyles() }
