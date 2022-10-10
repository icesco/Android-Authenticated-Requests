package net.aliaslab.authenticatedrequests.authentication

import kotlinx.coroutines.*
import net.aliaslab.authenticatedrequests.model.ARClientCredentials
import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.model.Result
import net.aliaslab.authenticatedrequests.request
import net.aliaslab.authenticatedrequests.requests.AuthenticationEndpoint
import net.aliaslab.authenticatedrequests.tokenpersistence.ARTokenManager
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
Atomic object that manages to refresh the OAuthToken when needed.
It must be configured with a `ClientCredentials` in order to correctly fetch and save the `OAuthToken`s.
 */
class ARAuthenticator(var authenticationEndpoint: AuthenticationEndpoint,
                      val tokenManager: ARTokenManager = ARTokenManager(null, "")):
    Authenticator<ARClientCredentials> {

    private var currentToken = OAuthToken.invalidToken
    private var credentials: ARClientCredentials? = null

    init {
        currentToken = tokenManager.currentToken() ?: OAuthToken.invalidToken
    }

    /**
    The task that is responsible for the fetch of a new access token.
     */
    private var currentTokenJob: Deferred<OAuthToken>? = null

    override fun getConfiguration(): ARClientCredentials? {
        return credentials
    }

    override fun setConfiguration(configuration: ARClientCredentials) {
        credentials = configuration
        tokenManager.setPrefix(configuration.clientID)
        currentToken = tokenManager.currentToken() ?: OAuthToken.invalidToken
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
                    currentToken = token
                    tokenManager.saveToken(token)
                    return@async token
                } else {
                    throw AuthenticatorException("Failed to fetch a new token.")
                }
            }

            return currentTokenJob!!.await()
        }
    }

}