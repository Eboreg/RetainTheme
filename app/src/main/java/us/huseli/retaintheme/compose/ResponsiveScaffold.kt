@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.huseli.retaintheme.isInLandscapeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveScaffold(
    activeScreen: String?,
    mainMenuItems: List<MainMenuItem>,
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    horizontalMenuModifier: Modifier = Modifier,
    verticalMenuModifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
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
                    VerticalMainMenu(
                        modifier = verticalMenuModifier.padding(innerPadding),
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
            topBar = {
                HorizontalMainMenu(
                    modifier = horizontalMenuModifier,
                    activeScreen = activeScreen,
                    mainMenuItems = mainMenuItems,
                    onMenuItemClick = onMenuItemClick,
                )
            },
            content = content,
        )
    }
}
