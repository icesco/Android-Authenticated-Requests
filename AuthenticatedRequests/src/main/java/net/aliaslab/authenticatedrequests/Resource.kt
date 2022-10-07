package net.aliaslab.authenticatedrequests

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.aliaslab.authenticatedrequests.model.Result

public interface Resource {

    fun httpMethod(): HttpMethod

    fun <Input: URLQueryable> urlRequest(input: Input): URLRequest
}

suspend inline fun <Input: URLQueryable, reified Output> Resource.request(parameter: Input): Result<Output> {

    val request = this.urlRequest(parameter)

    return coroutineScope {
        withContext(Dispatchers.IO) {
            try {
                val rawResult = HTTPClient.sendRequest(request)
                val parsedResult = GsonBuilder().create().fromJson(rawResult, Output::class.java)
                return@withContext Result.Success(parsedResult)
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }
}