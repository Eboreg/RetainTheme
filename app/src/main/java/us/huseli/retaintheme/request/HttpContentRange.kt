package us.huseli.retaintheme.request

/** Ref: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Range */
data class HttpContentRange(
    val unit: String,
    val rangeStart: Int,
    val rangeEnd: Int,
    val size: Int? = null,
)

fun String.parseContentRange(): HttpContentRange? =
    Regex("(\\w+) (\\d+)-(\\d+)/(\\d+|\\*)").find(this)?.groupValues?.let {
        HttpContentRange(
            unit = it[1],
            rangeStart = it[2].toInt(),
            rangeEnd = it[3].toInt(),
            size = if (it[4] == "*") null else it[4].toInt(),
        )
    }
