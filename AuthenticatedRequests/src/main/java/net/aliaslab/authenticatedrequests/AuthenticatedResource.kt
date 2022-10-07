package net.aliaslab.authenticatedrequests

import net.aliaslab.authenticatedrequests.model.ARClientCredentials

public interface AuthenticatedResource {

    fun authenticator(): Authenticator<ARClientCredentials>

}