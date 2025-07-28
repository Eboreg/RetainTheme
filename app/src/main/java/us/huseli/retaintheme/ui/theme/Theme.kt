@file:Suppress("unused")

package us.huseli.retaintheme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

object RetainTheme {
    val bodyStyles: BodyTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalBodyTextStyles.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

@Composable
fun RetainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    darkColors: ColorScheme = DarkColors,
    lightColors: ColorScheme = LightColors,
    content: @Composable () -> Unit,
) {
    val colorScheme = getColorScheme(
        lightColors = lightColors,
        darkColors = darkColors,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
    )
    val basicColors = remember { if (darkTheme) RetainBasicColorsDark else RetainBasicColorsLight }
    val basicColorsInverted = remember { if (darkTheme) RetainBasicColorsLight else RetainBasicColorsDark }
    val bodyTextStyles = BodyTextStyles(
        primary = typography.bodyLarge,
        primaryBold = typography.titleMedium,
        primarySmall = typography.bodyMedium,
        primarySmallBold = typography.titleSmall,
        primaryExtraSmall = typography.bodySmall,
        secondary = typography.bodyLarge.copy(color = colorScheme.onSurfaceVariant),
        secondaryBold = typography.titleMedium.copy(color = colorScheme.onSurfaceVariant),
        secondarySmall = typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant),
        secondarySmallBold = typography.titleSmall.copy(color = colorScheme.onSurfaceVariant),
        secondaryExtraSmall = typography.bodySmall.copy(color = colorScheme.onSurfaceVariant),
    )

    CompositionLocalProvider(
        LocalBodyTextStyles provides bodyTextStyles,
        LocalBasicColors provides basicColors,
        LocalBasicColorsInverted provides basicColorsInverted,
    ) {
        MaterialTheme(
            shapes = shapes,
            colorScheme = colorScheme,
            content = content,
        )
    }
}
