package net.aliaslab.authenticatedrequests.tokenpersistence

import net.aliaslab.authenticatedrequests.model.KeychainKey
import net.aliaslab.authenticatedrequests.model.OAuthToken
import java.util.*

public class ARTokenManager(
    private val tokenStore: TokenStore?,
    private var prefix: String) {

    public fun saveToken(token: OAuthToken): Boolean {

        if (tokenStore == null) {
            return false
        }

        val success = tokenStore.set(token, KeychainKey.CLIENT_TOKEN.prefixed(prefix))

        val dateSuccess = tokenStore.set(Date().time, KeychainKey.CREATION_DATE.prefixed(prefix))

        return dateSuccess && success
    }

    public fun setPrefix(prefix: String) {
        this.prefix = prefix
    }

    public fun currentToken(): OAuthToken? {
        return tokenStore?.parcelable(KeychainKey.CLIENT_TOKEN.prefixed(prefix))
    }

    public fun tokenDate(): Date? {
        return tokenStore?.serializable(KeychainKey.CREATION_DATE.prefixed(prefix))
    }

    public fun removeToken(): Boolean {
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