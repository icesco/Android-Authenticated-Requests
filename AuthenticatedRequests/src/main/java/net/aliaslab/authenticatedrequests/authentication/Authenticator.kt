package net.aliaslab.authenticatedrequests.authentication

import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.networking.URLTransformable

class AuthenticatorException(override val message: String): Exception()

interface Authenticator <ARConfiguration: URLTransformable> {

    fun getConfiguration(): ARConfiguration?

    fun setConfiguration(configuration: ARConfiguration)

    @Throws(AuthenticatorException::class)
    suspend fun validToken(): OAuthToken

}