package com.example.simplenote.data.repository

import android.util.Log
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
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    @Named("authorizedApi") private val api: SimpleNoteApi,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun register(username: String, password: String, email: String, firstName: String, lastName: String): Resource<Unit> {
        return try {
            api.register(RegisterRequest(username, password, email, firstName, lastName))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun login(username: String, password: String): Resource<Unit> {
        return try {
            val response = api.login(LoginRequest(username, password))
            preferencesManager.saveTokens(response.access, response.refresh)
            verifyToken()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun getUserInfo(): Resource<User> {
        return try {
            val response = api.getUserInfo()
            val user = User(response.id, response.username, response.email, response.firstName, response.lastName)
            userDao.insertUser(UserEntity(user.id, user.username, user.email, user.firstName, user.lastName))
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user info")
        }
    }

    override suspend fun logout() {
        preferencesManager.clearAll()
        userDao.deleteAllUsers()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return preferencesManager.accessToken.map { it != null }
    }

    override suspend fun verifyToken(): Resource<Unit> {
        Log.d("AuthCheck", "--- Starting token verification ---")
        try {
            val token = preferencesManager.accessToken.first()
            Log.d("AuthCheck", "Retrieved token from preferences: $token")

            if (token == null) {
                Log.d("AuthCheck", "Result: No token found. User is unauthenticated.")
                return Resource.Error("No token found.")
            }

            Log.d("AuthCheck", "Token found. Attempting to get user info from server...")
            val userInfoResponse = api.getUserInfo()
            Log.d("AuthCheck", "Successfully got user info: ${userInfoResponse.username}. User is authenticated.")

            preferencesManager.saveUserInfo(userInfoResponse.id, userInfoResponse.username)
            userDao.insertUser(UserEntity(userInfoResponse.id, userInfoResponse.username, userInfoResponse.email, userInfoResponse.firstName, userInfoResponse.lastName))
            return Resource.Success(Unit)

        } catch (e: HttpException) {
            Log.e("AuthCheck", "Result: HTTP Exception during verification. Clearing session. Error: ${e.message}")
            preferencesManager.clearAll()
            return Resource.Error("Session expired. Please log in.")

        } catch (e: Exception) {
            Log.e("AuthCheck", "Result: Generic Exception during verification. Clearing session. Error: ${e.message}")
            preferencesManager.clearAll()
            return Resource.Error("Could not connect to the server.")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val response = api.changePassword(PasswordChangeRequest(oldPassword, newPassword))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Handle specific error messages from the server if available
                Resource.Error("Failed to change password. Please check your old password.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}