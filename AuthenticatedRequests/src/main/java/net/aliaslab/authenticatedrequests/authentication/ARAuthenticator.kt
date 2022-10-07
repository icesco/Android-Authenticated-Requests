package net.aliaslab.authenticatedrequests.authentication

import net.aliaslab.authenticatedrequests.Authenticator
import net.aliaslab.authenticatedrequests.model.ARClientCredentials
import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.requests.AuthenticationEndpoint
import java.util.*

class ARAuthenticator(var authenticationEndpoint: AuthenticationEndpoint):
    Authenticator<ARClientCredentials> {

    private var currentToken = OAuthToken(access_token = "", refresh_token = null, expires_in = 0, token_type = "bearer", date = Date())
    private var credentials: ARClientCredentials? = null

    override fun getConfiguration(): ARClientCredentials? {
        return credentials
    }

    override fun setConfiguration(configuration: ARClientCredentials) {
        credentials = configuration
    }

    override suspend fun validToken(): OAuthToken {
        TODO("Not yet implemented")
    }

}