package net.aliaslab.authenticatedrequests.networking

import java.net.HttpURLConnection

fun HttpURLConnection.asCurl(body: String?): String {

    val stringBuilder = StringBuilder()
    stringBuilder.append("curl -v -X $requestMethod \\")
    stringBuilder.append("\n")

    requestProperties.forEach { (k, v) ->
        val value = v.concat()
        stringBuilder.append("-H \"$k: $value\" \\\n")
    }

    if (body != null) {
        stringBuilder.append("-d \"$body\" \\\n")
    }

    stringBuilder.append("$url")

    return stringBuilder.toString()
}

fun List<String>.concat() = this.joinToString("") { it }