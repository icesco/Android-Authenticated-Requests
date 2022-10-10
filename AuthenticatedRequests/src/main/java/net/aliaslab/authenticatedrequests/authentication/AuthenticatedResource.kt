package net.aliaslab.authenticatedrequests.authentication

import net.aliaslab.authenticatedrequests.authentication.Authenticator
import net.aliaslab.authenticatedrequests.model.ARClientCredentials

public interface AuthenticatedResource {

    fun authenticator(): Authenticator<ARClientCredentials>

}