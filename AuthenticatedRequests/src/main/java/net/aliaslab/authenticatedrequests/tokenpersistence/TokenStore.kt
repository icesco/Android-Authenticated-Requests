package net.aliaslab.authenticatedrequests.tokenpersistence

import net.aliaslab.authenticatedrequests.model.OAuthToken
import java.util.Date

public interface TokenStore {

    fun <Input> set(input: Input, forKey: String): Boolean

    fun getToken(forKey: String): OAuthToken?
    fun getDate(forKey: String): Date?

    fun delete(key: String): Boolean
}
