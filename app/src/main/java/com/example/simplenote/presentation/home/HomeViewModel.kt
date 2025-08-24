package com.example.simplenote.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _query = MutableStateFlow<String?>(null)

    /** Expose paginated notes.  Changing the query triggers a new paging source. */
    val notes: Flow<PagingData<Note>> = _query
        .debounce(300) // reduce search churn
        .distinctUntilChanged()
        .flatMapLatest { query ->
            noteRepository.getPagedNotes(query)
        }
        .cachedIn(viewModelScope)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _navigateToAuth = MutableSharedFlow<Unit>()
    val navigateToAuth: SharedFlow<Unit> = _navigateToAuth

    init {
        viewModelScope.launch {
            if (authRepository.isLoggedIn().first()) {
                refreshNotes()
            } else {
                _error.value = "User not logged in."
                _navigateToAuth.emit(Unit)
            }
        }
    }

    /** Call this on text changes to filter notes. */
    fun searchNotes(query: String) {
        _query.value = if (query.isBlank()) null else query
    }

    /** Refresh notes from the server and update local DB. */
    fun refreshNotes() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn().first()) {
                _error.value = "Not logged in to refresh notes."
                _navigateToAuth.emit(Unit)
                return@launch
            }
            _isRefreshing.value = true
            when (val result = noteRepository.refreshNotes()) {
                is Resource.Error -> _error.value = result.message ?: "Failed to refresh notes"
                else -> { /* success */ }
            }
            _isRefreshing.value = false
        }
    }

    /** Delete a note.  On success the paging source picks up changes from DB. */
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            when (noteRepository.deleteNote(id)) {
                is Resource.Error -> _error.value = "Failed to delete note"
                else -> { /* success */ }
            }
        }
    }
}
