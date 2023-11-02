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
    private val _error = MutableStateFlow<SnackbarMessage?>(null)
    private val _info = MutableStateFlow<SnackbarMessage?>(null)
    private val _shownIds = MutableStateFlow<List<UUID>>(emptyList())
    private val _isSnackbarShowing = MutableStateFlow(false)

    val errorSnackbarHostState = SnackbarHostState()
    val infoSnackbarHostState = SnackbarHostState()

    init {
        collectSnackbarMessages(_info, infoSnackbarHostState) { clearInfo() }
        collectSnackbarMessages(_error, errorSnackbarHostState) { clearError() }
    }

    fun addError(
        message: String,
        actionLabel: String,
        onActionPerformed: () -> Unit,
        onDismissed: (() -> Unit)? = null,
    ) {
        _error.value = SnackbarMessage(
            message = message,
            actionLabel = actionLabel,
            onActionPerformed = onActionPerformed,
            onDismissed = onDismissed,
        )
    }

    fun addError(message: String, onDismissed: (() -> Unit)? = null) {
        _error.value = SnackbarMessage(message = message, onDismissed = onDismissed)
    }

    fun addInfo(
        message: String,
        actionLabel: String,
        onActionPerformed: () -> Unit,
        onDismissed: (() -> Unit)? = null,
    ) {
        _info.value = SnackbarMessage(
            message = message,
            actionLabel = actionLabel,
            onActionPerformed = onActionPerformed,
            onDismissed = onDismissed,
        )
    }

    fun addInfo(message: String, onDismissed: (() -> Unit)? = null) {
        _info.value = SnackbarMessage(message = message, onDismissed = onDismissed)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearInfo() {
        _info.value = null
    }

    private inline fun collectSnackbarMessages(
        messageFlow: Flow<SnackbarMessage?>,
        hostState: SnackbarHostState,
        crossinline clearFunction: () -> Unit,
    ) = scope.launch {
        messageFlow
            .filterNotNull()
            .combine(_isSnackbarShowing.filter { !it }) { m, _ -> m }
            .distinctUntilChanged()
            .collect { message ->
                _isSnackbarShowing.value = true
                showSnackbarMessage(message, hostState)
                _shownIds.value += message.id
                clearFunction()
                _isSnackbarShowing.value = false
            }
    }

    private suspend fun showSnackbarMessage(message: SnackbarMessage, hostState: SnackbarHostState) {
        val result = hostState.showSnackbar(
            message = message.message,
            actionLabel = message.actionLabel,
            withDismissAction = true,
            duration = if (message.actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.Dismissed -> message.onDismissed?.invoke()
            SnackbarResult.ActionPerformed -> message.onActionPerformed?.invoke()
        }
    }
}
