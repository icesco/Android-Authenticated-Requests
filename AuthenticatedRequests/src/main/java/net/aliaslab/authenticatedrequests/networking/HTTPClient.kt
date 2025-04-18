package net.aliaslab.authenticatedrequests.networking

import android.util.Log
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

    @Throws(IOException::class,
            HTTPClientException::class)
    fun sendRequest(request: URLRequest): String {
        try {
            val url = request.url
            Log.d("HTTPClient", "Calling ${request.method}: $url")
            val connection = url.openConnection() as? HttpURLConnection
            connection?.requestMethod = request.method.toString()
            connection?.readTimeout = request.readTimeout
            connection?.connectTimeout = request.connectionTimeout
            connection?.setRequestProperty("User-Agent", request.userAgent ?: userAgent)
            connection?.setRequestProperty("Content-Type", request.contentType)

            // println("HTTP Request ${request.authentication}")
            if (request.authentication != null) {
                connection?.setRequestProperty("Authorization", request.authentication)
            }

            if (connection != null) {
                logCurl(connection, request)
            }

            if (request.method != HttpMethod.GET && request.body != null) {
                sendPostBody(connection, request.body)
            }

            val responseCode = connection?.responseCode
            Log.d(tag, "Response from $url")
            Log.d(tag, "Response Code :: $responseCode")
            Log.d(tag, "Response Message :: ${connection?.responseMessage}")
            if (responseCode in 200..299) { // success
                val response = parseResponse(connection?.inputStream)
                connection?.disconnect()
                return response
            } else {
                val response = parseErrorResponse(connection?.errorStream)
                Log.d(tag, "Error Message :: $response")
                connection?.disconnect()
                throw HTTPClientException(responseCode ?: 0, "Bad response $responseCode", originalResponse = response)
            }
        } catch (exception: IOException) {
            Log.d(tag, "IOException: ${exception.message}")
            throw HTTPClientException(unableToResolveHostErrorCode, "Unable to resolve host: ${request.url.host}", null)
        }
    }

    @Throws(IOException::class,
            HTTPClientException::class)
    fun sendDownloadRequest(request: URLRequest, destinationFile: File) {
        try {
            val url = request.url
            Log.d("HTTPClient", "Calling download: $url")
            val connection = url.openConnection() as? HttpURLConnection
            connection?.requestMethod = request.method.toString()
            connection?.connectTimeout = request.connectionTimeout
            connection?.readTimeout = request.readTimeout
            connection?.setRequestProperty("User-Agent", request.userAgent ?: userAgent)
            connection?.setRequestProperty("Content-Type", request.contentType)

            println("HTTP Request ${request.authentication}")
            if (request.authentication != null) {
                connection?.setRequestProperty("Authorization", request.authentication)
            }

            if (connection != null) {
                logCurl(connection, request)
            }

            if (request.method != HttpMethod.GET && request.body != null) {
                sendPostBody(connection, request.body)
            }

            val responseCode = connection?.responseCode
            Log.d(tag, "Response Code :: $responseCode")
            Log.d(tag, "Response Message: ${connection?.responseMessage}")
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                saveResponseToFile(connection.inputStream, destinationFile)
                connection.disconnect()
            } else {
                val response = parseErrorResponse(connection?.errorStream)
                Log.d(tag, "Error Message :: $response")
                connection?.disconnect()
                throw HTTPClientException(responseCode ?: 0, "Bad response $responseCode", response)
            }
        } catch (exception: IOException) {
            Log.d(tag, "IOException: ${exception.message}")
            throw HTTPClientException(unableToResolveHostErrorCode, "Unable to resolve host: ${request.url.host}", null)
        }
    }

    @Throws(IOException::class)
    fun sendRequest(url: URL,
                    method: HttpMethod,
                    contentType: String,
                    body: ByteArray?,
                    connectionTimeout: Int = 15_000,
                    readTimeout: Int = 10_000): String {
        try {
            val connection = url.openConnection() as? HttpURLConnection
            connection?.requestMethod = method.toString()
            connection?.readTimeout = readTimeout
            connection?.connectTimeout = connectionTimeout
            connection?.setRequestProperty("User-Agent", userAgent)
            connection?.setRequestProperty("Content-Type", contentType)

            if (method != HttpMethod.GET && body != null) {
                sendPostBody(connection, body)
            }

            val responseCode = connection?.responseCode
            Log.d(tag, "Response Code :: $responseCode")
            Log.d(tag, "Response Message :: ${connection?.responseMessage}")
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                val response = parseResponse(connection.inputStream)
                connection.disconnect()
                return response
            } else {
                val response = parseErrorResponse(connection?.errorStream)
                Log.d(tag, "Error Message :: $response")
                connection?.disconnect()
                throw HTTPClientException(responseCode ?: 0, "Bad response $responseCode", response)
            }
        } catch (exception: IOException) {
            Log.d(tag, "IOException: ${exception.message}")
            throw HTTPClientException(unableToResolveHostErrorCode, "Unable to resolve host: ${url.host}", null)
        }
    }

    @Throws(IOException::class)
    private fun parseResponse(stream: InputStream?): String {
        val bufferedInputStream = BufferedReader(InputStreamReader(stream))
        var inputLine: String?
        val response = StringBuffer()
        bufferedInputStream.use {
            while (it.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
        }

        Log.d(tag,"Parsed response, it's ${response.count()} characters long.")

        return response.toString()
    }

    private fun parseErrorResponse(stream: InputStream?): String {
        return try {
            val bufferedInputStream = BufferedReader(InputStreamReader(stream))
            var inputLine: String?
            val response = StringBuffer()
            bufferedInputStream.use {
                while (it.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
            }

            response.toString()
        } catch(exception: IOException) {
            ""
        }
    }

    @Throws(IOException::class)
    private fun saveResponseToFile(stream: InputStream?, file: File) {
        if (stream == null) {
            return
        }

        Log.i(tag, "Saving http response to file.")

        val outputFile = FileOutputStream(file).use { outputStream ->
            var length: Int
            val bytes = ByteArray(1024)
            // copy data from input stream to output stream
            while (stream.read(bytes).also { length = it } != -1) {
                outputStream.write(bytes, 0, length)
            }

            outputStream.close()
            stream.close()
        }
    }

    @Throws(IOException::class)
    private fun sendPostBody(connection: HttpURLConnection?,
                             body: ByteArray?) {
        connection?.doOutput = true

        if (body != null) {
            val os = connection?.outputStream
            os?.write(body)
            os?.flush()
            os?.close()
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