@file:Suppress("unused")

package us.huseli.retaintheme.request

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import us.huseli.retaintheme.RetainConnectionError
import us.huseli.retaintheme.RetainHttpError
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
    enum class Method(val value: String) { GET("GET"), POST("POST") }

    val url = constructUrl(url, query)

    var contentLength: Int? = null
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
    fun getBitmap(): Bitmap? = getInputStream().use { BitmapFactory.decodeStream(it) }.also { finish() }

    @WorkerThread
    fun getInputStream(): InputStream {
        val conn = connect()
        val isGzipped = conn.headerFields["Content-Encoding"]?.contains("gzip") == true

        return if (isGzipped) GZIPInputStream(conn.inputStream) else conn.inputStream
    }

    @WorkerThread
    fun getJsonArray(gson: Gson = Request.gson): List<*> = getInputStream().use {
        gson.fromJson(it.bufferedReader(), jsonArrayResponseType) ?: emptyList<Any>()
    }.also { finish() }

    @WorkerThread
    fun getJsonObject(gson: Gson = Request.gson): Map<String, *> = getInputStream().use {
        gson.fromJson(it.bufferedReader(), jsonObjectResponseType) ?: emptyMap<String, Any>()
    }.also { finish() }

    @WorkerThread
    inline fun <reified T> getObject(gson: Gson = Request.gson): T =
        getInputStream().use { gson.fromJson(it.bufferedReader(), T::class.java) }.also { finish() }

    @WorkerThread
    fun <T> getObject(typeOfT: Type, gson: Gson = Request.gson): T =
        getInputStream().use { gson.fromJson<T>(it.bufferedReader(), typeOfT) }.also { finish() }

    @WorkerThread
    inline fun <reified T> getObjectOrNull(gson: Gson = Request.gson): T? = try {
        getObject<T>(gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    @WorkerThread
    fun <T> getObjectOrNull(typeOfT: Type, gson: Gson = Request.gson): T? = try {
        getObject(typeOfT, gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    @WorkerThread
    fun getString(): String =
        getInputStream().use { it.bufferedReader().readText() }.also { finish(it.length) }

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
        contentLength = contentRange?.size ?: conn.getHeaderField("Content-Length")?.toInt()

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

        fun appendHeaders(value: Map<String, String>): Builder = apply { _headers.putAll(value) }

        fun appendQuery(value: Map<String, String>): Builder = apply {
            _query.putAll(value)
        }

        fun build(): Request = Request(
            url = url,
            query = _query,
            headers = _headers,
            method = method,
            body = body,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
        )

        fun setBody(value: String?): Builder = apply { body = value }

        fun setHeaders(value: Map<String, String>): Builder = apply {
            _headers.clear()
            _headers.putAll(value)
        }

        fun setMethod(value: Method): Builder = apply { method = value }

        fun setQuery(value: Map<String, String>): Builder = apply {
            _query.clear()
            _query.putAll(value)
        }

        fun setUrl(value: String, clearQuery: Boolean = false): Builder = apply {
            val query = value.substringAfter('?', "").split('&').mapNotNull { param ->
                param.split('=', limit = 2).takeIf { it.size == 2 }?.let { (key, value) -> key to value }
            }.toMap()

            this.url = value.substringBefore('?')
            if (clearQuery) setQuery(query)
            else appendQuery(query)
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
            .setQuery(query)
            .setHeaders(headers.plus("Content-Type" to "application/json"))
            .setBody(gson.toJson(json))
            .setMethod(Method.POST)
            .build()

        private fun encodeQuery(params: Map<String, String>) =
            params.map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.joinToString("&")
    }
}
