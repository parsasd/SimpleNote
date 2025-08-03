package com.example.simplenote.data.repository

import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.local.entities.NoteEntity
import com.example.simplenote.data.remote.api.SimpleNoteApi
import com.example.simplenote.data.remote.dto.CreateNoteRequest
import com.example.simplenote.data.remote.dto.UpdateNoteRequest
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import com.example.simplenote.utils.toDate
import com.example.simplenote.utils.toNote
import com.example.simplenote.utils.toNoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val api: SimpleNoteApi,
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<Resource<List<Note>>> = flow {
        emit(Resource.Loading())

        // First emit cached data
        noteDao.getAllNotes().collect { entities ->
            emit(Resource.Success(entities.map { it.toNote() }))
        }
    }

    override suspend fun refreshNotes(): Resource<Unit> {
        return try {
            val response = api.getNotes()
            val entities = response.results.map { it.toNoteEntity() }
            noteDao.insertNotes(entities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh notes")
        }
    }

    override suspend fun getNote(id: Int): Resource<Note> {
        return try {
            // Try to get from network first
            val response = api.getNote(id)
            val entity = response.toNoteEntity()
            noteDao.insertNote(entity)
            Resource.Success(entity.toNote())
        } catch (e: Exception) {
            // Fallback to local database
            val entity = noteDao.getNoteById(id)
            if (entity != null) {
                Resource.Success(entity.toNote())
            } else {
                Resource.Error(e.message ?: "Note not found")
            }
        }
    }

    override suspend fun createNote(title: String, description: String): Resource<Note> {
        return try {
            val response = api.createNote(CreateNoteRequest(title, description))
            val entity = response.toNoteEntity()
            noteDao.insertNote(entity)
            Resource.Success(entity.toNote())
        } catch (e: Exception) {
            // For offline support, could create a local note with isSynced = false
            Resource.Error(e.message ?: "Failed to create note")
        }
    }

    override suspend fun updateNote(id: Int, title: String, description: String): Resource<Note> {
        return try {
            val response = api.updateNote(id, UpdateNoteRequest(title, description))
            val entity = response.toNoteEntity()
            noteDao.updateNote(entity)
            Resource.Success(entity.toNote())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update note")
        }
    }

    override suspend fun deleteNote(id: Int): Resource<Unit> {
        return try {
            api.deleteNote(id)
            noteDao.getNoteById(id)?.let { noteDao.deleteNote(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete note")
        }
    }

    override fun searchNotes(query: String): Flow<Resource<List<Note>>> {
        return noteDao.searchNotes("%$query%").map { entities ->
            Resource.Success(entities.map { it.toNote() })
        }
    }
}