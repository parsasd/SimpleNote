package com.example.simplenote.domain.repository

import com.example.simplenote.domain.model.User
import com.example.simplenote.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(username: String, password: String, email: String, firstName: String, lastName: String): Resource<Unit>
    suspend fun login(username: String, password: String): Resource<Unit>
    suspend fun getUserInfo(): Resource<User>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    suspend fun verifyToken(): Resource<Unit>

    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>

}