package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.auth.model.AuthSession
import com.example.core.auth.model.AuthUser
import com.example.core.auth.storage.SessionStorage
import com.example.core.auth.viewmodel.AuthViewModel
import com.example.core.localization.StrictAuthStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthUnitTest {

    @Test
    fun `test email validation logic`() {
        assertTrue(AuthViewModel.isValidEmail("traveler@example.com"))
        assertTrue(AuthViewModel.isValidEmail("john.doe@tribe.travel"))
        assertFalse(AuthViewModel.isValidEmail("invalid-email"))
        assertFalse(AuthViewModel.isValidEmail("@missing-username.com"))
        assertFalse(AuthViewModel.isValidEmail("missing-domain@"))
    }

    @Test
    fun `test user display name resolution`() {
        // With full_name metadata
        val userWithMeta = AuthUser(
            id = "user-123",
            email = "rakib@example.com",
            userMetadata = mapOf("full_name" to "Rakib Hasan")
        )
        assertEquals("Rakib Hasan", userWithMeta.displayName)

        // Without full_name metadata, falls back to email prefix
        val userWithoutMeta = AuthUser(
            id = "user-456",
            email = "traveler_99@example.com",
            userMetadata = null
        )
        assertEquals("traveler_99", userWithoutMeta.displayName)
    }

    @Test
    fun `test session storage save restore and clear`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = SessionStorage(context)

        storage.clearSession()
        assertNull(storage.restoreSession())
        assertFalse(storage.hasActiveSession())

        val mockUser = AuthUser(
            id = "test-uid-789",
            email = "explorer@tribe.com",
            userMetadata = mapOf("full_name" to "Test Explorer")
        )

        val validSession = AuthSession(
            accessToken = "mock-access-token-xyz",
            refreshToken = "mock-refresh-token-123",
            expiresAtMillis = System.currentTimeMillis() + 3600_000L,
            user = mockUser
        )

        storage.saveSession(validSession)

        val restored = storage.restoreSession()
        assertNotNull(restored)
        assertEquals("mock-access-token-xyz", restored?.accessToken)
        assertEquals("test-uid-789", restored?.user?.id)
        assertEquals("Test Explorer", restored?.user?.displayName)
        assertTrue(storage.hasActiveSession())

        // Clear session on sign out
        storage.clearSession()
        assertNull(storage.restoreSession())
        assertFalse(storage.hasActiveSession())
    }

    @Test
    fun `test session expiration calculation`() {
        val user = AuthUser(id = "user-1", email = "test@example.com")
        val expiredSession = AuthSession(
            accessToken = "expired-token",
            refreshToken = "refresh-token",
            expiresAtMillis = System.currentTimeMillis() - 1000L, // 1 sec ago
            user = user
        )
        assertTrue(expiredSession.isExpired)

        val activeSession = AuthSession(
            accessToken = "active-token",
            refreshToken = "refresh-token",
            expiresAtMillis = System.currentTimeMillis() + 60_000L, // 1 min in future
            user = user
        )
        assertFalse(activeSession.isExpired)
    }

    @Test
    fun `test strict English labels compliance for Phase 2`() {
        assertEquals("Welcome to TRIBE", StrictAuthStrings.WELCOME_TITLE)
        assertEquals("Your next adventure starts here.", StrictAuthStrings.WELCOME_SUBTITLE)
        assertEquals("Continue with Google", StrictAuthStrings.CONTINUE_WITH_GOOGLE)
        assertEquals("Or continue with email", StrictAuthStrings.OR_CONTINUE_WITH_EMAIL)
        assertEquals("Email Address", StrictAuthStrings.EMAIL_ADDRESS)
        assertEquals("Password", StrictAuthStrings.PASSWORD)
        assertEquals("Forgot Password?", StrictAuthStrings.FORGOT_PASSWORD)
        assertEquals("Sign In", StrictAuthStrings.SIGN_IN)
        assertEquals("Create Account", StrictAuthStrings.CREATE_ACCOUNT)
        assertEquals("Sign Up", StrictAuthStrings.SIGN_UP)
        assertEquals("Don't have an account?", StrictAuthStrings.DONT_HAVE_ACCOUNT)
        assertEquals("Already have an account?", StrictAuthStrings.ALREADY_HAVE_ACCOUNT)
    }

    @Test
    fun `test nonce generation and SHA-256 hashing for Supabase and Google Sign-In`() {
        val (rawNonce, hashedNonce) = AuthViewModel.generateNonce()
        assertNotNull(rawNonce)
        assertTrue(rawNonce.isNotBlank())
        assertNotNull(hashedNonce)
        // SHA-256 hex string must be exactly 64 characters
        assertEquals(64, hashedNonce.length)

        // Verify deterministic SHA-256 calculation
        val knownRaw = "test-raw-nonce-12345"
        val (_, deterministicHashed) = AuthViewModel.generateNonce(knownRaw)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val expectedHash = md.digest(knownRaw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        assertEquals(expectedHash, deterministicHashed)
    }

    @Test
    fun `test GoogleIdTokenRequest payload schema for Supabase token endpoint`() {
        val request = com.example.core.auth.model.GoogleIdTokenRequest(
            provider = "google",
            idToken = "mock_google_jwt_token_sample",
            nonce = "raw_nonce_uuid_value"
        )
        assertEquals("google", request.provider)
        assertEquals("mock_google_jwt_token_sample", request.idToken)
        assertEquals("raw_nonce_uuid_value", request.nonce)

        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(com.example.core.auth.model.GoogleIdTokenRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"provider\":\"google\""))
        assertTrue(json.contains("\"id_token\":\"mock_google_jwt_token_sample\""))
        assertTrue(json.contains("\"nonce\":\"raw_nonce_uuid_value\""))
    }
}
