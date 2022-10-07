package net.aliaslab.authenticatedrequests.tokenpersistence

import android.os.Parcelable
import java.io.Serializable

public interface TokenStore {

    fun <Output: Parcelable> parcelable(forKey: String): Output?
    fun <Output: Serializable> serializable(forKey: String): Output?

    fun set(parcelable: Parcelable, forKey: String): Boolean
    fun set(serializable: Serializable, forKey: String): Boolean

    fun delete(key: String): Boolean
}