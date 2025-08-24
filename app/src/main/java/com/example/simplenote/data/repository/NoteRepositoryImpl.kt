package com.example.simplenote.data.repository

import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.local.entities.NoteEntity
import com.example.simplenote.data.remote.api.SimpleNoteApi
import com.example.simplenote.data.remote.dto.CreateNoteRequest
import com.example.simplenote.data.remote.dto.UpdateNoteRequest
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import com.example.simplenote.utils.toNote
import com.example.simplenote.utils.toNoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import androidx.paging.Pager          // <-- new imports
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import java.util.Date
import kotlin.random.Random
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val api: SimpleNoteApi,
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<Resource<List<Note>>> = flow {
        emit(Resource.Loading())
        noteDao.getAllNotes().collect { entities ->
            emit(Resource.Success(entities.map { it.toNote() }))
        }
    }

    /** Synchronize all pages of notes from the API. */
    override suspend fun refreshNotes(): Resource<Unit> {
        return try {
            syncUnsyncedNotes()
            val allEntities = mutableListOf<NoteEntity>()
            var page = 1
            do {
                val response = api.getNotes(page = page)
                allEntities += response.results.map { it.toNoteEntity() }
                page++
            } while (response.next != null) // continue until there is no next page
            noteDao.insertNotes(allEntities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh notes")
        }
    }

    override suspend fun getNote(id: Int): Resource<Note> {
        return try {
            val response = api.getNote(id)
            val entity = response.toNoteEntity()
            noteDao.insertNote(entity)
            Resource.Success(entity.toNote())
        } catch (e: Exception) {
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
        } catch (_: Exception) {
            // Offline – create a local note with a negative ID
            val now = Date()
            val localEntity = NoteEntity(
                id = generateLocalId(),
                title = title,
                description = description,
                createdAt = now,
                updatedAt = now,
                creatorName = "",
                creatorUsername = "",
                isSynced = false,
                isDeleted = false
            )
            noteDao.insertNote(localEntity)
            Resource.Success(localEntity.toNote())
        }
    }

    override suspend fun updateNote(id: Int, title: String, description: String): Resource<Note> {
        return try {
            val response = api.updateNote(id, UpdateNoteRequest(title, description))
            val entity = response.toNoteEntity()
            noteDao.updateNote(entity)
            Resource.Success(entity.toNote())
        } catch (e: Exception) {
            // Offline – update locally and mark as unsynced
            val local = noteDao.getNoteById(id)
            return if (local != null) {
                val updated = local.copy(
                    title = title,
                    description = description,
                    updatedAt = Date(),
                    isSynced = false
                )
                noteDao.updateNote(updated)
                Resource.Success(updated.toNote())
            } else {
                Resource.Error(e.message ?: "Failed to update note")
            }
        }
    }

    override suspend fun deleteNote(id: Int): Resource<Unit> {
        return try {
            api.deleteNote(id)
            noteDao.getNoteById(id)?.let { noteDao.deleteNote(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            // Offline – mark the note as deleted and unsynced
            val local = noteDao.getNoteById(id)
            return if (local != null) {
                val deleted = local.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = Date()
                )
                noteDao.updateNote(deleted)
                Resource.Success(Unit)
            } else {
                Resource.Error(e.message ?: "Failed to delete note")
            }
        }
    }

    override fun searchNotes(query: String): Flow<Resource<List<Note>>> {
        return noteDao.searchNotes("%$query%").map { entities ->
            Resource.Success(entities.map { it.toNote() })
        }
    }

    /** Paging flow for notes; uses Room as the single source of truth. */
    override fun getPagedNotes(query: String?): Flow<PagingData<Note>> {
        val pagingSourceFactory = if (query.isNullOrBlank()) {
            { noteDao.pagingSource() }
        } else {
            { noteDao.searchPagingSource("%$query%") }
        }
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toNote() }
        }
    }

    /** Generate a unique negative ID for offline notes. */
    private fun generateLocalId(): Int {
        return -Random.nextInt(1, Int.MAX_VALUE)
    }

    /** Upload locally created/updated/deleted notes to the server. */
    private suspend fun syncUnsyncedNotes() {
        val unsynced = noteDao.getUnsyncedNotes()
        for (note in unsynced) {
            try {
                if (note.isDeleted) {
                    if (note.id > 0) {
                        api.deleteNote(note.id)
                    }
                    noteDao.deleteNote(note)
                } else {
                    if (note.id > 0) {
                        val response = api.updateNote(
                            note.id,
                            UpdateNoteRequest(note.title, note.description)
                        )
                        val updated = response.toNoteEntity()
                        noteDao.updateNote(updated)
                    } else {
                        val response = api.createNote(
                            CreateNoteRequest(note.title, note.description)
                        )
                        val newEntity = response.toNoteEntity()
                        noteDao.deleteNote(note)
                        noteDao.insertNote(newEntity)
                    }
                }
            } catch (_: Exception) {
                // Ignore network failures; they'll be retried next refresh
            }
        }
    }
}
