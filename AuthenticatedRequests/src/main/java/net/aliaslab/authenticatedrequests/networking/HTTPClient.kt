package net.aliaslab.authenticatedrequests.networking

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.aliaslab.authenticatedrequests.BuildConfig
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

object HTTPClient {

    private const val tag = "HTTPClient"
    const val unableToResolveHostErrorCode: Int = -400

    var userAgent: String = "AuthenticatedRequests"

    class HTTPClientException(val code: Int,
                              override val message: String,
                              val originalResponse: String?): Exception()

    private fun isSuccessfulResponse(code: Int): Boolean {
        return code in 200..299
    }

    private fun HttpURLConnection.configure(request: URLRequest) {
        requestMethod = request.method.toString()
        readTimeout = request.readTimeout
        connectTimeout = request.connectionTimeout
        setRequestProperty("User-Agent", request.userAgent ?: userAgent)
        setRequestProperty("Content-Type", request.contentType)
        if (request.authentication != null) {
            setRequestProperty("Authorization", request.authentication)
        }
    }

    private fun logResponse(conn: HttpURLConnection) {
        val responseCode = conn.responseCode
        val responseMessage = conn.responseMessage
        Log.d(tag, "Response Code :: $responseCode, Response Message :: $responseMessage")
    }

    private suspend fun parseStream(stream: InputStream?): String = withContext(Dispatchers.IO) {
        try {
            val bufferedInputStream = BufferedReader(InputStreamReader(stream))
            var inputLine: String?
            val response = StringBuffer()
            bufferedInputStream.use {
                while (it.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
            }
            response.toString()
        } catch (exception: IOException) {
            ""
        }
    }

    @Throws(IOException::class, HTTPClientException::class)
    suspend fun sendRequest(request: URLRequest): String = withContext(Dispatchers.IO) {
        val url = request.url
        Log.d(tag, "Calling ${request.method}: $url")

        val connection = url.openConnection() as? HttpURLConnection
            ?: throw IOException("Failed to open HTTP connection")

        return@withContext connection.use { conn ->
            conn.configure(request)

            logCurl(conn, request)

            if (request.method != HttpMethod.GET && request.body != null) {
                sendPostBody(conn, request.body)
            }

            logResponse(conn)

            val responseCode = conn.responseCode
            if (isSuccessfulResponse(responseCode)) {
                parseStream(conn.inputStream)
            } else {
                val response = parseStream(conn.errorStream)
                throw HTTPClientException(responseCode, "Bad response $responseCode", response)
            }
        }
    }

    @Throws(IOException::class,
            HTTPClientException::class)
    suspend fun sendDownloadRequest(request: URLRequest, destinationFile: File) = withContext(Dispatchers.IO) {
        try {
            val url = request.url
            Log.d("HTTPClient", "Calling download: $url")
            val connection = url.openConnection() as? HttpURLConnection
                ?: throw IOException("Failed to open HTTP connection")

            connection.use { conn ->
                conn.configure(request)

                println("HTTP Request ${request.authentication}")

                logCurl(conn, request)

                if (request.method != HttpMethod.GET && request.body != null) {
                    sendPostBody(conn, request.body)
                }

                logResponse(conn)

                val responseCode = conn.responseCode
                if (isSuccessfulResponse(responseCode)) { // success
                    saveResponseToFile(conn.inputStream, destinationFile)
                    return@use ""
                } else {
                    val response = parseStream(conn.errorStream)
                    Log.d(tag, "Error Message :: $response")
                    throw HTTPClientException(responseCode, "Bad response $responseCode", response)
                }
            }
        } catch (exception: IOException) {
            Log.d(tag, "IOException: ${exception.message}")
            throw HTTPClientException(unableToResolveHostErrorCode, "Unable to resolve host: ${request.url.host}", null)
        }
    }

    @Throws(IOException::class)
    suspend fun sendRequest(
        url: URL,
        method: HttpMethod,
        contentType: String,
        body: ByteArray?,
        connectionTimeout: Int = 15_000,
        readTimeout: Int = 10_000,
        authentication: String? = null
    ): String = sendRequest(
        URLRequest(
            url = url,
            method = method,
            contentType = contentType,
            body = body,
            connectionTimeout = connectionTimeout,
            readTimeout = readTimeout,
            userAgent = userAgent,
            authentication = authentication
        )
    )

    @Throws(IOException::class)
    private suspend fun parseResponse(stream: InputStream?): String = parseStream(stream)

    private suspend fun parseErrorResponse(stream: InputStream?): String = parseStream(stream)

    @Throws(IOException::class)
    private suspend fun saveResponseToFile(stream: InputStream?, file: File) = withContext(Dispatchers.IO) {
        if (stream == null) {
            return@withContext
        }

        Log.i(tag, "Saving http response to file.")

        FileOutputStream(file).use { outputStream ->
            var length: Int
            val bytes = ByteArray(1024)
            // copy data from input stream to output stream
            while (stream.read(bytes).also { length = it } != -1) {
                outputStream.write(bytes, 0, length)
            }
        }
    }

    @Throws(IOException::class)
    private fun sendPostBody(connection: HttpURLConnection,
                             body: ByteArray?) {
        connection.doOutput = true

        if (body != null) {
            val os = connection.outputStream
            os.write(body)
            os.flush()
            os.close()
        }
    }

    private fun logCurl(connection: HttpURLConnection,
                        request: URLRequest) {

        if (BuildConfig.DEBUG) {
            val cURl = connection.asCurl(
                if (request.method != HttpMethod.GET) request.body.toString() else null
            )
            Log.d(tag, cURl)
        }
    }

}

private suspend fun <T> HttpURLConnection.use(block: suspend (HttpURLConnection) -> T): T {
    try {
        return block(this)
    } finally {
        this.disconnect()
    }
}