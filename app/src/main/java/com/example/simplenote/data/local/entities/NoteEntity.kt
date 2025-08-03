package com.example.simplenote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: Date,
    val updatedAt: Date,
    val creatorName: String,
    val creatorUsername: String,
    val isSynced: Boolean = true
)