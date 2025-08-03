package com.example.simplenote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.local.dao.UserDao
import com.example.simplenote.data.local.entities.NoteEntity
import com.example.simplenote.data.local.entities.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SimpleNoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
}