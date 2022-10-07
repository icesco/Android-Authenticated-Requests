package net.aliaslab.authenticatedrequests.model

import java.util.*

public data class OAuthToken(val date: Date = Date(),
                             val access_token: String,
                             val refresh_token: String?,
                             val expires_in: Int,
                             val token_type: String) {

    /// checks if token is still valid or has expired
    fun isValid(): Boolean {

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, expires_in)

        return calendar.time > Date()
    }

}
