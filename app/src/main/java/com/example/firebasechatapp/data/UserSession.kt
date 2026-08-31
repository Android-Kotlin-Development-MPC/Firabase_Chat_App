package com.example.firebasechatapp.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Maintains the local user's identity used to tag messages and to decide,
 * at delete time, whether a bubble belongs to the current user.
 *
 * A stable [userId] (random UUID) and an editable [displayName] are persisted
 * in SharedPreferences so they survive app restarts.
 */
class UserSession(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    val userId: String = prefs.getString(KEY_USER_ID, null)
        ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_USER_ID, it).apply()
        }

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        set(value) {
            val trimmed = value.trim().ifBlank { DEFAULT_NAME }
            prefs.edit().putString(KEY_DISPLAY_NAME, trimmed).apply()
        }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        const val DEFAULT_NAME = "Anonymous"
    }
}
