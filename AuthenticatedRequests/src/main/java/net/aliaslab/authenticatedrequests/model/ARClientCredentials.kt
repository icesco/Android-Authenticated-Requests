package net.aliaslab.authenticatedrequests.model

import net.aliaslab.authenticatedrequests.URLQueryable
import java.net.URLEncoder

public data class ARClientCredentials(val clientID: String,
                                      val clientSecret: String,
                                      val scope: Set<String>): URLQueryable {

    override fun httpBody(): ByteArray {
        val loginMap = mapOf(
            "grant_type" to "client_credentials",
            "client_id" to clientID,
            "client_secret" to clientSecret
        )

        val loginData = loginMap
            .map { (k, v) ->
                "${URLEncoder.encode(k, "utf-8")}=${
                    URLEncoder.encode(v, "utf-8")
                }"
            }.reduce { p1, p2 -> "$p1&$p2" }

        return loginData.toByteArray()
    }

    override fun httpQuery(): String? {
        return null
    }

}
