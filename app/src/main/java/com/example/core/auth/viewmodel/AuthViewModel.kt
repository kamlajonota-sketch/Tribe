package com.example.core.auth.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.auth.model.AuthUser
import com.example.core.auth.repository.AuthRepository
import com.example.core.auth.repository.SignUpOutcome
import com.example.core.auth.repository.SupabaseAuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

sealed class AuthState {
    object Initial : AuthState()
    data class Loading(val message: String = "Please wait...") : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
    data class PasswordRecoverySent(val email: String) : AuthState()
    data class ConfirmationEmailSent(val email: String) : AuthState()
    data class GoogleOAuthSetupRequired(
        val reason: String,
        val title: String = "Google Sign-In Configuration Required"
    ) : AuthState()
}

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = SupabaseAuthRepository.getInstance(application)
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(
        authRepository.getCurrentSession()?.let {
            if (!it.isExpired) AuthState.Authenticated(it.user) else null
        } ?: AuthState.Unauthenticated
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            try {
                val session = authRepository.restoreSession()
                if (session != null) {
                    _authState.value = AuthState.Authenticated(session.user)
                } else if (_authState.value is AuthState.Authenticated) {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                // If offline or network check fails, preserve active cached state or stay unauthenticated
                if (_authState.value !is AuthState.Authenticated) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email address.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your password.")
            return
        }

        if (trimmedPassword.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Signing in...")
            val result = authRepository.signInWithEmail(trimmedEmail, trimmedPassword)
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Sign in failed.")
            }
        }
    }

    fun signUpWithEmail(fullName: String, email: String, password: String, confirmPassword: String) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirm = confirmPassword.trim()

        if (trimmedName.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your full name.")
            return
        }

        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email address.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        if (trimmedPassword != trimmedConfirm) {
            _authState.value = AuthState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Creating account...")
            val result = authRepository.signUpWithEmail(trimmedEmail, trimmedPassword, trimmedName)
            result.onSuccess { outcome ->
                when (outcome) {
                    is SignUpOutcome.Authenticated -> {
                        _authState.value = AuthState.Authenticated(outcome.user)
                    }
                    is SignUpOutcome.ConfirmationPending -> {
                        _authState.value = AuthState.ConfirmationEmailSent(outcome.email)
                    }
                }
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Account creation failed.")
            }
        }
    }

    fun recoverPassword(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email address.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Sending recovery instructions...")
            val result = authRepository.recoverPassword(trimmedEmail)
            result.onSuccess {
                _authState.value = AuthState.PasswordRecoverySent(trimmedEmail)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Failed to request password recovery.")
            }
        }
    }

    fun signInWithGoogle(context: Context, serverClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID) {
        viewModelScope.launch {
            val clientId = serverClientId.trim()
            if (clientId.isBlank() || clientId == "placeholder_google_web_client_id") {
                _authState.value = AuthState.GoogleOAuthSetupRequired(
                    "GOOGLE_WEB_CLIENT_ID is not configured yet. Please configure your Google Cloud Web OAuth Client ID in your .env or AI Studio Secrets panel."
                )
                return@launch
            }

            _authState.value = AuthState.Loading("Connecting to Google...")
            try {
                // Nonce handling for Supabase & Google ID Token validation:
                // 1. Generate raw nonce (UUID string)
                // 2. Compute SHA-256 hash of the raw nonce
                // 3. Pass SHA-256 hash to Credential Manager (GetGoogleIdOption)
                // 4. Send raw nonce to Supabase (/auth/v1/token?grant_type=id_token)
                val (rawNonce, hashedNonce) = generateNonce()

                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val authResult = authRepository.signInWithGoogleIdToken(idToken = idToken, nonce = rawNonce)
                    authResult.onSuccess { user ->
                        _authState.value = AuthState.Authenticated(user)
                    }.onFailure { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Google authentication failed on server.")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential returned by Google.")
                }
            } catch (e: GetCredentialCancellationException) {
                // User dismissed the Google sign in dialog
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                // If device Google Play Services has no account or Credential Manager fails,
                // seamlessly launch Supabase's Google OAuth browser flow so user can sign in with Google
                try {
                    val authUrl = "https://cididsanututlefebehw.supabase.co/auth/v1/authorize?provider=google&redirect_to=tribe://auth-callback"
                    val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl)).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    _authState.value = AuthState.Loading("Opening Google Sign-In...")
                } catch (browserError: Exception) {
                    _authState.value = AuthState.Error(
                        "Unable to open Google Sign-In. You can sign in or create an account with email below."
                    )
                }
            }
        }
    }

    fun handleOAuthUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading("Completing sign in...")
            try {
                val params = mutableMapOf<String, String>()
                val fragment = uri.fragment
                if (!fragment.isNullOrBlank()) {
                    fragment.split("&").forEach { pair ->
                        val parts = pair.split("=")
                        if (parts.size == 2) {
                            params[parts[0]] = java.net.URLDecoder.decode(parts[1], "UTF-8")
                        }
                    }
                }
                uri.queryParameterNames.forEach { name ->
                    uri.getQueryParameter(name)?.let { params[name] = it }
                }

                val accessToken = params["access_token"]
                val refreshToken = params["refresh_token"]
                val expiresIn = params["expires_in"]?.toLongOrNull() ?: 3600L

                if (!accessToken.isNullOrBlank()) {
                    val result = authRepository.saveOAuthSession(accessToken, refreshToken, expiresIn)
                    result.onSuccess { user ->
                        _authState.value = AuthState.Authenticated(user)
                    }.onFailure {
                        _authState.value = AuthState.Error(it.message ?: "Failed to save session.")
                    }
                } else {
                    val errorDesc = params["error_description"] ?: params["error"]
                    if (!errorDesc.isNullOrBlank()) {
                        _authState.value = AuthState.Error(errorDesc)
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-in failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading("Signing out...")
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getCurrentSession(): com.example.core.auth.model.AuthSession? {
        return authRepository.getCurrentSession()
    }

    fun clearError() {
        if (_authState.value is AuthState.Error ||
            _authState.value is AuthState.PasswordRecoverySent ||
            _authState.value is AuthState.ConfirmationEmailSent ||
            _authState.value is AuthState.GoogleOAuthSetupRequired
        ) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    companion object {
        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun generateNonce(raw: String = UUID.randomUUID().toString()): Pair<String, String> {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
            val hashed = digest.joinToString("") { "%02x".format(it) }
            return Pair(raw, hashed)
        }
    }
}
