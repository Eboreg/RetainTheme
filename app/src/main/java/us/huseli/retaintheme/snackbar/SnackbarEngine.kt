package us.huseli.retaintheme.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.UUID

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SnackbarEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val error = MutableStateFlow<SnackbarMessage?>(null)
    private val info = MutableStateFlow<SnackbarMessage?>(null)
    private val shownIds = MutableStateFlow<List<UUID>>(emptyList())
    private val isSnackbarShowing = MutableStateFlow(false)

    val errorSnackbarHostState = SnackbarHostState()
    val infoSnackbarHostState = SnackbarHostState()

    init {
        collectSnackbarMessages(info, infoSnackbarHostState) { clearInfo() }
        collectSnackbarMessages(error, errorSnackbarHostState) { clearError() }
    }

    fun addError(
        message: String,
        actionLabel: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
        onActionPerformed: () -> Unit,
        onDismissed: (() -> Unit)? = null,
    ) {
        error.value = SnackbarMessage(
            message = message,
            actionLabel = actionLabel,
            onActionPerformed = onActionPerformed,
            onDismissed = onDismissed,
            duration = duration,
        )
    }

    fun addError(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onDismissed: (() -> Unit)? = null,
    ) {
        error.value = SnackbarMessage(message = message, onDismissed = onDismissed, duration = duration)
    }

    fun addInfo(
        message: String,
        actionLabel: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
        onActionPerformed: () -> Unit,
        onDismissed: (() -> Unit)? = null,
    ) {
        info.value = SnackbarMessage(
            message = message,
            actionLabel = actionLabel,
            onActionPerformed = onActionPerformed,
            onDismissed = onDismissed,
            duration = duration,
        )
    }

    fun addInfo(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onDismissed: (() -> Unit)? = null,
    ) {
        info.value = SnackbarMessage(message = message, onDismissed = onDismissed, duration = duration)
    }

    fun clearError() {
        error.value = null
    }

    fun clearInfo() {
        info.value = null
    }

    private inline fun collectSnackbarMessages(
        messageFlow: Flow<SnackbarMessage?>,
        hostState: SnackbarHostState,
        crossinline clearFunction: () -> Unit,
    ) = scope.launch {
        messageFlow
            .filterNotNull()
            .combine(isSnackbarShowing.filter { !it }) { m, _ -> m }
            .distinctUntilChanged()
            .collect { message ->
                isSnackbarShowing.value = true
                showSnackbarMessage(message, hostState)
                shownIds.value += message.id
                clearFunction()
                isSnackbarShowing.value = false
            }
    }

    private suspend fun showSnackbarMessage(message: SnackbarMessage, hostState: SnackbarHostState) {
        val result = hostState.showSnackbar(
            message = message.message,
            actionLabel = message.actionLabel,
            withDismissAction = true,
            duration = message.duration,
        )
        when (result) {
            SnackbarResult.Dismissed -> message.onDismissed?.invoke()
            SnackbarResult.ActionPerformed -> message.onActionPerformed?.invoke()
        }
    }
}
