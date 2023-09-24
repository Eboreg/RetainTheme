@file:Suppress("unused")

package us.huseli.retaintheme.navigation

import androidx.navigation.NamedNavArgument

abstract class AbstractDestination {
    abstract val routeTemplate: String
    abstract val arguments: List<NamedNavArgument>
}

abstract class AbstractSimpleDestination(override val routeTemplate: String) : AbstractDestination() {
    override val arguments: List<NamedNavArgument> = emptyList()
    val route: String
        get() = routeTemplate
}
