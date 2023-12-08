package us.huseli.retaintheme.compose

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

class MainMenuItem<T : Enum<T>>(
    val contentScreen: T,
    val icon: ImageVector,
    val description: String? = null,
)

@Composable
inline fun <T : Enum<T>> HorizontalMainMenu(
    modifier: Modifier = Modifier,
    activeScreen: T?,
    mainMenuItems: List<MainMenuItem<T>>,
    crossinline onMenuItemClick: (T) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        mainMenuItems.forEach { item ->
            NavigationBarItem(
                selected = activeScreen == item.contentScreen,
                onClick = { onMenuItemClick(item.contentScreen) },
                icon = { Icon(item.icon, null) },
                label = item.description?.let { { Text(item.description) } },
            )
        }
    }
}

@Composable
inline fun <T : Enum<T>> VerticalMainMenu(
    modifier: Modifier = Modifier,
    activeScreen: T?,
    mainMenuItems: List<MainMenuItem<T>>,
    crossinline onMenuItemClick: (T) -> Unit,
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.widthIn(max = 200.dp)) {
                mainMenuItems.forEach { item ->
                    NavigationDrawerItem(
                        modifier = Modifier.height(50.dp),
                        icon = { Icon(item.icon, null) },
                        label = { item.description?.let { Text(item.description) } },
                        selected = activeScreen == item.contentScreen,
                        onClick = { onMenuItemClick(item.contentScreen) },
                        shape = RectangleShape,
                    )
                }
            }
        },
        content = {},
    )
}
