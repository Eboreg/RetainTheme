@file:Suppress("PropertyName")

package us.huseli.retaintheme.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

interface RetainBasicColors {
    val Brown: Color
    val Purple: Color
    val Cerulean: Color
    val Gray: Color
    val Pink: Color
    val Blue: Color
    val Red: Color
    val Yellow: Color
    val Green: Color
    val Teal: Color
    val Orange: Color
}

object RetainBasicColorsDark : RetainBasicColors {
    override val Brown = Color(0xff4b443a)
    override val Purple = Color(0xff472e5b)
    override val Cerulean = Color(0xff284255)
    override val Gray = Color(0xff232427)
    override val Pink = Color(0xff6c394f)
    override val Blue = Color(0xff256377)
    override val Red = Color(0xff77172e)
    override val Yellow = Color(0xff7c4a03)
    override val Green = Color(0xff264d3b)
    override val Teal = Color(0xff0c625d)
    override val Orange = Color(0xff692b17)
}

object RetainBasicColorsLight : RetainBasicColors {
    override val Brown = Color(0xffe9e3d4)
    override val Purple = Color(0xffd3bfdb)
    override val Cerulean = Color(0xffaeccdc)
    override val Gray = Color(0xffefeff1)
    override val Pink = Color(0xfff6e2dd)
    override val Blue = Color(0xffd4e4ed)
    override val Red = Color(0xfffaafa8)
    override val Yellow = Color(0xfffff8b8)
    override val Green = Color(0xffe2f6d4)
    override val Teal = Color(0xffb4ddd3)
    override val Orange = Color(0xfff39f76)
}

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
    onSurfaceVariant = Color(0xFF848787),
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

val LocalBasicColorsInverted: ProvidableCompositionLocal<RetainBasicColors> =
    staticCompositionLocalOf { RetainBasicColorsDark }
