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
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val preferencesManager: PreferencesManager,
    // This now correctly accepts the unauthorizedApi provided by Hilt
    @Named("unauthorizedApi") private val api: SimpleNoteApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops if a refresh token request itself fails with 401
        if (response.request.url.encodedPath.contains("/api/auth/token/refresh/")) {
            return null
        }

        val refreshToken = runBlocking { preferencesManager.refreshToken.first() }

        if (refreshToken == null) {
            runBlocking { preferencesManager.clearAll() }
            return null
        }

        val newAccessToken = runBlocking {
            try {
                // Use the injected 'api' instance for the refresh call
                val refreshResponse = api.refreshToken(RefreshTokenRequest(refreshToken))
                preferencesManager.saveTokens(refreshResponse.access, refreshToken)
                refreshResponse.access
            } catch (e: Exception) {
                // If refresh fails, clear tokens to force a full re-login.
                preferencesManager.clearAll()
                null
            }
        }

        return if (newAccessToken != null) {
            // If refresh succeeded, retry the original request with the new token.
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            // If refresh failed, do not retry.
            null
        }
    }
}