package net.aliaslab.authenticatedrequests.tokenpersistence

import android.os.Parcelable
import java.io.Serializable

public interface TokenStore {

    fun <Input> set(input: Input, forKey: String): Boolean
    fun <Output> item(forKey: String): Output?

    fun delete(key: String): Boolean
}