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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notes =
        MutableStateFlow<Resource<List<Note>>>(Resource.Loading())
    val notes: StateFlow<Resource<List<Note>>> = _notes

    private val _navigateToAuth = MutableSharedFlow<Unit>()
    val navigateToAuth: SharedFlow<Unit> = _navigateToAuth

    private var currentQuery = ""

    init {
        // Always load local notes first
        loadNotesFromLocal()
        viewModelScope.launch {
            if (authRepository.isLoggedIn().first()) {
                refreshNotes()
            } else {
                _notes.value = Resource.Error("User not logged in.", _notes.value.data)
                _navigateToAuth.emit(Unit)
            }
        }
    }

    private fun loadNotesFromLocal() {
        if (currentQuery.isEmpty()) {
            noteRepository.getAllNotes().onEach { resource ->
                _notes.value = resource
            }.launchIn(viewModelScope)
        } else {
            noteRepository.searchNotes(currentQuery).onEach { resource ->
                _notes.value = resource
            }.launchIn(viewModelScope)
        }
    }

    fun searchNotes(query: String) {
        currentQuery = query
        loadNotesFromLocal()
    }

    fun refreshNotes() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn().first()) {
                val result = noteRepository.refreshNotes()
                if (result is Resource.Error) {
                    _notes.value = Resource.Error(result.message ?: "Failed to refresh notes", _notes.value.data)
                    if (!authRepository.isLoggedIn().first()) {
                        _navigateToAuth.emit(Unit)
                    }
                }
            } else {
                _notes.value = Resource.Error("Not logged in to refresh notes.", _notes.value.data)
                _navigateToAuth.emit(Unit)
            }
        }
    }

    // New: Deletes a note and updates the local list.
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            when (noteRepository.deleteNote(id)) {
                is Resource.Success -> {
                    // Refresh local data after deletion without hitting the network
                    currentQuery = ""
                    loadNotesFromLocal()
                }
                is Resource.Error -> {
                    _notes.value = Resource.Error("Failed to delete note", _notes.value.data)
                }
                else -> {}
            }
        }
    }
}
