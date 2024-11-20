@file:Suppress("unused")

package us.huseli.retaintheme.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun RetainTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    typography: Typography = Typography,
    shapes: Shapes = MaterialTheme.shapes,
    darkColors: ColorScheme = DarkColors,
    lightColors: ColorScheme = LightColors,
    statusBarColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val colorScheme = getColorScheme(
        lightColors = lightColors,
        darkColors = darkColors,
        useDarkTheme = useDarkTheme,
        dynamicColor = dynamicColor,
    )
    val basicColors = remember { if (useDarkTheme) RetainBasicColorsDark else RetainBasicColorsLight }
    val basicColorsInverted = remember { if (useDarkTheme) RetainBasicColorsLight else RetainBasicColorsDark }

    // TODO Probably remove
    if (statusBarColor != null && !view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkTheme
        }
    }

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
