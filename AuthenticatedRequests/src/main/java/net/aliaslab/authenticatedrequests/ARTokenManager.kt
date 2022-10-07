package net.aliaslab.authenticatedrequests

import net.aliaslab.authenticatedrequests.model.KeychainKey
import net.aliaslab.authenticatedrequests.model.OAuthToken
import java.util.*

class ARTokenManager(val tokenStore: TokenStore?,
                     private var prefix: String) {

    fun saveToken(token: OAuthToken): Boolean {

        if (tokenStore == null) {
            return false
        }

        val success = tokenStore.set(token, KeychainKey.CLIENT_TOKEN.prefixed(prefix))

        val dateSuccess = tokenStore.set(Date().time, KeychainKey.CREATION_DATE.prefixed(prefix))

        return dateSuccess && success
    }

    fun currentToken(): OAuthToken? {
        return tokenStore?.parcelable(KeychainKey.CLIENT_TOKEN.prefixed(prefix))
    }

    fun tokenDate(): Date? {
        return tokenStore?.serializable(KeychainKey.CREATION_DATE.prefixed(prefix))
    }

}