package com.example.simplenote.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class RegisterResponse(
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class PasswordChangeRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class RefreshTokenRequest(
    val refresh: String
)

data class AccessTokenResponse(
    val access: String
)
