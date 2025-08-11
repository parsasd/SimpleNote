package com.example.simplenote.di

import android.content.Context
import androidx.room.Room
import com.example.simplenote.data.local.PreferencesManager
import com.example.simplenote.data.local.database.SimpleNoteDatabase
import com.example.simplenote.data.remote.api.SimpleNoteApi
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
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSimpleNoteDatabase(@ApplicationContext context: Context): SimpleNoteDatabase {
        return Room.databaseBuilder(context, SimpleNoteDatabase::class.java, "simplenote_db")
            .fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: SimpleNoteDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideUserDao(database: SimpleNoteDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideAuthRepository(
        @Named("authorizedApi") api: SimpleNoteApi,
        userDao: UserDao,
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, userDao, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        @Named("authorizedApi") api: SimpleNoteApi,
        noteDao: NoteDao
    ): NoteRepository {
        return NoteRepositoryImpl(api, noteDao)
    }
}