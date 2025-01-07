@file:Suppress("unused")

package us.huseli.retaintheme.request

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import us.huseli.retaintheme.RetainConnectionError
import us.huseli.retaintheme.RetainHttpError
import us.huseli.retaintheme.utils.AbstractScopeHolder
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
    private val headers: Map<String, String> = emptyMap(),
    params: Map<String, String> = emptyMap(),
    val method: Method = Method.GET,
    private val body: String? = null,
    private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
    private val readTimeout: Int = DEFAULT_READ_TIMEOUT,
    private val suppressLogs: Boolean = false,
) : ILogger, AbstractScopeHolder() {
    enum class Method(val value: String) { GET("GET"), POST("POST") }

    private var requestStart: Long? = null

    val url = constructUrl(url, params)

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

    fun finish(receivedBytes: Int? = null) {
        requestStart?.also { start ->
            val elapsed = (System.currentTimeMillis() - start).toDouble() / 1000
            var message = "FINISH ${method.value} $url: ${elapsed}s"

            if (receivedBytes != null) {
                val kbps = ((receivedBytes / elapsed) / 1024).roundToInt()
                message += ", ${receivedBytes}B ($kbps KB/s)"
            }

            log("Request", message)
        }
    }

    suspend fun getBitmap(): Bitmap? = onIOThread {
        getInputStream().use { BitmapFactory.decodeStream(it) }.also { finish() }
    }

    suspend fun getInputStream(): InputStream = onIOThread {
        val conn = connect()
        val isGzipped = conn.headerFields["Content-Encoding"]?.contains("gzip") == true

        if (isGzipped) GZIPInputStream(conn.inputStream) else conn.inputStream
    }

    suspend fun getJsonArray(gson: Gson = Request.gson): List<*> = onIOThread {
        getInputStream().use {
            gson.fromJson(it.bufferedReader(), jsonArrayResponseType) ?: emptyList<Any>()
        }.also { finish() }
    }

    suspend fun getJsonObject(gson: Gson = Request.gson): Map<String, *> = onIOThread {
        getInputStream().use {
            gson.fromJson(it.bufferedReader(), jsonObjectResponseType) ?: emptyMap<String, Any>()
        }.also { finish() }
    }

    suspend inline fun <reified T> getObject(gson: Gson = Request.gson): T = onIOThread {
        getInputStream().use { gson.fromJson(it.bufferedReader(), T::class.java) }.also { finish() }
    }

    suspend fun <T> getObject(typeOfT: Type, gson: Gson = Request.gson): T = onIOThread {
        getInputStream().use { gson.fromJson<T>(it.bufferedReader(), typeOfT) }.also { finish() }
    }

    suspend inline fun <reified T> getObjectOrNull(gson: Gson = Request.gson): T? = try {
        getObject<T>(gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    suspend fun <T> getObjectOrNull(typeOfT: Type, gson: Gson = Request.gson): T? = try {
        getObject<T>(typeOfT, gson)
    } catch (e: Throwable) {
        logError("getObjectOrNull(): $method $url", e)
        null
    }

    suspend fun getString(): String =
        onIOThread { getInputStream().use { it.bufferedReader().readText() }.also { finish(it.length) } }

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT = 4_050
        const val DEFAULT_READ_TIMEOUT = 10_000

        val gson: Gson = GsonBuilder().create()
        val jsonObjectResponseType = object : TypeToken<Map<String, *>>() {}
        val jsonArrayResponseType = object : TypeToken<List<*>>() {}

        fun constructUrl(url: String, params: Map<String, String> = emptyMap()) =
            if (params.isNotEmpty()) encodeQuery(params).let { if (url.contains("?")) "$url&$it" else "$url?$it" }
            else url

        fun postJson(
            url: String,
            params: Map<String, String> = emptyMap(),
            headers: Map<String, String> = emptyMap(),
            json: Any,
            gson: Gson = Request.gson,
        ) =
            Request(
                url = url,
                params = params,
                headers = headers.plus("Content-Type" to "application/json"),
                body = gson.toJson(json),
                method = Method.POST,
            )

        fun postFormData(url: String, headers: Map<String, String> = emptyMap(), formData: Map<String, String>) =
            Request(
                url = url,
                headers = headers.plus("Content-Type" to "application/x-www-form-urlencoded"),
                body = encodeQuery(formData),
                method = Method.POST,
            )

        private fun encodeQuery(params: Map<String, String>) =
            params.map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }.joinToString("&")
    }
}
