package com.example.simplenote.data.remote.interceptors

import com.example.simplenote.data.local.PreferencesManager
import com.example.simplenote.data.remote.api.SimpleNoteApi
import com.example.simplenote.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TokenAuthenticator] is an OkHttp Authenticator that handles refreshing access tokens
 * when a 401 Unauthorized response is received from the server.
 * It attempts to use the refresh token to obtain a new access token and retries the original request.
 * If the refresh fails, it clears the user's session.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val preferencesManager: PreferencesManager
    // IMPORTANT: Do NOT inject SimpleNoteApi directly here to avoid circular dependency.
    // A new Retrofit instance for refresh calls is created within authenticate().
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops if a refresh request itself returns 401
        if (response.request.url.encodedPath.contains("/api/auth/token/refresh/")) {
            return null // Do not retry a refresh token request if it fails
        }

        // If the original request did not have an Authorization header,
        // it means it wasn't an authenticated request, so we don't try to refresh.
        if (response.request.header("Authorization") == null) {
            return null
        }

        // Get the refresh token synchronously from preferences
        val refreshToken = runBlocking { preferencesManager.refreshToken.first() }

        // If no refresh token is available, clear all tokens and return null to indicate no retry.
        if (refreshToken == null) {
            runBlocking { preferencesManager.clearAll() } // Clear all tokens to force re-login
            return null
        }

        // Synchronously call the refresh token API to get a new access token
        val newAccessToken = runBlocking {
            try {
                // Create a new, minimal Retrofit instance specifically for the refresh token call.
                // This prevents circular dependencies with the main OkHttpClient and its interceptors/authenticators.
                val refreshApi = retrofit2.Retrofit.Builder()
                    .baseUrl("http://192.168.0.245:8000/") // Ensure this base URL matches your API
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
                    .create(SimpleNoteApi::class.java)

                val refreshResponse = refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                // Save the new access token and the existing refresh token
                preferencesManager.saveTokens(refreshResponse.access, refreshToken)
                refreshResponse.access
            } catch (e: Exception) {
                // If refresh fails (e.g., refresh token is expired or invalid),
                // clear all tokens to force a full re-login.
                preferencesManager.clearAll()
                null // Indicate that no new request should be made
            }
        }

        return if (newAccessToken != null) {
            // If a new access token was successfully obtained,
            // retry the original request with the new token.
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            // If token refresh failed (newAccessToken is null), do not retry the request.
            // The original request will ultimately fail with 401, and the app should
            // handle this by redirecting to the login screen.
            null
        }
    }
}
