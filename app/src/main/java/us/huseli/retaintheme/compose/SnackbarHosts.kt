@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.huseli.retaintheme.snackbar.SnackbarColors
import us.huseli.retaintheme.snackbar.SnackbarEngine
import us.huseli.retaintheme.snackbar.errorSnackbarColors
import us.huseli.retaintheme.snackbar.infoSnackbarColors

@Composable
fun SnackbarHosts(
    modifier: Modifier = Modifier,
    infoColors: SnackbarColors = infoSnackbarColors(),
    errorColors: SnackbarColors = errorSnackbarColors(),
) {
    SnackbarHost(SnackbarEngine.infoSnackbarHostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = infoColors.containerColor,
            contentColor = infoColors.contentColor,
            actionColor = infoColors.actionColor,
            actionContentColor = infoColors.actionContentColor,
            dismissActionContentColor = infoColors.dismissActionContentColor,
        )
    }
    SnackbarHost(SnackbarEngine.errorSnackbarHostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = errorColors.containerColor,
            contentColor = errorColors.contentColor,
            actionColor = errorColors.actionColor,
            actionContentColor = errorColors.actionContentColor,
            dismissActionContentColor = errorColors.dismissActionContentColor,
        )
    }
}
