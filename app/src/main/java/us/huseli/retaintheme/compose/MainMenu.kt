package us.huseli.retaintheme.compose

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
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

class MainMenuItem(
    val contentScreen: String,
    val icon: ImageVector,
    val description: String? = null,
)

@Composable
fun HorizontalMainMenu(
    activeScreen: String?,
    mainMenuItems: List<MainMenuItem>,
    onMenuItemClick: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalMainMenu(
    modifier: Modifier = Modifier,
    activeScreen: String?,
    mainMenuItems: List<MainMenuItem>,
    onMenuItemClick: (String) -> Unit,
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
