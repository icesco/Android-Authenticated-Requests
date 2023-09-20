package net.aliaslab.authenticatedrequests.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class OAuthToken(
    var dateEpoch: Long = Date().time,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String?,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String): Parcelable {

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
            calendar.add(Calendar.SECOND, expiresIn)
            calendar.time > Date()
        } catch (exception: java.lang.Exception) {
            false
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dateEpoch)
        parcel.writeString(accessToken)
        parcel.writeString(refreshToken)
        parcel.writeInt(expiresIn)
        parcel.writeString(tokenType)
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
