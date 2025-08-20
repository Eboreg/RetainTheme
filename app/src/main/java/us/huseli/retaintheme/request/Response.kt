@file:Suppress("unused")

package us.huseli.retaintheme.request

class Response<T>(
    val request: Request,
    val responseCode: Int,
    val responseMessage: String?,
    val contentRange: HttpContentRange?,
    val contentLength: Long?,
    val headers: Map<String, List<String>>,
    val data: T,
    val elapsed: Long,
    val kbps: Int?,
)
