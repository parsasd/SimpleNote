package com.example.simplenote.domain.model

import java.util.Date

data class Note(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: Date,
    val updatedAt: Date,
    val creatorName: String,
    val creatorUsername: String
)