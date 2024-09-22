package us.huseli.retaintheme.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

data class SnackbarColors(
    val containerColor: Color,
    val contentColor: Color,
    val actionColor: Color,
    val actionContentColor: Color,
    val dismissActionContentColor: Color,
)

@Composable
fun infoSnackbarColors() = SnackbarColors(
    containerColor = SnackbarDefaults.color,
    contentColor = SnackbarDefaults.contentColor,
    actionColor = SnackbarDefaults.actionColor,
    actionContentColor = SnackbarDefaults.actionContentColor,
    dismissActionContentColor = SnackbarDefaults.dismissActionContentColor,
)

@Composable
@ReadOnlyComposable
fun errorSnackbarColors() = SnackbarColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
    actionColor = MaterialTheme.colorScheme.error,
    actionContentColor = MaterialTheme.colorScheme.onError,
    dismissActionContentColor = MaterialTheme.colorScheme.error,
)
