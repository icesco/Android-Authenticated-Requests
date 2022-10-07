package net.aliaslab.authenticatedrequests.requests

import net.aliaslab.authenticatedrequests.HttpMethod
import net.aliaslab.authenticatedrequests.Resource
import net.aliaslab.authenticatedrequests.URLQueryable
import net.aliaslab.authenticatedrequests.URLRequest
import java.net.URL

class AuthenticationEndpoint(
    private val baseEndpoint: URL,
    private val path: String,
    private val userAgent: String? = null) : Resource {

    override fun httpMethod(): HttpMethod {
       return HttpMethod.POST
    }

    override fun <Input: URLQueryable> urlRequest(input: Input): URLRequest {
        return URLRequest(URL(baseEndpoint.toString() + path), input.httpBody(), userAgent, httpMethod(), null)
    }

}