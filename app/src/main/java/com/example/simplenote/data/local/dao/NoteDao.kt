package com.example.simplenote.data.local.dao

import androidx.room.*
import com.example.simplenote.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource // <-- new import

@Dao
interface NoteDao {
    /** Get all non‑deleted notes, ordered by most recently updated. */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /** Paging source for all notes; used by Paging library. */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun pagingSource(): PagingSource<Int, NoteEntity>

    /** Get a single non‑deleted note by ID. */
    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    suspend fun getNoteById(id: Int): NoteEntity?

    /** Search title/description for non‑deleted notes (full list). */
    @Query(
        "SELECT * FROM notes " +
                "WHERE (title LIKE :query OR description LIKE :query) AND isDeleted = 0 " +
                "ORDER BY updatedAt DESC"
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    /** Paging search; wrap query with '%' when calling. */
    @Query(
        "SELECT * FROM notes " +
                "WHERE (title LIKE :query OR description LIKE :query) AND isDeleted = 0 " +
                "ORDER BY updatedAt DESC"
    )
    fun searchPagingSource(query: String): PagingSource<Int, NoteEntity>

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
