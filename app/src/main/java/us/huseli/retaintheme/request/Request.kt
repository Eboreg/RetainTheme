@file:Suppress("unused")

package us.huseli.retaintheme.request

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
    val uri: Uri,
    private val headers: Map<String, String> = emptyMap(),
    val method: Method = Method.GET,
    private val body: String? = null,
    private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
    private val readTimeout: Int = DEFAULT_READ_TIMEOUT,
    private val suppressLogs: Boolean = false,
) : ILogger {
    constructor(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        method: Method = Method.GET,
        body: String? = null,
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT,
        suppressLogs: Boolean = false,
    ) : this(
        uri = constructUri(url, params),
        headers = headers,
        method = method,
        body = body,
        connectTimeout = connectTimeout,
        readTimeout = readTimeout,
        suppressLogs = suppressLogs,
    )

    enum class Method(val value: String) { GET("GET"), POST("POST") }

    private var requestStart: Long? = null

    @Deprecated("Use uri instead.")
    val url = uri.toString()

    var contentRange: HttpContentRange? = null
        private set
    var contentLength: Int? = null
        private set
    var responseCode: Int? = null
        private set

    override fun shouldLog(log: LogInstance): Boolean {
        if (suppressLogs) return false
        return super.shouldLog(log)
    }

    private fun connect(): HttpURLConnection {
        requestStart = System.currentTimeMillis()
        log("START ${method.value} $uri", priority = Log.DEBUG)
        if (body != null) log("BODY $body", priority = Log.DEBUG)

        val conn = URL(uri.toString()).openConnection() as HttpURLConnection

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
                throw RetainConnectionError(uri = uri, method = method, cause = e)
            }
        }

        val responseCode = try {
            conn.responseCode
        } catch (e: Throwable) {
            throw RetainConnectionError(uri = uri, method = method, cause = e)
        }

        this@Request.responseCode = responseCode
        if (responseCode >= 400) throw RetainHttpError(
            uri = uri,
            method = method,
            statusCode = responseCode,
            message = conn.responseMessage,
        )

        contentRange = conn.getHeaderField("Content-Range")?.parseContentRange()
        contentLength = contentRange?.size ?: conn.getHeaderField("Content-Length")?.toInt()

        return conn
    }

    fun finish(receivedBytes: Int? = null) {
        requestStart?.also { start ->
            val elapsed = (System.currentTimeMillis() - start).toDouble() / 1000
            var message = "FINISH ${method.value} $uri: ${elapsed}s"

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
        logError("getObjectOrNull(): $method $uri", e)
        null
    }

    @WorkerThread
    fun <T> getObjectOrNull(typeOfT: Type, gson: Gson = Request.gson): T? = try {
        getObject<T>(typeOfT, gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $uri", e)
        null
    }

    @WorkerThread
    fun getString(): String =
        getInputStream().use { it.bufferedReader().readText() }.also { finish(it.length) }

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT = 4_050
        const val DEFAULT_READ_TIMEOUT = 10_000

        val gson: Gson = GsonBuilder().create()
        val jsonObjectResponseType = object : TypeToken<Map<String, *>>() {}
        val jsonArrayResponseType = object : TypeToken<List<*>>() {}

        fun constructUri(url: String, params: Map<String, String> = emptyMap()): Uri {
            val path = url.substringBefore('?')
            val query = url.substringAfter('?', "")

            return Uri.Builder().encodedPath(path).encodedQuery(query).apply {
                for ((key, value) in params) appendQueryParameter(key, value)
            }.build()
        }

        @Deprecated("Use constructUri instead.")
        fun constructUrl(url: String, params: Map<String, String> = emptyMap()) =
            if (params.isNotEmpty()) encodeQuery(params).let { if (url.contains("?")) "$url&$it" else "$url?$it" }
            else url

        @Deprecated("Use the one with Uri parameter instead.")
        fun postJson(
            url: String,
            params: Map<String, String> = emptyMap(),
            headers: Map<String, String> = emptyMap(),
            json: Any,
            gson: Gson = Request.gson,
        ) = Request(
            url = url,
            params = params,
            headers = headers.plus("Content-Type" to "application/json"),
            body = gson.toJson(json),
            method = Method.POST,
        )

        fun postJson(
            uri: Uri,
            headers: Map<String, String> = emptyMap(),
            json: Any,
            gson: Gson = Request.gson,
        ) = Request(
            uri = uri,
            headers = headers.plus("Content-Type" to "application/json"),
            body = gson.toJson(json),
            method = Method.POST,
        )

        @Deprecated("Use the one with Uri parameter instead.")
        fun postFormData(url: String, headers: Map<String, String> = emptyMap(), formData: Map<String, String>) =
            Request(
                url = url,
                headers = headers.plus("Content-Type" to "application/x-www-form-urlencoded"),
                body = encodeQuery(formData),
                method = Method.POST,
            )

        fun postFormData(uri: Uri, headers: Map<String, String> = emptyMap(), formData: Map<String, String>) =
            Request(
                uri = uri,
                headers = headers.plus("Content-Type" to "application/x-www-form-urlencoded"),
                body = encodeQuery(formData),
                method = Method.POST,
            )

        private fun encodeQuery(params: Map<String, String>) =
            params.map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.joinToString("&")
    }
}
