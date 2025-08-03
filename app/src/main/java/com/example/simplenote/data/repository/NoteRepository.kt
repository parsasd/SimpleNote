package com.example.simplenote.domain.repository

import com.example.simplenote.domain.model.Note
import com.example.simplenote.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<Resource<List<Note>>>

    suspend fun refreshNotes(): Resource<Unit>

    suspend fun getNote(id: Int): Resource<Note>

    suspend fun createNote(title: String, description: String): Resource<Note>

    suspend fun updateNote(id: Int, title: String, description: String): Resource<Note>

    suspend fun deleteNote(id: Int): Resource<Unit>

    fun searchNotes(query: String): Flow<Resource<List<Note>>>
}