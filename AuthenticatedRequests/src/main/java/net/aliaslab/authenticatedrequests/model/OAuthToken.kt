package net.aliaslab.authenticatedrequests.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

public data class OAuthToken(
    var date: Date = Date(),
    val access_token: String,
    val refresh_token: String?,
    val expires_in: Int,
    val token_type: String): Parcelable {

    constructor(parcel: Parcel) : this(
        Date(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    /// checks if token is still valid or has expired
    fun isValid(): Boolean {

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, expires_in)

        return calendar.time > Date()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
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
    }

}
