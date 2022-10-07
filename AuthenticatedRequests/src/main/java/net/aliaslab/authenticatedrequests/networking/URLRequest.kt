package net.aliaslab.authenticatedrequests.networking

import java.net.URL

public data class URLRequest(val url: URL,
                             val body: ByteArray?,
                             val userAgent: String?,
                             val method: HttpMethod = HttpMethod.GET,
                             var authentication: String?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as URLRequest

        if (url != other.url) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }
}

public interface URLTransformable {

    fun httpBody(): ByteArray?

    fun httpQuery(): String?

}
