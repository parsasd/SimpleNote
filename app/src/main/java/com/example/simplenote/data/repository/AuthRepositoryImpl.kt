package com.example.simplenote.data.repository

import com.example.simplenote.data.local.PreferencesManager
import com.example.simplenote.data.local.dao.UserDao
import com.example.simplenote.data.local.entities.UserEntity
import com.example.simplenote.data.remote.api.SimpleNoteApi
import com.example.simplenote.data.remote.dto.*
import com.example.simplenote.domain.model.User
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: SimpleNoteApi,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String
    ): Resource<Unit> {
        return try {
            val response = api.register(
                RegisterRequest(username, password, email, firstName, lastName)
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun login(username: String, password: String): Resource<Unit> {
        return try {
            val response = api.login(LoginRequest(username, password))
            // Save tokens immediately upon successful login response
            preferencesManager.saveTokens(response.access, response.refresh)

            // Attempt to get user info. This call will now be protected by AuthInterceptor
            // and potentially TokenAuthenticator for token refresh.
            val userInfo = api.getUserInfo()
            preferencesManager.saveUserInfo(userInfo.id, userInfo.username)

            // Save user to local database
            userDao.insertUser(
                UserEntity(
                    id = userInfo.id,
                    username = userInfo.username,
                    email = userInfo.email,
                    firstName = userInfo.firstName,
                    lastName = userInfo.lastName
                )
            )

            Resource.Success(Unit)
        } catch (e: Exception) {
            // IMPORTANT CHANGE: Removed preferencesManager.clearAll() here.
            // If the login (token acquisition) was successful, but a subsequent
            // call like getUserInfo() fails, we should not immediately clear
            // all tokens. The TokenAuthenticator is responsible for handling
            // authentication failures (e.g., 401s) and clearing tokens if a refresh fails.
            // This prevents a successful login from being immediately undone by a
            // transient error in fetching user details.
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun refreshToken(): Resource<Unit> {
        // This method is primarily intended to be called by the TokenAuthenticator.
        // It's exposed in the repository for completeness but direct manual calls should be rare.
        return try {
            val refreshToken = preferencesManager.refreshToken.first()
                ?: return Resource.Error("No refresh token available for refresh attempt.")

            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            // Keep the old refresh token, only update the access token
            val currentRefreshToken = preferencesManager.refreshToken.first() ?: ""
            preferencesManager.saveTokens(response.access, currentRefreshToken)

            Resource.Success(Unit)
        } catch (e: Exception) {
            // If refresh fails (e.g., refresh token itself is invalid/expired),
            // clear all tokens to force a full re-login.
            preferencesManager.clearAll()
            Resource.Error(e.message ?: "Token refresh failed. Please log in again.")
        }
    }

    override suspend fun getUserInfo(): Resource<User> {
        return try {
            val response = api.getUserInfo()
            // Update local user info in case it changed on the server or was fetched for the first time
            userDao.insertUser(
                UserEntity(
                    id = response.id,
                    username = response.username,
                    email = response.email,
                    firstName = response.firstName,
                    lastName = response.lastName
                )
            )
            Resource.Success(
                User(
                    id = response.id,
                    username = response.username,
                    email = response.email,
                    firstName = response.firstName,
                    lastName = response.lastName
                )
            )
        } catch (e: Exception) {
            // If network fetch fails, try to retrieve user info from the local database as a fallback.
            val userId = preferencesManager.userId.first()
            val userEntity = if (userId != null) userDao.getUserById(userId) else null

            if (userEntity != null) {
                Resource.Success(
                    User(
                        id = userEntity.id,
                        username = userEntity.username,
                        email = userEntity.email,
                        firstName = userEntity.firstName,
                        lastName = userEntity.lastName
                    )
                )
            } else {
                // If network failed and no local data, then it's a true error.
                // This might indicate the user is genuinely not logged in or their session is invalid.
                Resource.Error(e.message ?: "Failed to get user info. No cached data available.")
            }
        }
    }

    override suspend fun logout() {
        // Clear all authentication tokens and user data from local storage
        preferencesManager.clearAll()
        userDao.deleteAllUsers()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        // A user is considered logged in if an access token is present in preferences.
        // The TokenAuthenticator will handle the validity and refreshing of this token.
        return preferencesManager.accessToken.map { it != null }
    }
}
