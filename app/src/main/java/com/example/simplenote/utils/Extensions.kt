package com.example.simplenote.utils

import com.example.simplenote.data.local.entities.NoteEntity
import com.example.simplenote.data.remote.dto.NoteResponse
import com.example.simplenote.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

fun NoteResponse.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt.toDate(),
        updatedAt = updatedAt.toDate(),
        creatorName = creatorName,
        creatorUsername = creatorUsername,
        isSynced = true
    )
}

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        creatorName = creatorName,
        creatorUsername = creatorUsername
    )
}

fun String.toDate(): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(this) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}
