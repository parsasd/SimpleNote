package com.example.simplenote.data.local.dao

import androidx.room.*
import com.example.simplenote.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    /** Get all non-deleted notes, ordered by most recently updated. */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /** Get a single non-deleted note by ID. */
    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    suspend fun getNoteById(id: Int): NoteEntity?

    /** Search title/description for non-deleted notes. */
    @Query(
        "SELECT * FROM notes " +
                "WHERE (title LIKE :query OR description LIKE :query) AND isDeleted = 0 " +
                "ORDER BY updatedAt DESC"
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    /** Return all locally changed notes (unsynced or marked deleted). */
    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<NoteEntity>
}
