package net.aliaslab.authenticatedrequests

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.aliaslab.authenticatedrequests.authentication.AuthenticatedResource
import net.aliaslab.authenticatedrequests.model.Result
import net.aliaslab.authenticatedrequests.networking.HTTPClient
import net.aliaslab.authenticatedrequests.networking.HttpMethod
import net.aliaslab.authenticatedrequests.networking.URLTransformable
import net.aliaslab.authenticatedrequests.networking.URLRequest

public interface Resource {

    fun httpMethod(): HttpMethod

    fun <Input: URLTransformable> urlRequest(input: Input): URLRequest
}

suspend inline fun <Input: URLTransformable, reified Output> Resource.request(parameter: Input): Result<Output> {

    val request = this.urlRequest(parameter)

    return coroutineScope {
        withContext(Dispatchers.IO) {
            try {
                val authenticated = this as? AuthenticatedResource
                if (authenticated != null) {
                    val token = authenticated.authenticator().validToken()
                    request.authentication = token.token_type + " " + token.access_token
                }
                val rawResult = HTTPClient.sendRequest(request)
                val parsedResult = GsonBuilder().create().fromJson(rawResult, Output::class.java)
                return@withContext Result.Success(parsedResult)
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }
}