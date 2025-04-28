package net.aliaslab.authenticatedrequests

import android.util.Log
import kotlinx.serialization.json.Json
import net.aliaslab.authenticatedrequests.authentication.AuthenticatedResource
import net.aliaslab.authenticatedrequests.model.Result
import net.aliaslab.authenticatedrequests.networking.HTTPClient
import net.aliaslab.authenticatedrequests.networking.HttpMethod
import net.aliaslab.authenticatedrequests.networking.URLTransformable
import net.aliaslab.authenticatedrequests.networking.URLRequest
import java.io.File

interface Resource {

    fun httpMethod(): HttpMethod

    fun <Input: URLTransformable> urlRequest(input: Input): URLRequest
}

class EmptyResult

suspend inline fun <Input: URLTransformable, reified Output> Resource.request(
    parameter: Input,
    serializer: Json = Json { ignoreUnknownKeys = true }
): Result<Output> {

    val request = this.urlRequest(parameter)

    return try {
        val authenticated = this as? AuthenticatedResource
        if (authenticated != null) {
            val token = authenticated.authenticator().validToken()
            request.authentication = token.token_type + " " + token.access_token
        }

        val rawResult = HTTPClient.sendRequest(request)

        Log.d("Resource", "Raw result: $rawResult")

        when (Output::class.java) {
            String::class.java -> Result.Success(rawResult as Output)
            EmptyResult::class.java -> Result.Success(EmptyResult() as Output)
            else -> {
                val resultItem = serializer.decodeFromString<Output>(rawResult)
                Result.Success(resultItem)
            }
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}

suspend inline fun <Input: URLTransformable> Resource.download(
    parameter: Input,
    destinationFile: File
): Result<Boolean> {

    val request = this.urlRequest(parameter)

    return try {
        val authenticated = this as? AuthenticatedResource
        if (authenticated != null) {
            val token = authenticated.authenticator().validToken()
            request.authentication = token.token_type + " " + token.access_token
        }

        HTTPClient.sendDownloadRequest(request, destinationFile)
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }
}