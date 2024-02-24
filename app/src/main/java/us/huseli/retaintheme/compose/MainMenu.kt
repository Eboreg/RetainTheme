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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Suppress("unused")
open class MenuItem<MI : Enum<MI>>(
    val id: MI,
    val icon: @Composable () -> Unit,
    val description: String? = null,
) {
    constructor(id: MI, imageVector: ImageVector, description: String? = null) :
        this(id, { Icon(imageVector, null) }, description)

    constructor(id: MI, painter: Painter, description: String? = null) :
        this(id, { Icon(painter, null) }, description)

    constructor(id: MI, imageBitmap: ImageBitmap, description: String? = null) :
        this(id, { Icon(imageBitmap, null) }, description)
}

@Composable
inline fun <MI : Enum<MI>> HorizontalMainMenu(
    modifier: Modifier = Modifier,
    activeMenuItemId: MI?,
    menuItems: List<MenuItem<MI>>,
    crossinline onMenuItemClick: (MI) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        menuItems.forEach { item ->
            NavigationBarItem(
                selected = activeMenuItemId == item.id,
                onClick = { onMenuItemClick(item.id) },
                icon = item.icon,
                label = item.description?.let { { Text(item.description) } },
            )
        }
    }
}

@Composable
inline fun <MI : Enum<MI>> VerticalMainMenu(
    modifier: Modifier = Modifier,
    activeMenuItemId: MI?,
    menuItems: List<MenuItem<MI>>,
    crossinline onMenuItemClick: (MI) -> Unit,
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.widthIn(max = 200.dp)) {
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        modifier = Modifier.height(50.dp),
                        icon = item.icon,
                        label = { item.description?.let { Text(item.description) } },
                        selected = activeMenuItemId == item.id,
                        onClick = { onMenuItemClick(item.id) },
                        shape = RectangleShape,
                    )
                }
            }
        },
        content = {},
    )
}
