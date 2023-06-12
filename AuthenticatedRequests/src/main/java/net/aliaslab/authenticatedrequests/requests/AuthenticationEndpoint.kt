package net.aliaslab.authenticatedrequests.requests

import net.aliaslab.authenticatedrequests.networking.HttpMethod
import net.aliaslab.authenticatedrequests.Resource
import net.aliaslab.authenticatedrequests.networking.URLRequest
import net.aliaslab.authenticatedrequests.networking.URLTransformable
import java.net.URL

class AuthenticationEndpoint(
    private val baseEndpoint: URL,
    private val path: String,
    private val userAgent: String? = null) : Resource {

    override fun httpMethod(): HttpMethod {
       return HttpMethod.POST
    }

    override fun <Input: URLTransformable> urlRequest(input: Input): URLRequest {
        return URLRequest(URL(baseEndpoint.toString() + path), input.httpBody(), userAgent, "application/x-www-form-urlencoded", httpMethod(), null)
    }

}