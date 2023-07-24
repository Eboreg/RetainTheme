package us.huseli.retaintheme.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = RetainColorLight.Primary,
    onPrimary = RetainColorLight.OnPrimary,
    primaryContainer = RetainColorLight.PrimaryContainer,
    onPrimaryContainer = RetainColorLight.OnPrimaryContainer,
    secondary = RetainColorLight.Secondary,
    onSecondary = RetainColorLight.OnSecondary,
    secondaryContainer = RetainColorLight.SecondaryContainer,
    onSecondaryContainer = RetainColorLight.OnSecondaryContainer,
    tertiary = RetainColorLight.Tertiary,
    onTertiary = RetainColorLight.OnTertiary,
    tertiaryContainer = RetainColorLight.TertiaryContainer,
    onTertiaryContainer = RetainColorLight.OnTertiaryContainer,
    error = RetainColorLight.Error,
    onError = RetainColorLight.OnError,
    errorContainer = RetainColorLight.ErrorContainer,
    onErrorContainer = RetainColorLight.OnErrorContainer,
    outline = RetainColorLight.Outline,
    background = RetainColorLight.Background,
    onBackground = RetainColorLight.OnBackground,
    surface = RetainColorLight.Surface,
    onSurface = RetainColorLight.OnSurface,
    surfaceVariant = RetainColorLight.SurfaceVariant,
    onSurfaceVariant = RetainColorLight.OnSurfaceVariant,
    inverseSurface = RetainColorLight.InverseSurface,
    inverseOnSurface = RetainColorLight.InverseOnSurface,
    inversePrimary = RetainColorLight.InversePrimary,
    surfaceTint = RetainColorLight.SurfaceTint,
    outlineVariant = RetainColorLight.OutlineVariant,
    scrim = RetainColorLight.Scrim,
)

private val DarkColors = darkColorScheme(
    primary = RetainColorDark.Primary,
    onPrimary = RetainColorDark.OnPrimary,
    primaryContainer = RetainColorDark.PrimaryContainer,
    onPrimaryContainer = RetainColorDark.OnPrimaryContainer,
    secondary = RetainColorDark.Secondary,
    onSecondary = RetainColorDark.OnSecondary,
    secondaryContainer = RetainColorDark.SecondaryContainer,
    onSecondaryContainer = RetainColorDark.OnSecondaryContainer,
    tertiary = RetainColorDark.Tertiary,
    onTertiary = RetainColorDark.OnTertiary,
    tertiaryContainer = RetainColorDark.TertiaryContainer,
    onTertiaryContainer = RetainColorDark.OnTertiaryContainer,
    error = RetainColorDark.Error,
    onError = RetainColorDark.OnError,
    errorContainer = RetainColorDark.ErrorContainer,
    onErrorContainer = RetainColorDark.OnErrorContainer,
    outline = RetainColorDark.Outline,
    background = RetainColorDark.Background,
    onBackground = RetainColorDark.OnBackground,
    surface = RetainColorDark.Surface,
    onSurface = RetainColorDark.OnSurface,
    surfaceVariant = RetainColorDark.SurfaceVariant,
    onSurfaceVariant = RetainColorDark.OnSurfaceVariant,
    inverseSurface = RetainColorDark.InverseSurface,
    inverseOnSurface = RetainColorDark.InverseOnSurface,
    inversePrimary = RetainColorDark.InversePrimary,
    surfaceTint = RetainColorDark.SurfaceTint,
    outlineVariant = RetainColorDark.OutlineVariant,
    scrim = RetainColorDark.Scrim,
)

@Composable
fun RetainTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
