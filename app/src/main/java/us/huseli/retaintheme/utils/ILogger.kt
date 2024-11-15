package us.huseli.retaintheme.utils

import android.util.Log
import us.huseli.retaintheme.snackbar.SnackbarEngine

@Suppress("unused")
interface ILogger {
    private fun formatMessage(message: String): String = "[${getThreadSignature()}] $message"

    private fun getThreadSignature(): String {
        val thread = Thread.currentThread()
        val ret = "${thread.name}:${thread.id}:${thread.priority}"

        return thread.threadGroup?.name?.let { "$ret:$it" } ?: ret
    }

    fun logImpl(
        priority: Int,
        tag: String? = null,
        message: String? = null,
        force: Boolean = false,
        exception: Throwable? = null,
    )

    fun log(
        message: String? = null,
        priority: Int = Log.INFO,
        tag: String = javaClass.simpleName,
        force: Boolean = false,
        showSnackbar: Boolean = false,
        exception: Throwable? = null,
    ) {
        val logMessage = message?.let { formatMessage(message) } ?: exception?.message ?: exception?.toString()
        val snackbarMessage = message ?: exception?.message ?: exception?.toString()

        logImpl(
            priority = priority,
            tag = tag,
            message = logMessage,
            force = force,
            exception = exception,
        )
        if (showSnackbar && snackbarMessage != null) {
            if (priority <= 5) SnackbarEngine.addInfo(message = snackbarMessage)
            else SnackbarEngine.addError(message = snackbarMessage)
        }
    }

    fun logError(
        message: String? = null,
        exception: Throwable? = null,
        tag: String = javaClass.simpleName,
        force: Boolean = false,
        showSnackbar: Boolean = false
    ) = log(
        priority = Log.ERROR,
        tag = tag,
        message = message,
        exception = exception,
        showSnackbar = showSnackbar,
        force = force,
    )

    fun logWarning(
        message: String? = null,
        exception: Throwable? = null,
        tag: String = javaClass.simpleName,
        force: Boolean = false,
        showSnackbar: Boolean = false,
    ) = log(
        priority = Log.WARN,
        tag = tag,
        message = message,
        exception = exception,
        showSnackbar = showSnackbar,
        force = force,
    )

    fun showErrorSnackbar(message: String) = logError(message = message, showSnackbar = true)

    fun showErrorSnackbar(exception: Throwable) = logError(exception = exception, showSnackbar = true)

    fun showInfoSnackbar(message: String) = log(message = message, showSnackbar = true)
}
