package com.example.simplenote.data.remote.api

import com.example.simplenote.data.remote.dto.*
import retrofit2.http.*
import retrofit2.Response

interface SimpleNoteApi {

    // Auth endpoints
    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/token/")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/auth/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AccessTokenResponse

    @GET("api/auth/userinfo/")
    suspend fun getUserInfo(): UserInfoResponse

    // Notes endpoints
    @GET("api/notes/")
    suspend fun getNotes(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): NotesListResponse

    @POST("api/notes/")
    suspend fun createNote(@Body request: CreateNoteRequest): NoteResponse

    @GET("api/notes/{id}/")
    suspend fun getNote(@Path("id") id: Int): NoteResponse

    @PUT("api/notes/{id}/")
    suspend fun updateNote(
        @Path("id") id: Int,
        @Body request: UpdateNoteRequest
    ): NoteResponse

    @PATCH("api/notes/{id}/")
    suspend fun partialUpdateNote(
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): NoteResponse

    @POST("api/auth/change-password/")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Response<Unit>

    @DELETE("api/notes/{id}/")
    suspend fun deleteNote(@Path("id") id: Int)

    @GET("api/notes/filter")
    suspend fun filterNotes(
        @Query("title") title: String? = null,
        @Query("description") description: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): NotesListResponse
}