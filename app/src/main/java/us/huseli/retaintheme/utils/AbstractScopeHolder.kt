package us.huseli.retaintheme.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class AbstractScopeHolder {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun launchOnIOThread(block: suspend CoroutineScope.() -> Unit) =
        scope.launch(Dispatchers.IO, block = block)

    fun launchOnMainThread(block: suspend CoroutineScope.() -> Unit) =
        scope.launch(Dispatchers.Main, block = block)

    suspend fun <T> onIOThread(block: suspend CoroutineScope.() -> T) =
        withContext(context = Dispatchers.IO, block = block)

    suspend fun <T> onMainThread(block: suspend CoroutineScope.() -> T) =
        withContext(context = Dispatchers.Main, block = block)

    fun <T> Flow<T>.stateEagerly(initialValue: T): StateFlow<T> =
        stateIn(scope, SharingStarted.Eagerly, initialValue)

    fun <T> Flow<T>.stateEagerly(): StateFlow<T?> = stateEagerly(null)

    fun <T> Flow<T>.stateLazily(initialValue: T): StateFlow<T> =
        stateIn(scope, SharingStarted.Lazily, initialValue)

    fun <T> Flow<T?>.stateLazily(): StateFlow<T?> = stateLazily(null)

    fun <T> Flow<T>.stateWhileSubscribed(initialValue: T): StateFlow<T> =
        stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)

    fun <T> Flow<T>.stateWhileSubscribed(): StateFlow<T?> = stateWhileSubscribed(null)
}
