package net.aliaslab.authenticatedrequests.networking

import android.util.Log
import net.aliaslab.authenticatedrequests.BuildConfig
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object HTTPClient {

    var userAgent: String = "AuthenticatedRequests"

    @Throws(IOException::class)
    fun sendRequest(request: URLRequest): String {
        val url = request.url
        Log.d("HTTPClient", "Calling ${request.method.toString()}: $url")
        val connection = url.openConnection() as? HttpURLConnection
        connection?.requestMethod = request.method.toString()
        connection?.setRequestProperty("User-Agent", request.userAgent ?: userAgent)
        connection?.setRequestProperty("Content-Type", request.contentType)

        println("HTTP Request ${request.authentication}")
        if (request.authentication != null) {
            connection?.setRequestProperty("Authorization", request.authentication)
        }

        if (connection != null) {
            logCurl(connection, request)
        }

        if (request.method == HttpMethod.POST && request.body != null) {
            sendPostBody(connection, request.body)
        }

        val responseCode = connection?.responseCode
        println("Response Code :: $responseCode")
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            val response = parseResponse(connection.inputStream)
            connection.disconnect()
            return response
        } else {
            print(parseResponse(connection?.inputStream))
            connection?.disconnect()
            throw java.lang.Exception("Bad response $responseCode")
        }
    }

    @Throws(IOException::class)
    fun sendDownloadRequest(request: URLRequest, destinationFile: File) {
        val url = request.url
        Log.d("HTTPClient", "Calling download: $url")
        val connection = url.openConnection() as? HttpURLConnection
        connection?.requestMethod = request.method.toString()
        connection?.setRequestProperty("User-Agent", request.userAgent ?: userAgent)
        connection?.setRequestProperty("Content-Type", request.contentType)

        println("HTTP Request ${request.authentication}")
        if (request.authentication != null) {
            connection?.setRequestProperty("Authorization", request.authentication)
        }

        if (connection != null) {
            logCurl(connection, request)
        }

        if (request.method == HttpMethod.POST && request.body != null) {
            sendPostBody(connection, request.body)
        }

        val responseCode = connection?.responseCode
        println("Response Code :: $responseCode")
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            saveResponseToFile(connection.inputStream, destinationFile)
            connection.disconnect()
        } else {
            print(parseResponse(connection?.inputStream))
            connection?.disconnect()
            throw java.lang.Exception("Bad response $responseCode")
        }
    }

    @Throws(IOException::class)
    fun sendRequest(url: URL, method: HttpMethod, contentType: String, body: ByteArray?): String {
        val connection = url.openConnection() as? HttpURLConnection
        connection?.requestMethod = method.toString()
        connection?.setRequestProperty("User-Agent", userAgent)
        connection?.setRequestProperty("Content-Type", contentType)

        if (method == HttpMethod.POST && body != null) {
            sendPostBody(connection, body)
        }

        val responseCode = connection?.responseCode
        println("Response Code :: $responseCode")
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            val response = parseResponse(connection.inputStream)
            connection.disconnect()
            return response
        } else {
            connection?.disconnect()
            throw java.lang.Exception("Bad response $responseCode")
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

        return response.toString()
    }

    @Throws(IOException::class)
    private fun saveResponseToFile(stream: InputStream?, file: File) {
        if (stream == null) {
            return
        }

        Log.i("HTTPClient", "Saving http response to file.")

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
                if (request.method == HttpMethod.POST) request.body.toString() else null
            )
            Log.d("HTTPClient", cURl)
        }
    }

}