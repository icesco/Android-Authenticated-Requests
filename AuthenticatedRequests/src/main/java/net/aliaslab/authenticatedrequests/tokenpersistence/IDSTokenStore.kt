package net.aliaslab.authenticatedrequests.tokenpersistence

import android.content.SharedPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.aliaslab.authenticatedrequests.model.OAuthToken
import java.util.*

/**
 * Used to retrieve object from the Preferences.
 *
 * @param key Shared Preference key with which object was saved.
 **/
@Throws(SerializationException::class)
inline fun <reified T> SharedPreferences.get(key: String): T? {
    //We read JSON String which was saved.
    val value = getString(key, null) ?: return null
    //JSON String was found which means object can be read.
    //We convert this JSON String to model object. Parameter "c" (of
    //type Class < T >" is used to cast.
    return Json.decodeFromString(value)
}

/**
 * Used to set an object to the Preferences.
 *
 * @param item The item that needs to be saved into the preferences.
 * @param key Shared Preference key with which object was saved.
 **/
inline fun <reified Input> SharedPreferences.set(input: Input, forKey: String): Boolean {
    return if (input is Date) {
        edit().putLong(forKey, input.time).commit()
    } else {
        val json = Json.encodeToString(input)
        edit().putString(forKey, json).commit()
    }
}

fun SharedPreferences.getDate(forKey: String): Date? {
    val seconds = get<Long>(forKey) ?: return null
    return Date(seconds)
}

fun SharedPreferences.getToken(forKey: String): OAuthToken? {
    return get(forKey)
}

fun SharedPreferences.delete(key: String): Boolean {
    val editor = edit()
    return editor.remove(key).commit()
}