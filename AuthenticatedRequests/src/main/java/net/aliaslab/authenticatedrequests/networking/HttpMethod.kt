package net.aliaslab.authenticatedrequests.networking

enum class HttpMethod {

    GET, POST, PUT, DELETE, PATCH;

    override fun toString(): String {
        return when (this) {
            GET -> "GET"
            POST -> "POST"
            PUT -> "PUT"
            PATCH -> "PATCH"
            DELETE -> "DELETE"
        }
    }
}