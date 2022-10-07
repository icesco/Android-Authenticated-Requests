package net.aliaslab.authenticatedrequests.model

enum class KeychainKey {

    CLIENT_TOKEN, CREATION_DATE;

    override fun toString(): String {
        return when (this) {
            CLIENT_TOKEN -> "clientToken"
            CREATION_DATE -> "creationDate"
        }
    }

    fun prefixed(prefix: String): String {
        return prefix + this.toString()
    }

}