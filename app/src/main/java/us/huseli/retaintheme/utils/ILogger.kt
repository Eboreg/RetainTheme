package us.huseli.retaintheme.utils

import android.util.Log
import us.huseli.retaintheme.snackbar.SnackbarEngine

data class LogInstance(
    val priority: Int,
    val tag: String,
    val message: String? = null,
    val exception: Throwable? = null,
    val force: Boolean = false,
    val showSnackbar: Boolean = false,
) {
    val exceptionString: String?
        get() = exception?.message ?: exception?.toString()

    val snackbarMessage: String?
        get() = if (showSnackbar) message ?: exceptionString else null
}

@Suppress("unused")
interface ILogger {
    private fun formatMessage(message: String): String = "[${getThreadSignature()}] $message"

    private fun getThreadSignature(): String {
        val thread = Thread.currentThread()
        val ret = "${thread.name}:${thread.id}:${thread.priority}"

        return thread.threadGroup?.name?.let { "$ret:$it" } ?: ret
    }

    fun shouldLog(log: LogInstance): Boolean

    fun log(log: LogInstance) {
        val logMessage = log.message?.let { formatMessage(log.message) } ?: log.exceptionString

        if (log.force || shouldLog(log)) {
            if (log.priority >= Log.ERROR) Log.e(log.tag, logMessage, log.exception)
            else if (log.priority == Log.WARN) Log.w(log.tag, logMessage, log.exception)
            else Log.i(log.tag, logMessage, log.exception)
        }
        log.snackbarMessage?.also {
            if (log.priority <= Log.WARN) SnackbarEngine.addInfo(message = it)
            else SnackbarEngine.addError(message = it)
        }
    }

    fun log(
        tag: String,
        message: String,
        priority: Int = Log.INFO,
        force: Boolean = false,
        showSnackbar: Boolean = false,
        exception: Throwable? = null,
    ) = log(
        LogInstance(
            exception = exception,
            force = force,
            message = message,
            priority = priority,
            showSnackbar = showSnackbar,
            tag = tag,
        )
    )

    fun log(
        message: String,
        priority: Int = Log.INFO,
        force: Boolean = false,
        showSnackbar: Boolean = false,
        exception: Throwable? = null,
    ) = log(
        exception = exception,
        force = force,
        message = message,
        priority = priority,
        showSnackbar = showSnackbar,
        tag = javaClass.simpleName,
    )

    fun logError(
        tag: String,
        message: String? = null,
        exception: Throwable? = null,
        force: Boolean = false,
        showSnackbar: Boolean = false
    ) = log(
        LogInstance(
            exception = exception,
            force = force,
            message = message,
            priority = Log.ERROR,
            showSnackbar = showSnackbar,
            tag = tag,
        )
    )

    fun logError(
        message: String? = null,
        exception: Throwable? = null,
        force: Boolean = false,
        showSnackbar: Boolean = false
    ) = logError(
        exception = exception,
        force = force,
        message = message,
        showSnackbar = showSnackbar,
        tag = javaClass.simpleName,
    )

    fun logWarning(
        tag: String,
        message: String? = null,
        exception: Throwable? = null,
        force: Boolean = false,
        showSnackbar: Boolean = false,
    ) = log(
        LogInstance(
            exception = exception,
            force = force,
            message = message,
            priority = Log.WARN,
            showSnackbar = showSnackbar,
            tag = tag,
        )
    )

    fun logWarning(
        message: String? = null,
        exception: Throwable? = null,
        force: Boolean = false,
        showSnackbar: Boolean = false,
    ) = logWarning(
        exception = exception,
        force = force,
        message = message,
        showSnackbar = showSnackbar,
        tag = javaClass.simpleName,
    )
}
