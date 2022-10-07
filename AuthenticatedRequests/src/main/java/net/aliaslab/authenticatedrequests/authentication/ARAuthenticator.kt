package net.aliaslab.authenticatedrequests.authentication

import kotlinx.coroutines.*
import net.aliaslab.authenticatedrequests.Authenticator
import net.aliaslab.authenticatedrequests.AuthenticatorException
import net.aliaslab.authenticatedrequests.model.ARClientCredentials
import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.model.Result
import net.aliaslab.authenticatedrequests.request
import net.aliaslab.authenticatedrequests.requests.AuthenticationEndpoint
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
Atomic object that manages to refresh the OAuthToken when needed.
It must be configured with a `ClientCredentials` in order to correctly fetch and save the `OAuthToken`s.
 */
class ARAuthenticator(var authenticationEndpoint: AuthenticationEndpoint):
    Authenticator<ARClientCredentials> {

    private var currentToken = OAuthToken(access_token = "", refresh_token = null, expires_in = 0, token_type = "bearer", date = Date())
    private var credentials: ARClientCredentials? = null

    /**
    The task that is responsible for the fetch of a new access token.
     */
    private var currentTokenJob: Deferred<OAuthToken>? = null

    override fun getConfiguration(): ARClientCredentials? {
        return credentials
    }

    override fun setConfiguration(configuration: ARClientCredentials) {
        credentials = configuration
    }

    @Throws(AuthenticatorException::class)
    override suspend fun validToken(): OAuthToken {

        if (currentToken.isValid()) {
            return currentToken
        }

        if (currentTokenJob?.isActive == true) {
            return currentTokenJob!!.await()
        } else {
            val currentCredentials = credentials ?: throw AuthenticatorException("Missing credentials")

            currentTokenJob = CoroutineScope(coroutineContext).async(Dispatchers.IO) {
                val result = authenticationEndpoint.request<ARClientCredentials, OAuthToken>(currentCredentials)
                val token = result.unwrap()

                if (token != null) {
                    return@async token
                } else {
                    throw AuthenticatorException("Failed to fetch a new token.")
                }
            }

            return currentTokenJob!!.await()
        }


    }

}