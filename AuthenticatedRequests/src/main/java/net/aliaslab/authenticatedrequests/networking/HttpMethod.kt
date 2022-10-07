package net.aliaslab.authenticatedrequests.networking

public enum class HttpMethod {

    GET, POST;

    override fun toString(): String {
        return when (this) {
            GET -> "GET"
            POST -> "POST"
        }
    }
}