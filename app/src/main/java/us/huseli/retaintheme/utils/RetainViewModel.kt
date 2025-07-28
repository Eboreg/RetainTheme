@file:Suppress("unused")

package us.huseli.retaintheme.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

abstract class RetainViewModel : ViewModel() {
    suspend fun <T> onIOThread(block: suspend CoroutineScope.() -> T) =
        withContext(context = Dispatchers.IO, block = block)

    suspend fun <T> onMainThread(block: suspend CoroutineScope.() -> T) =
        withContext(context = Dispatchers.Main, block = block)

    fun <T> Flow<T>.stateWhileSubscribed(
        initialValue: T,
        stopTimeoutMillis: Long = 5_000,
    ): StateFlow<T> = stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), initialValue)

    fun <T> Flow<T>.stateWhileSubscribed(): StateFlow<T?> = stateWhileSubscribed(null)

    fun <T> Flow<T>.stateEagerly(initialValue: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)

    fun <T> Flow<T>.stateEagerly(): StateFlow<T?> = stateEagerly(null)

    fun <T> Flow<T>.stateLazily(initialValue: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Lazily, initialValue)

    fun <T> Flow<T?>.stateLazily(): StateFlow<T?> = stateLazily(null)
}
