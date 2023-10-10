@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.huseli.retaintheme.snackbar.SnackbarEngine

@Composable
fun SnackbarHosts(modifier: Modifier = Modifier) {
    SnackbarHost(SnackbarEngine.infoSnackbarHostState, modifier = modifier)
    SnackbarHost(SnackbarEngine.errorSnackbarHostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            actionColor = MaterialTheme.colorScheme.error,
            dismissActionContentColor = MaterialTheme.colorScheme.error,
        )
    }
}
