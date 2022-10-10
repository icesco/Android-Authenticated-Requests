package net.aliaslab.authenticatedrequests

import kotlinx.coroutines.runBlocking
import net.aliaslab.authenticatedrequests.model.ARClientCredentials
import net.aliaslab.authenticatedrequests.model.OAuthToken
import net.aliaslab.authenticatedrequests.requests.AuthenticationEndpoint
import org.junit.Test
import java.net.URL

class RequestsTest {

    @Test
    @Throws(Exception::class)
    fun getTokenWorks() = runBlocking {

        val test = AuthenticationEndpoint(URL("https://idsign-collaudo.aliaslab.net"), "/IDSign.IdP/connect/token", "IDSign-Mobile-Android")

        val result = test.request<ARClientCredentials, OAuthToken>(ARClientCredentials("idsign-coll-aliaslab", "idsign-coll-aliaslab", setOf()))

        val token = result.unwrap()

        print(result)
        assert(token != null)
    }

}