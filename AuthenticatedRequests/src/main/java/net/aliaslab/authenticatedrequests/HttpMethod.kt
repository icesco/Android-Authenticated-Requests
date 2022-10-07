package net.aliaslab.authenticatedrequests

public enum class HttpMethod {

    GET, POST;

    override fun toString(): String {
        return when (this) {
            GET -> "GET"
            POST -> "POST"
        }
    }
}