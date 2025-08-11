package com.example.simplenote.presentation.note.addedit

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
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    sealed class NoteLoadState {
        object Idle : NoteLoadState()
        object Loading : NoteLoadState()
        data class Success(val note: Note) : NoteLoadState()
        data class Error(val message: String) : NoteLoadState()
    }

    sealed class NoteSaveState {
        object Idle : NoteSaveState()
        object Loading : NoteSaveState()
        object Success : NoteSaveState()
        data class Error(val message: String) : NoteSaveState()
    }

    private val _noteLoadState = MutableStateFlow<NoteLoadState>(NoteLoadState.Idle)
    val noteLoadState: StateFlow<NoteLoadState> = _noteLoadState

    private val _noteSaveState = MutableStateFlow<NoteSaveState>(NoteSaveState.Idle)
    val noteSaveState: StateFlow<NoteSaveState> = _noteSaveState

    fun loadNote(id: Int) {
        if (id == 0) return
        viewModelScope.launch {
            _noteLoadState.value = NoteLoadState.Loading
            when (val result = noteRepository.getNote(id)) {
                is Resource.Success -> _noteLoadState.value = NoteLoadState.Success(result.data!!)
                is Resource.Error -> _noteLoadState.value = NoteLoadState.Error(result.message ?: "Failed to load note")
                else -> { /* No-op */ }
            }
        }
    }

    fun saveNote(id: Int?, title: String, description: String) {
        viewModelScope.launch {
            _noteSaveState.value = NoteSaveState.Loading
            val result = if (id == null) {
                noteRepository.createNote(title, description)
            } else {
                noteRepository.updateNote(id, title, description)
            }

            when (result) {
                is Resource.Success -> _noteSaveState.value = NoteSaveState.Success
                is Resource.Error -> _noteSaveState.value = NoteSaveState.Error(result.message ?: "Failed to save note")
                else -> { /* No-op */ }
            }
        }
    }
}