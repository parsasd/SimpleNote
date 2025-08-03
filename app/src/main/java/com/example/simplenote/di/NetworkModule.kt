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
import javax.inject.Singleton

// Custom qualifier for Retrofit instances that do NOT use the AuthInterceptor/TokenAuthenticator
// This is used for the refresh token API call within the TokenAuthenticator itself to prevent circular dependencies.
// The annotation class UnauthorizedApi is defined in its own file (UnauthorizedApi.kt)
// and should not be redeclared here.

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
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @UnauthorizedApi
    fun provideUnauthorizedOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSimpleNoteApi(okHttpClient: OkHttpClient): SimpleNoteApi {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.245:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimpleNoteApi::class.java)
    }

    @Provides
    @Singleton
    @UnauthorizedApi
    fun provideUnauthorizedSimpleNoteApi(@UnauthorizedApi okHttpClient: OkHttpClient): SimpleNoteApi {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.245:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimpleNoteApi::class.java)
    }
}
