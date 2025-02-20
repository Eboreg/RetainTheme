@file:Suppress("unused")

package us.huseli.retaintheme

import us.huseli.retaintheme.request.Request

open class RetainError(cause: Throwable? = null, message: String? = cause?.message) : Exception(message, cause) {
    constructor(message: String?) : this(cause = null, message = message)
}

open class RetainConnectionError(
    val url: String,
    val method: Request.Method,
    message: String? = null,
    cause: Throwable? = null,
) : RetainError(cause = cause, message = message)

class RetainHttpError(
    url: String,
    method: Request.Method,
    val statusCode: Int,
    message: String? = null,
    cause: Throwable? = null,
) : RetainConnectionError(url = url, method = method, message = message, cause = cause)
