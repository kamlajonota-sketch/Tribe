package com.example.core.auth.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMetadata(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null
) {
    val displayName: String
        get() {
            val metaName = userMetadata?.get("full_name") as? String
            return if (!metaName.isNullOrBlank()) metaName
            else email?.substringBefore("@") ?: "Traveler"
        }
}

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = "bearer",
    @Json(name = "expires_in") val expiresIn: Long? = 3600,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: AuthUser? = null
)

@JsonClass(generateAdapter = true)
data class SignUpResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: AuthUser? = null
)

@JsonClass(generateAdapter = true)
data class SignInRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class RecoverPasswordRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class GoogleIdTokenRequest(
    @Json(name = "provider") val provider: String = "google",
    @Json(name = "id_token") val idToken: String,
    @Json(name = "nonce") val nonce: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthErrorResponse(
    @Json(name = "error") val error: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: Int? = null
) {
    val userFriendlyMessage: String
        get() = errorDescription ?: msg ?: message ?: error ?: "Authentication failed. Please verify credentials."
}

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtMillis: Long,
    val user: AuthUser
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAtMillis
}
