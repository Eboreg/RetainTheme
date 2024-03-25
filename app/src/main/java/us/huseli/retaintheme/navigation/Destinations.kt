@file:Suppress("unused")

package us.huseli.retaintheme.navigation

import androidx.navigation.NamedNavArgument

abstract class AbstractDestination<MI : Enum<MI>> {
    abstract val routeTemplate: String
    abstract val arguments: List<NamedNavArgument>
    open val menuItemId: MI? = null
}

abstract class AbstractSimpleDestination<MI : Enum<MI>>(
    override val routeTemplate: String,
    override val menuItemId: MI? = null,
) : AbstractDestination<MI>() {
    override val arguments: List<NamedNavArgument> = emptyList()
    val route: String
        get() = routeTemplate
}
