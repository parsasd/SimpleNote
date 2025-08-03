package com.example.simplenote.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NoteResponse(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("creator_name")
    val creatorName: String,
    @SerializedName("creator_username")
    val creatorUsername: String
)

data class NotesListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NoteResponse>
)

data class CreateNoteRequest(
    val title: String,
    val description: String
)

data class UpdateNoteRequest(
    val title: String,
    val description: String
)