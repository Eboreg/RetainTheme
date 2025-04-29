@file:Suppress("unused")

package us.huseli.retaintheme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun RetainTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    typography: Typography = Typography,
    shapes: Shapes = MaterialTheme.shapes,
    darkColors: ColorScheme = DarkColors,
    lightColors: ColorScheme = LightColors,
    content: @Composable () -> Unit,
) {
    val colorScheme = getColorScheme(
        lightColors = lightColors,
        darkColors = darkColors,
        useDarkTheme = useDarkTheme,
        dynamicColor = dynamicColor,
    )
    val basicColors = remember { if (useDarkTheme) RetainBasicColorsDark else RetainBasicColorsLight }
    val basicColorsInverted = remember { if (useDarkTheme) RetainBasicColorsLight else RetainBasicColorsDark }

    CompositionLocalProvider(
        LocalBasicColors provides basicColors,
        LocalBasicColorsInverted provides basicColorsInverted,
    ) {
        MaterialTheme(
            shapes = shapes,
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
