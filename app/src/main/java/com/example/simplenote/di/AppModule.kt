package com.example.simplenote.di

import android.content.Context
import androidx.room.Room
import com.example.simplenote.data.local.PreferencesManager
import com.example.simplenote.data.local.database.SimpleNoteDatabase
import com.example.simplenote.data.remote.api.SimpleNoteApi // Keep this import as it's used for repositories
import com.example.simplenote.data.local.dao.UserDao
import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.repository.AuthRepositoryImpl
import com.example.simplenote.data.repository.NoteRepositoryImpl
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
// Removed imports related to OkHttpClient, Retrofit, etc., as they are now handled in NetworkModule

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSimpleNoteDatabase(
        @ApplicationContext context: Context
    ): SimpleNoteDatabase {
        return Room.databaseBuilder(
            context,
            SimpleNoteDatabase::class.java,
            "simplenote_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: SimpleNoteDatabase) = database.noteDao()

    @Provides
    @Singleton
    fun provideUserDao(database: SimpleNoteDatabase) = database.userDao()

    // The provideOkHttpClient method has been moved to NetworkModule.kt
    // @Provides
    // @Singleton
    // fun provideOkHttpClient(...) { ... }

    // The provideSimpleNoteApi method has been moved to NetworkModule.kt
    // @Provides
    // @Singleton
    // fun provideSimpleNoteApi(...) { ... }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: SimpleNoteApi, // SimpleNoteApi is now provided by NetworkModule
        userDao: UserDao,
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, userDao, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        api: SimpleNoteApi, // SimpleNoteApi is now provided by NetworkModule
        noteDao: NoteDao
    ): NoteRepository {
        return NoteRepositoryImpl(api, noteDao)
    }
}
