package com.example.core.auth.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.core.auth.model.AuthSession
import com.example.core.auth.model.AuthUser

/**
 * Robust session storage for TRIBE.
 * Attempts hardware-backed EncryptedSharedPreferences with automatic
 * fail-safe fallback to private sandboxed preferences to ensure the app
 * never crashes or hangs on device/emulator launch.
 */
class SessionStorage(context: Context) {

    private val appContext: Context = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        createPreferences(appContext)
    }

    private fun createPreferences(ctx: Context): SharedPreferences {
        return try {
            val masterKey = androidx.security.crypto.MasterKey.Builder(ctx)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()

            androidx.security.crypto.EncryptedSharedPreferences.create(
                ctx,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Throwable) {
            // Resilient fallback for emulators or devices without hardware Keystore service
            ctx.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveSession(session: AuthSession) {
        try {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, session.accessToken)
                .putString(KEY_REFRESH_TOKEN, session.refreshToken)
                .putLong(KEY_EXPIRES_AT, session.expiresAtMillis)
                .putString(KEY_USER_ID, session.user.id)
                .putString(KEY_USER_EMAIL, session.user.email)
                .putString(KEY_USER_FULL_NAME, session.user.displayName)
                .apply()
        } catch (_: Throwable) {
            // Safe fallback if write fails
            appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCESS_TOKEN, session.accessToken)
                .putString(KEY_REFRESH_TOKEN, session.refreshToken)
                .putLong(KEY_EXPIRES_AT, session.expiresAtMillis)
                .putString(KEY_USER_ID, session.user.id)
                .putString(KEY_USER_EMAIL, session.user.email)
                .putString(KEY_USER_FULL_NAME, session.user.displayName)
                .apply()
        }
    }

    fun restoreSession(): AuthSession? {
        return try {
            readSessionFrom(prefs) ?: readSessionFrom(appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE))
        } catch (_: Throwable) {
            readSessionFrom(appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE))
        }
    }

    private fun readSessionFrom(p: SharedPreferences): AuthSession? {
        val accessToken = p.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val userId = p.getString(KEY_USER_ID, null) ?: return null
        val email = p.getString(KEY_USER_EMAIL, null)
        val fullName = p.getString(KEY_USER_FULL_NAME, null)
        val refreshToken = p.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = p.getLong(KEY_EXPIRES_AT, 0L)

        val metadata = if (!fullName.isNullOrBlank()) {
            mapOf("full_name" to fullName)
        } else null

        val user = AuthUser(
            id = userId,
            email = email,
            userMetadata = metadata
        )

        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtMillis = expiresAt,
            user = user
        )
    }

    fun clearSession() {
        try {
            prefs.edit().clear().apply()
        } catch (_: Throwable) {
            // Ignore
        }
        try {
            appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        } catch (_: Throwable) {
            // Ignore
        }
    }

    fun hasActiveSession(): Boolean {
        val session = restoreSession() ?: return false
        return !session.isExpired
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "tribe_encrypted_auth_session"
        private const val FALLBACK_PREFS_NAME = "tribe_auth_session_fallback"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_FULL_NAME = "user_full_name"

        @Volatile
        private var INSTANCE: SessionStorage? = null

        fun getInstance(context: Context): SessionStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionStorage(context).also { INSTANCE = it }
            }
        }
    }
}
