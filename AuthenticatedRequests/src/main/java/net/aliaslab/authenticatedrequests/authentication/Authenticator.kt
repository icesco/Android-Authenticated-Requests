package net.aliaslab.authenticatedrequests

import net.aliaslab.authenticatedrequests.model.OAuthToken

public class AuthenticatorException(): Exception()

public interface Authenticator <ARConfiguration: URLQueryable> {

    fun getConfiguration(): ARConfiguration?

    fun setConfiguration(configuration: ARConfiguration)

    @Throws(AuthenticatorException::class)
    suspend fun validToken(): OAuthToken

}