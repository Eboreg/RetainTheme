@file:Suppress("unused")

package us.huseli.retaintheme.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LightColors = lightColorScheme(
    background = Color(0xFFFAFDFD),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    inverseOnSurface = Color(0xFFEFF1F1),
    inversePrimary = Color(0xFF85CFFF),
    inverseSurface = Color(0xFF2E3132),
    onBackground = Color(0xFF191C1D),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF001E2E),
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF0B1D29),
    onSurface = Color(0xFF191C1D),
    onSurfaceVariant = Color(0xFF3F484A),
    onTertiary = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF171E00),
    outline = Color(0xFF6F797A),
    outlineVariant = Color(0xFFBFC8CA),
    primary = Color(0xFF00658F),
    primaryContainer = Color(0xFFC7E7FF),
    scrim = Color(0xFF000000),
    secondary = Color(0xFF4F616E),
    secondaryContainer = Color(0xFFD2E5F5),
    surface = Color(0xFFF8FAFA),
    surfaceTint = Color(0xFF00658F),
    surfaceVariant = Color(0xFFDBE4E6),
    tertiary = Color(0xFF526600),
    tertiaryContainer = Color(0xFFD4ED7F),
)

val DarkColors = darkColorScheme(
    background = Color(0xFF191C1D),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    inverseOnSurface = Color(0xFF191C1D),
    inversePrimary = Color(0xFF00658F),
    inverseSurface = Color(0xFFE1E3E3),
    onBackground = Color(0xFFE1E3E3),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    onPrimary = Color(0xFF00344C),
    onPrimaryContainer = Color(0xFFC7E7FF),
    onSecondary = Color(0xFF21323E),
    onSecondaryContainer = Color(0xFFD2E5F5),
    onSurface = Color(0xFFC4C7C7),
    onSurfaceVariant = Color(0xFFBFC8CA),
    onTertiary = Color(0xFF293500),
    onTertiaryContainer = Color(0xFFD4ED7F),
    outline = Color(0xFF899294),
    outlineVariant = Color(0xFF3F484A),
    primary = Color(0xFF85CFFF),
    primaryContainer = Color(0xFF004C6C),
    scrim = Color(0xFF000000),
    secondary = Color(0xFFB6C9D8),
    secondaryContainer = Color(0xFF374955),
    surface = Color(0xFF101415),
    surfaceTint = Color(0xFF85CFFF),
    surfaceVariant = Color(0xFF3F484A),
    tertiary = Color(0xFFB8D166),
    tertiaryContainer = Color(0xFF3D4D00),
)

@Composable
fun getColorScheme(
    lightColors: ColorScheme = LightColors,
    darkColors: ColorScheme = DarkColors,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> darkColors
        else -> lightColors
    }
}

val LocalBasicColors: ProvidableCompositionLocal<RetainBasicColors> =
    staticCompositionLocalOf { RetainBasicColorsLight }

@Composable
fun RetainTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    typography: Typography = Typography,
    shapes: Shapes = MaterialTheme.shapes,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = getColorScheme(useDarkTheme = useDarkTheme, dynamicColor = dynamicColor)
    val basicColors = remember { if (useDarkTheme) RetainBasicColorsDark else RetainBasicColorsLight }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkTheme
        }
    }

    CompositionLocalProvider(LocalBasicColors provides basicColors) {
        MaterialTheme(
            shapes = shapes,
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
