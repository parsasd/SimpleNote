package com.example.simplenote.presentation.note.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    sealed class NoteDetailState {
        object Idle : NoteDetailState()
        object Loading : NoteDetailState()
        data class Success(val note: Note) : NoteDetailState()
        data class Error(val message: String) : NoteDetailState()
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object Loading : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

    private val _noteState = MutableStateFlow<NoteDetailState>(NoteDetailState.Idle)
    val noteState: StateFlow<NoteDetailState> = _noteState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    fun loadNote(id: Int) {
        viewModelScope.launch {
            _noteState.value = NoteDetailState.Loading
            when (val result = noteRepository.getNote(id)) {
                is Resource.Success -> _noteState.value = NoteDetailState.Success(result.data!!)
                is Resource.Error -> _noteState.value = NoteDetailState.Error(result.message ?: "Failed to load note")
                else -> { /* No-op */ }
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            when (noteRepository.deleteNote(id)) {
                is Resource.Success -> _deleteState.value = DeleteState.Success
                is Resource.Error -> _deleteState.value = DeleteState.Error("Failed to delete note")
                else -> { /* No-op */ }
            }
        }
    }
}