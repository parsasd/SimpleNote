package com.example.simplenote.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)