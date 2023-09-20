package net.aliaslab.authenticatedrequests.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class OAuthToken(
    var dateEpoch: Long = Date().time,
    val access_token: String,
    val refresh_token: String? = null,
    val expires_in: Int,
    val token_type: String): Parcelable {

    var date: Date
        get() = Date(dateEpoch)
        set(value) {
            dateEpoch = value.time
        }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    /// checks if token is still valid or has expired
    fun isValid(): Boolean {

        if (this == invalidToken) {
            return false
        }

        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.SECOND, expires_in)
            calendar.time > Date()
        } catch (exception: java.lang.Exception) {
            false
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dateEpoch)
        parcel.writeString(access_token)
        parcel.writeString(refresh_token)
        parcel.writeInt(expires_in)
        parcel.writeString(token_type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OAuthToken> {
        override fun createFromParcel(parcel: Parcel): OAuthToken {
            return OAuthToken(parcel)
        }

        override fun newArray(size: Int): Array<OAuthToken?> {
            return arrayOfNulls(size)
        }

        val invalidToken: OAuthToken = OAuthToken(0, "invalid", null, -1000,  "invalid")
    }

}
