@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.huseli.retaintheme.isInLandscapeMode

@Composable
fun ResponsiveScaffold(
    activeScreen: String?,
    mainMenuItems: List<MainMenuItem>,
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    portraitMenuModifier: Modifier = Modifier,
    landscapeMenuModifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    landscapeMenu: (@Composable ColumnScope.(PaddingValues) -> Unit)? = null,
    portraitMenu: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    if (isInLandscapeMode()) {
        Scaffold(
            modifier = modifier,
            snackbarHost = snackbarHost,
            bottomBar = bottomBar,
        ) { innerPadding ->
            Row {
                Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                    landscapeMenu?.let { it(innerPadding) } ?: VerticalMainMenu(
                        modifier = landscapeMenuModifier.padding(innerPadding),
                        activeScreen = activeScreen,
                        mainMenuItems = mainMenuItems,
                        onMenuItemClick = onMenuItemClick,
                    )
                }
                Box { content(innerPadding) }
            }
        }
    } else {
        Scaffold(
            snackbarHost = snackbarHost,
            bottomBar = bottomBar,
            topBar = portraitMenu ?: {
                HorizontalMainMenu(
                    modifier = portraitMenuModifier,
                    activeScreen = activeScreen,
                    mainMenuItems = mainMenuItems,
                    onMenuItemClick = onMenuItemClick,
                )
            },
            content = content,
        )
    }
}
