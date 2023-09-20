package net.aliaslab.authenticatedrequests.authentication


import net.aliaslab.authenticatedrequests.model.ARClientCredentials

interface AuthenticatedResource {

    fun authenticator(): Authenticator<ARClientCredentials>

}