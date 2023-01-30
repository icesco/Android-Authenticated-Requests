package net.aliaslab.authenticatedrequests.model

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    fun unwrap(): R? {
        return when (this) {
            is Success<*> -> data as? R

            else -> null
        }
    }

    fun error(): Exception? {
        return when (this) {
            is Error -> exception
            else -> null
        }
    }
}