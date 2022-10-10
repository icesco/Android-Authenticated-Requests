package net.aliaslab.authenticatedrequests.networking

import net.aliaslab.authenticatedrequests.networking.URLRequest
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HTTPClient {

    var userAgent: String = "AuthenticatedRequests"

    @Throws(IOException::class)
    fun sendRequest(request: URLRequest): String {
        val url = request.url
        val connection = url.openConnection() as? HttpURLConnection
        connection?.requestMethod = request.method.toString()
        connection?.setRequestProperty("User-Agent", request.userAgent ?: userAgent)
        connection?.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        if (request.authentication != null) {
            connection?.setRequestProperty("Authorization", request.authentication)
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
    fun sendRequest(url: URL, method: HttpMethod, body: ByteArray?): String {
        val connection = url.openConnection() as? HttpURLConnection
        connection?.requestMethod = method.toString()
        connection?.setRequestProperty("User-Agent", userAgent)
        connection?.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

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
        while (bufferedInputStream.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        bufferedInputStream.close()

        return response.toString()
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


}