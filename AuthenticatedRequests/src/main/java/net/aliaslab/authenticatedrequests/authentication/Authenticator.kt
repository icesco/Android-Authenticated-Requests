package net.aliaslab.authenticatedrequests.authentication

import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.networking.URLTransformable

public class AuthenticatorException(override val message: String): Exception()

public interface Authenticator <ARConfiguration: URLTransformable> {

    fun getConfiguration(): ARConfiguration?

    fun setConfiguration(configuration: ARConfiguration)

    @Throws(AuthenticatorException::class)
    suspend fun validToken(): OAuthToken

}