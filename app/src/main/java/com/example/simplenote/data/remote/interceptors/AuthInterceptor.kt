package com.example.simplenote.data.remote.interceptors

import com.example.simplenote.data.local.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Retrieve the access token from preferences synchronously using runBlocking.
        // This is necessary because interceptors are synchronous.
        val token = runBlocking {
            preferencesManager.accessToken.first()
        }

        // Define which authentication-related endpoints should NOT receive an Authorization header.
        // Login and Register endpoints typically do not require an access token.
        // The userinfo endpoint, however, usually requires an access token.
        // We explicitly check for the exact paths for login and register to avoid
        // excluding userinfo which is also under /api/auth/ but requires auth.
        val path = originalRequest.url.encodedPath
        val isLoginOrRegisterEndpoint = path.endsWith("/api/auth/token/") ||
                path.endsWith("/api/auth/register/")

        // If a token exists and the current request is not for a login/register endpoint,
        // add the Authorization header.
        return if (token != null && !isLoginOrRegisterEndpoint) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            // Proceed with the original request if no token is available or if it's a login/register endpoint.
            chain.proceed(originalRequest)
        }
    }
}
