package net.aliaslab.authenticatedrequests.tokenpersistence

import android.content.SharedPreferences
import net.aliaslab.authenticatedrequests.model.KeychainKey
import net.aliaslab.authenticatedrequests.model.OAuthToken
import java.util.*

/**
This class is responsible to maintain all the token for every user.
Everytime the user changes, it's required to set the keyPrefix to the corresponding username or client ID.
 */
class ARTokenManager(
    private val tokenStore: SharedPreferences?,
    private var prefix: String) {

    fun saveToken(token: OAuthToken): Boolean {

        if (tokenStore == null) {
            return false
        }

        val success = tokenStore.set(token, KeychainKey.CLIENT_TOKEN.prefixed(prefix))

        val dateSuccess = tokenStore.set(Date().time, KeychainKey.CREATION_DATE.prefixed(prefix))

        return dateSuccess && success
    }

    fun setPrefix(prefix: String) {
        this.prefix = prefix
    }

    fun currentToken(): OAuthToken? {
        val currentToken: OAuthToken? = tokenStore?.getToken(KeychainKey.CLIENT_TOKEN.prefixed(prefix))
        val date = tokenDate()
        if (date != null && currentToken != null) {
            currentToken.date = date
        }
        return currentToken
    }

    private fun tokenDate(): Date? {
        return tokenStore?.getDate(KeychainKey.CREATION_DATE.prefixed(prefix))
    }

    fun removeToken(): Boolean {
        var success: Boolean = true

        if (tokenStore == null) {
            return false
        }

        if (!tokenStore.delete(KeychainKey.CLIENT_TOKEN.prefixed(prefix))) {
            success = false
        }

        if (!tokenStore.delete(KeychainKey.CREATION_DATE.prefixed(prefix))) {
            success = false
        }

        return success
    }

}