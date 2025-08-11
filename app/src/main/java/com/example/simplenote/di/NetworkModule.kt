package com.example.simplenote.di

import com.example.simplenote.data.local.PreferencesManager
import com.example.simplenote.data.remote.api.SimpleNoteApi
import com.example.simplenote.data.remote.interceptors.AuthInterceptor
import com.example.simplenote.data.remote.interceptors.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): AuthInterceptor {
        return AuthInterceptor(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        preferencesManager: PreferencesManager,
        @Named("unauthorizedApi") api: SimpleNoteApi
    ): TokenAuthenticator {
        return TokenAuthenticator(preferencesManager, api)
    }

    @Provides
    @Singleton
    @Named("authorizedOkHttpClient")
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("unauthorizedOkHttpClient")
    fun provideUnauthorizedOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("authorizedApi")
    fun provideSimpleNoteApi(@Named("authorizedOkHttpClient") okHttpClient: OkHttpClient): SimpleNoteApi {
        return Retrofit.Builder()
            .baseUrl("http:/172.20.10.4:8000/") // <-- IMPORTANT: Make sure this is your computer's IP address
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimpleNoteApi::class.java)
    }

    @Provides
    @Singleton
    @Named("unauthorizedApi")
    fun provideUnauthorizedSimpleNoteApi(@Named("unauthorizedOkHttpClient") okHttpClient: OkHttpClient): SimpleNoteApi {
        return Retrofit.Builder()
            .baseUrl("http://172.20.10.4:8000/") // <-- IMPORTANT: Make sure this is your computer's IP address
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimpleNoteApi::class.java)
    }
}