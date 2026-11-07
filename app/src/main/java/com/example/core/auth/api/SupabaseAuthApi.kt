package com.example.core.auth.api

import com.example.core.auth.model.AuthUser
import com.example.core.auth.model.GoogleIdTokenRequest
import com.example.core.auth.model.RecoverPasswordRequest
import com.example.core.auth.model.RefreshTokenRequest
import com.example.core.auth.model.SignInRequest
import com.example.core.auth.model.SignUpRequest
import com.example.core.auth.model.SignUpResponse
import com.example.core.auth.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseAuthApi {

    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(
        @Header("apikey") apiKey: String,
        @Body request: SignInRequest
    ): Response<TokenResponse>

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @POST("auth/v1/recover")
    suspend fun recoverPassword(
        @Header("apikey") apiKey: String,
        @Body request: RecoverPasswordRequest
    ): Response<Unit>

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refreshToken(
        @Header("apikey") apiKey: String,
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>

    @POST("auth/v1/token?grant_type=id_token")
    suspend fun signInWithGoogleIdToken(
        @Header("apikey") apiKey: String,
        @Body request: GoogleIdTokenRequest
    ): Response<TokenResponse>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Response<Unit>

    @GET("auth/v1/user")
    suspend fun getUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Response<AuthUser>
}
