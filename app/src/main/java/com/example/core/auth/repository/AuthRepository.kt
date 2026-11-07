package com.example.core.auth.repository

import android.content.Context
import com.example.BuildConfig
import com.example.core.auth.api.SupabaseAuthApi
import com.example.core.auth.model.AuthErrorResponse
import com.example.core.auth.model.AuthSession
import com.example.core.auth.model.AuthUser
import com.example.core.auth.model.GoogleIdTokenRequest
import com.example.core.auth.model.RecoverPasswordRequest
import com.example.core.auth.model.RefreshTokenRequest
import com.example.core.auth.model.SignInRequest
import com.example.core.auth.model.SignUpRequest
import com.example.core.auth.storage.SessionStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SignUpOutcome {
    data class Authenticated(val user: AuthUser) : SignUpOutcome()
    data class ConfirmationPending(val email: String) : SignUpOutcome()
}

class AuthException(message: String, val statusCode: Int = 0) : Exception(message)
class MissingCredentialsException(message: String) : Exception(message)

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<SignUpOutcome>
    suspend fun recoverPassword(email: String): Result<Unit>
    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String? = null): Result<AuthUser>
    suspend fun saveOAuthSession(accessToken: String, refreshToken: String?, expiresIn: Long): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
    suspend fun restoreSession(): AuthSession?
    fun getCurrentSession(): AuthSession?
    fun isSupabaseConfigured(): Boolean
}

class SupabaseAuthRepository(
    private val sessionStorage: SessionStorage,
    private val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    private val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
) : AuthRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(AuthErrorResponse::class.java)

    private val api: SupabaseAuthApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthApi::class.java)
    }

    override fun isSupabaseConfigured(): Boolean {
        return supabaseAnonKey.isNotBlank() &&
                supabaseAnonKey != "placeholder_anon_key" &&
                supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("your-project")
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            if (!isSupabaseConfigured()) {
                return@withContext Result.failure(
                    MissingCredentialsException(
                        "Supabase Publishable/Anon Key is not configured yet. Please configure SUPABASE_ANON_KEY in your environment."
                    )
                )
            }

            try {
                val response = api.signInWithPassword(
                    apiKey = supabaseAnonKey,
                    request = SignInRequest(email = email.trim(), password = password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val tokenResponse = response.body()!!
                    val user = tokenResponse.user
                        ?: return@withContext Result.failure(AuthException("Invalid response from auth server: user object missing"))

                    val token = tokenResponse.accessToken
                        ?: return@withContext Result.failure(AuthException("Access token missing in server response"))

                    val expiresAt = System.currentTimeMillis() + ((tokenResponse.expiresIn ?: 3600L) * 1000L)
                    val session = AuthSession(
                        accessToken = token,
                        refreshToken = tokenResponse.refreshToken,
                        expiresAtMillis = expiresAt,
                        user = user
                    )
                    sessionStorage.saveSession(session)
                    Result.success(user)
                } else {
                    Result.failure(parseError(response))
                }
            } catch (e: IOException) {
                Result.failure(AuthException("Network error: Unable to connect to Supabase. Check your internet connection."))
            } catch (e: Exception) {
                Result.failure(AuthException("Sign in failed: ${e.message ?: "Unknown error"}"))
            }
        }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<SignUpOutcome> = withContext(Dispatchers.IO) {
        if (!isSupabaseConfigured()) {
            return@withContext Result.failure(
                MissingCredentialsException(
                    "Supabase Publishable/Anon Key is not configured yet. Please configure SUPABASE_ANON_KEY in your environment."
                )
            )
        }

        try {
            val metadata = if (fullName.isNotBlank()) mapOf("full_name" to fullName.trim()) else null
            val response = api.signUp(
                apiKey = supabaseAnonKey,
                request = SignUpRequest(email = email.trim(), password = password, data = metadata)
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.accessToken
                val user = body.user ?: body.id?.let {
                    AuthUser(id = it, email = body.email ?: email, userMetadata = metadata)
                }

                if (token != null && user != null) {
                    val expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600L) * 1000L)
                    sessionStorage.saveSession(
                        AuthSession(
                            accessToken = token,
                            refreshToken = body.refreshToken,
                            expiresAtMillis = expiresAt,
                            user = user
                        )
                    )
                    Result.success(SignUpOutcome.Authenticated(user))
                } else {
                    // Email confirmation required by Supabase project settings
                    Result.success(SignUpOutcome.ConfirmationPending(email.trim()))
                }
            } else {
                Result.failure(parseError(response))
            }
        } catch (e: IOException) {
            Result.failure(AuthException("Network error: Unable to reach auth server."))
        } catch (e: Exception) {
            Result.failure(AuthException("Registration failed: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isSupabaseConfigured()) {
            return@withContext Result.failure(
                MissingCredentialsException(
                    "Supabase Publishable/Anon Key is not configured yet. Please configure SUPABASE_ANON_KEY in your environment."
                )
            )
        }

        try {
            val response = api.recoverPassword(
                apiKey = supabaseAnonKey,
                request = RecoverPasswordRequest(email = email.trim())
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(parseError(response))
            }
        } catch (e: IOException) {
            Result.failure(AuthException("Network error: Unable to reach auth server."))
        } catch (e: Exception) {
            Result.failure(AuthException("Password recovery request failed: ${e.message}"))
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String?): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            if (!isSupabaseConfigured()) {
                return@withContext Result.failure(
                    MissingCredentialsException(
                        "Supabase Publishable/Anon Key is not configured yet. Please configure SUPABASE_ANON_KEY in your environment."
                    )
                )
            }

            try {
                val response = api.signInWithGoogleIdToken(
                    apiKey = supabaseAnonKey,
                    request = GoogleIdTokenRequest(
                        provider = "google",
                        idToken = idToken,
                        nonce = nonce
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = body.user
                        ?: return@withContext Result.failure(AuthException("User missing in Google auth response"))
                    val token = body.accessToken
                        ?: return@withContext Result.failure(AuthException("Access token missing in Google auth response"))

                    val expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600L) * 1000L)
                    sessionStorage.saveSession(
                        AuthSession(
                            accessToken = token,
                            refreshToken = body.refreshToken,
                            expiresAtMillis = expiresAt,
                            user = user
                        )
                    )
                    Result.success(user)
                } else {
                    Result.failure(parseError(response))
                }
            } catch (e: IOException) {
                Result.failure(AuthException("Network error: Failed to connect to Supabase."))
            } catch (e: Exception) {
                Result.failure(AuthException("Google authentication failed: ${e.message}"))
            }
        }

    override suspend fun saveOAuthSession(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        if (!isSupabaseConfigured()) {
            return@withContext Result.failure(MissingCredentialsException("Supabase is not configured."))
        }

        try {
            val response = api.getUser(
                apiKey = supabaseAnonKey,
                bearerToken = "Bearer $accessToken"
            )
            val user = if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                AuthUser(id = "oauth-user", email = "Google User")
            }

            val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)
            val session = AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAtMillis = expiresAt,
                user = user
            )
            sessionStorage.saveSession(session)
            Result.success(user)
        } catch (e: Exception) {
            val fallbackUser = AuthUser(id = "oauth-user", email = "Google User")
            val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)
            sessionStorage.saveSession(
                AuthSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAtMillis = expiresAt,
                    user = fallbackUser
                )
            )
            Result.success(fallbackUser)
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        val session = sessionStorage.restoreSession()
        if (session != null && isSupabaseConfigured()) {
            try {
                api.logout(
                    apiKey = supabaseAnonKey,
                    bearerToken = "Bearer ${session.accessToken}"
                )
            } catch (_: Exception) {
                // Ignore network error on logout so local sign-out always completes cleanly
            }
        }
        sessionStorage.clearSession()
        Result.success(Unit)
    }

    override suspend fun restoreSession(): AuthSession? = withContext(Dispatchers.IO) {
        val session = sessionStorage.restoreSession() ?: return@withContext null
        if (!session.isExpired) {
            return@withContext session
        }

        // Token expired; attempt refresh if refresh_token exists
        val refreshToken = session.refreshToken ?: run {
            sessionStorage.clearSession()
            return@withContext null
        }

        if (!isSupabaseConfigured()) {
            return@withContext session
        }

        try {
            val response = api.refreshToken(
                apiKey = supabaseAnonKey,
                request = RefreshTokenRequest(refreshToken = refreshToken)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val newAccessToken = body.accessToken ?: return@withContext null
                val user = body.user ?: session.user
                val expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600L) * 1000L)
                val refreshedSession = AuthSession(
                    accessToken = newAccessToken,
                    refreshToken = body.refreshToken ?: refreshToken,
                    expiresAtMillis = expiresAt,
                    user = user
                )
                sessionStorage.saveSession(refreshedSession)
                refreshedSession
            } else {
                sessionStorage.clearSession()
                null
            }
        } catch (_: Exception) {
            // Keep existing session if offline
            session
        }
    }

    override fun getCurrentSession(): AuthSession? {
        return sessionStorage.restoreSession()
    }

    private fun <T> parseError(response: Response<T>): AuthException {
        val errorBody = response.errorBody()?.string()
        val errorResponse = errorBody?.let {
            try {
                errorAdapter.fromJson(it)
            } catch (_: Exception) {
                null
            }
        }
        val message = errorResponse?.userFriendlyMessage ?: "Error (${response.code()}): ${response.message()}"
        return AuthException(message, response.code())
    }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseAuthRepository? = null

        fun getInstance(context: Context): SupabaseAuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseAuthRepository(
                    sessionStorage = SessionStorage.getInstance(context)
                ).also { INSTANCE = it }
            }
        }
    }
}
