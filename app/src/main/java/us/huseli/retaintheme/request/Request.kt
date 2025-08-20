@file:Suppress("unused")

package us.huseli.retaintheme.request

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import us.huseli.retaintheme.RetainConnectionError
import us.huseli.retaintheme.RetainHttpError
import us.huseli.retaintheme.extensions.queryMap
import us.huseli.retaintheme.utils.ILogger
import us.huseli.retaintheme.utils.LogInstance
import java.io.InputStream
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import kotlin.math.roundToInt
import kotlin.text.Charsets.UTF_8

class Request(
    url: String,
    query: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val method: Method = Method.GET,
    private val body: String? = null,
    private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
    private val readTimeout: Int = DEFAULT_READ_TIMEOUT,
    private val suppressLogs: Boolean = false,
) : ILogger {
    enum class Method(val value: String) {
        GET("GET"),
        POST("POST"),
        HEAD("HEAD"),
        OPTIONS("OPTIONS"),
        PUT("PUT"),
        DELETE("DELETE"),
        TRACE("TRACE"),
    }

    val url = constructUrl(url, query)

    var contentLength: Long? = null
        private set
    var contentRange: HttpContentRange? = null
        private set
    var requestStart: Long? = null
        private set
    var responseCode: Int? = null
        private set

    fun finish(receivedBytes: Int? = null) {
        requestStart?.also { start ->
            val elapsed = (System.currentTimeMillis() - start).toDouble() / 1000
            var message = "FINISH ${method.value} $url: ${elapsed}s"

            if (receivedBytes != null) {
                val kbps = ((receivedBytes / elapsed) / 1024).roundToInt()
                message += ", ${receivedBytes}B ($kbps KB/s)"
            }

            log("Request", message, priority = Log.DEBUG)
        }
    }

    @WorkerThread
    @Deprecated("Use getBitmapResponse()")
    fun getBitmap(): Bitmap? = getInputStream().use { BitmapFactory.decodeStream(it) }.also { finish() }

    @WorkerThread
    fun getBitmapResponse(): Response<Bitmap?> = getResponse { BitmapFactory.decodeStream(it) }

    @WorkerThread
    fun getByteArrayResponse(): Response<ByteArray> = getResponse { it.readBytes() }

    @WorkerThread
    fun getInputStream(): InputStream {
        val conn = connect()
        val isGzipped = conn.headerFields["Content-Encoding"]?.contains("gzip") == true

        return if (isGzipped) GZIPInputStream(conn.inputStream) else conn.inputStream
    }

    @WorkerThread
    @Deprecated("Use getJsonArrayResponse()")
    fun getJsonArray(gson: Gson = Request.gson): List<*> = getInputStream().use {
        gson.fromJson(it.bufferedReader(), jsonArrayResponseType) ?: emptyList<Any>()
    }.also { finish() }

    @WorkerThread
    fun getJsonArrayResponse(gson: Gson = Request.gson): Response<List<*>> = getResponse {
        gson.fromJson(it.bufferedReader(), jsonArrayResponseType) ?: emptyList<Any>()
    }

    @WorkerThread
    @Deprecated("Use getJsonObjectResponse()")
    fun getJsonObject(gson: Gson = Request.gson): Map<String, *> = getInputStream().use {
        gson.fromJson(it.bufferedReader(), jsonObjectResponseType) ?: emptyMap<String, Any>()
    }.also { finish() }

    @WorkerThread
    fun getJsonObjectResponse(gson: Gson = Request.gson): Response<Map<String, *>> = getResponse {
        gson.fromJson(it.bufferedReader(), jsonObjectResponseType) ?: emptyMap<String, Any>()
    }

    @WorkerThread
    @Deprecated("Use getObjectResponse()")
    inline fun <reified T> getObject(gson: Gson = Request.gson): T =
        getInputStream().use { gson.fromJson(it.bufferedReader(), T::class.java) }.also { finish() }

    @WorkerThread
    inline fun <reified T> getObjectResponse(gson: Gson = Request.gson): Response<T> =
        getResponse { gson.fromJson(it.bufferedReader(), T::class.java) }

    @WorkerThread
    @Deprecated("Use getObjectResponse()")
    fun <T> getObject(typeOfT: Type, gson: Gson = Request.gson): T =
        getInputStream().use { gson.fromJson<T>(it.bufferedReader(), typeOfT) }.also { finish() }

    @WorkerThread
    fun <T> getObjectResponse(typeOfT: Type, gson: Gson = Request.gson): Response<T> =
        getResponse { gson.fromJson<T>(it.bufferedReader(), typeOfT) }

    @WorkerThread
    @Deprecated("Use getObjectResponseOrNull()")
    inline fun <reified T> getObjectOrNull(gson: Gson = Request.gson): T? = try {
        @Suppress("DEPRECATION")
        getObject<T>(gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    @WorkerThread
    inline fun <reified T> getObjectResponseOrNull(gson: Gson = Request.gson): Response<T>? = try {
        getObjectResponse<T>(gson)
    } catch (e: Throwable) {
        logError("getObjectOrNullResponse(): $method $url", e)
        null
    }

    @WorkerThread
    @Deprecated("Use getObjectResponseOrNull()")
    fun <T> getObjectOrNull(typeOfT: Type, gson: Gson = Request.gson): T? = try {
        @Suppress("DEPRECATION")
        getObject(typeOfT, gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    @WorkerThread
    fun <T> getObjectResponseOrNull(typeOfT: Type, gson: Gson = Request.gson): Response<T>? = try {
        getObjectResponse(typeOfT, gson)
    } catch (e: Throwable) {
        logError("getObjectResponseOrNull(): $method $url", e)
        null
    }

    @WorkerThread
    fun <T> getResponse(callback: (InputStream) -> T): Response<T> {
        val start = System.currentTimeMillis()
        val conn = connect()
        val isGzipped = conn.headerFields["Content-Encoding"]?.contains("gzip") == true
        val inputStream = if (isGzipped) GZIPInputStream(conn.inputStream) else conn.inputStream
        val data = inputStream.use { callback(it) }
        val contentRange = conn.getHeaderField("Content-Range")?.parseContentRange()
        val contentLength = when {
            contentRange?.size != null -> contentRange.size
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> conn.contentLengthLong
            else -> conn.getHeaderField("Content-Length")?.toLong()
        }
        val elapsed = System.currentTimeMillis() - start
        val elapsedSeconds = elapsed.toDouble() / 1000

        return Response(
            request = this,
            responseCode = conn.responseCode,
            responseMessage = conn.responseMessage,
            contentLength = contentLength,
            headers = conn.headerFields,
            contentRange = contentRange,
            data = data,
            elapsed = elapsed,
            kbps = contentLength?.let { ((it / elapsedSeconds) / 1024).roundToInt() },
        )
    }

    @WorkerThread
    @Deprecated("Use getStringResponse()")
    fun getString(): String =
        getInputStream().use { it.bufferedReader().readText() }.also { finish(it.length) }

    @WorkerThread
    fun getStringResponse(): Response<String> = getResponse { it.bufferedReader().readText() }

    private fun connect(): HttpURLConnection {
        requestStart = System.currentTimeMillis()
        log("START ${method.value} $url", priority = Log.DEBUG)
        if (body != null) log("BODY $body", priority = Log.DEBUG)

        val conn = URL(url).openConnection() as HttpURLConnection

        conn.connectTimeout = connectTimeout
        conn.readTimeout = readTimeout
        conn.requestMethod = method.value

        headers.forEach { (key, value) -> conn.setRequestProperty(key, value) }

        if (body != null && method == Method.POST) {
            try {
                val binaryBody = body.toByteArray(UTF_8)

                conn.doOutput = true
                conn.setFixedLengthStreamingMode(binaryBody.size)
                conn.outputStream.write(binaryBody, 0, binaryBody.size)
            } catch (e: Throwable) {
                throw RetainConnectionError(url = url, method = method, cause = e)
            }
        }

        val responseCode = try {
            conn.responseCode
        } catch (e: Throwable) {
            throw RetainConnectionError(url = url, method = method, cause = e)
        }

        this@Request.responseCode = responseCode
        if (responseCode >= 400) throw RetainHttpError(
            url = url,
            method = method,
            statusCode = responseCode,
            message = conn.responseMessage,
        )

        contentRange = conn.getHeaderField("Content-Range")?.parseContentRange()
        contentLength = contentRange?.size ?: conn.getHeaderField("Content-Length")?.toLong()

        return conn
    }

    override fun shouldLog(log: LogInstance): Boolean {
        if (suppressLogs) return false
        return super.shouldLog(log)
    }

    class Builder(url: String) {
        private val _headers = mutableMapOf<String, String>()
        private val _query = mutableMapOf<String, String>()

        var body: String? = null
            private set
        var connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT
            private set
        val headers: Map<String, String>
            get() = _headers.toMap()
        var method: Method = Method.GET
            private set
        val query: Map<String, String>
            get() = _query.toMap()
        var readTimeout: Int = DEFAULT_READ_TIMEOUT
            private set
        var url: String = url
            private set

        init {
            setUrl(url, true)
        }

        override fun equals(other: Any?): Boolean = other is Builder && other.hashCode() == hashCode()

        override fun hashCode(): Int {
            var result = _headers.hashCode()
            result = 31 * result + _query.hashCode()
            result = 31 * result + (body?.hashCode() ?: 0)
            result = 31 * result + method.hashCode()
            result = 31 * result + url.hashCode()
            return result
        }

        fun appendHeaders(value: Map<String, String>): Builder =
            apply { if (value.isNotEmpty()) _headers.putAll(value) }

        fun appendHeaders(vararg value: Pair<String, String>): Builder = appendHeaders(value.toMap())

        fun appendQuery(value: Map<String, String>): Builder = apply { if (value.isNotEmpty()) _query.putAll(value) }

        fun appendQuery(vararg value: Pair<String, String>): Builder = appendQuery(value.toMap())

        fun build(): Request = Request(
            url = url,
            query = _query,
            headers = _headers,
            method = method,
            body = body,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
        )

        fun deleteHeaders(vararg keys: String): Builder = apply { keys.forEach { _headers.remove(it) } }

        fun deleteQuery(vararg keys: String): Builder = apply { keys.forEach { _query.remove(it) } }

        fun setBody(value: String?): Builder = apply { body = value }

        fun setHeaders(value: Map<String, String>): Builder = apply {
            _headers.clear()
            if (value.isNotEmpty()) _headers.putAll(value)
        }

        fun setHeaders(vararg value: Pair<String, String>): Builder = setHeaders(value.toMap())

        fun setMethod(value: Method): Builder = apply { method = value }

        fun setQuery(value: Map<String, String>): Builder = apply {
            _query.clear()
            if (value.isNotEmpty()) _query.putAll(value)
        }

        fun setQuery(vararg value: Pair<String, String>): Builder = setQuery(value.toMap())

        fun setUrl(value: String, clearQuery: Boolean = false): Builder = apply {
            val uri = value.toUri()
            val query = uri.queryMap

            this.url = uri.buildUpon().clearQuery().build().toString()

            if (query.isNotEmpty()) {
                if (clearQuery) setQuery(query)
                else appendQuery(query)
            }
        }
    }

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT = 4_050
        const val DEFAULT_READ_TIMEOUT = 10_000

        val gson: Gson = GsonBuilder().create()
        val jsonArrayResponseType = object : TypeToken<List<*>>() {}
        val jsonObjectResponseType = object : TypeToken<Map<String, *>>() {}

        fun constructUrl(url: String, params: Map<String, String> = emptyMap()) =
            if (params.isNotEmpty()) encodeQuery(params).let { if (url.contains("?")) "$url&$it" else "$url?$it" }
            else url

        fun postFormData(
            url: String,
            headers: Map<String, String> = emptyMap(),
            formData: Map<String, String>,
        ): Request = Builder(url)
            .setHeaders(headers)
            .setBody(encodeQuery(formData))
            .setMethod(Method.POST)
            .build()

        fun postJson(
            url: String,
            query: Map<String, String> = emptyMap(),
            headers: Map<String, String> = emptyMap(),
            json: Any,
            gson: Gson = Request.gson,
        ): Request = Builder(url)
            .appendQuery(query)
            .setHeaders(headers)
            .appendHeaders("Content-Type" to "application/json")
            .setBody(gson.toJson(json))
            .setMethod(Method.POST)
            .build()

        private fun encodeQuery(params: Map<String, String>) =
            params.map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.joinToString("&")
    }
}
