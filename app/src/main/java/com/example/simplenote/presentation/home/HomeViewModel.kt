package com.example.simplenote.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository // Inject AuthRepository to check login status
) : ViewModel() {

    private val _notes = MutableStateFlow<Resource<List<Note>>>(Resource.Loading())
    val notes: StateFlow<Resource<List<Note>>> = _notes

    // SharedFlow to signal navigation to AuthActivity on authentication errors
    private val _navigateToAuth = MutableSharedFlow<Unit>()
    val navigateToAuth: SharedFlow<Unit> = _navigateToAuth

    private var currentQuery = ""

    init {
        // Load notes from local cache immediately to provide initial data
        loadNotesFromLocal()
        // Then attempt to refresh notes from the network, but only if logged in
        viewModelScope.launch {
            if (authRepository.isLoggedIn().first()) {
                refreshNotes()
            } else {
                // If not logged in, ensure we only show cached notes or empty state
                // and signal for potential re-authentication if the app somehow landed here unauthenticated
                _notes.value = Resource.Error("User not logged in.", _notes.value.data)
                _navigateToAuth.emit(Unit) // Signal to navigate to AuthActivity
            }
        }
    }

    /**
     * Loads notes from the local database. This should always be attempted
     * regardless of network status or authentication, providing offline support.
     */
    private fun loadNotesFromLocal() {
        if (currentQuery.isEmpty()) {
            noteRepository.getAllNotes().onEach { resource ->
                _notes.value = resource
                // Check if the resource is an error and if it's an authentication issue
                if (resource is Resource.Error && !authRepository.isLoggedIn().first()) {
                    _navigateToAuth.emit(Unit) // Signal to navigate to AuthActivity
                }
            }.launchIn(viewModelScope)
        } else {
            noteRepository.searchNotes(currentQuery).onEach { resource ->
                _notes.value = resource
                // Check if the resource is an error and if it's an authentication issue
                if (resource is Resource.Error && !authRepository.isLoggedIn().first()) {
                    _navigateToAuth.emit(Unit) // Signal to navigate to AuthActivity
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Initiates a search for notes.
     * @param query The search query string.
     */
    fun searchNotes(query: String) {
        currentQuery = query
        loadNotesFromLocal() // Search in local cache
        // Network search for notes is handled by refreshNotes, which is triggered periodically or manually.
    }

    /**
     * Refreshes notes from the remote API. This operation requires authentication.
     */
    fun refreshNotes() {
        viewModelScope.launch {
            // Only attempt to refresh notes if the user is logged in
            if (authRepository.isLoggedIn().first()) {
                val result = noteRepository.refreshNotes()
                if (result is Resource.Error) {
                    // If refresh fails (e.g., due to token expiry), notify but keep local data
                    _notes.value = Resource.Error(result.message ?: "Failed to refresh notes", _notes.value.data)
                    // If the error is due to authentication, signal redirection
                    if (!authRepository.isLoggedIn().first()) {
                        _navigateToAuth.emit(Unit)
                    }
                }
            } else {
                // If not logged in, do not attempt to refresh from network
                _notes.value = Resource.Error("Not logged in to refresh notes.", _notes.value.data)
                _navigateToAuth.emit(Unit) // Signal to navigate to AuthActivity
            }
        }
    }
}
