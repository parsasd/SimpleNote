// File: presentation/note/addedit/AddEditNoteViewModel.kt
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
import java.util.Date // Added import

import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _noteState = MutableStateFlow<Resource<Note>>(Resource.Success(
        Note(0, "", "", Date(), Date(), "", "")
    ))
    val noteState: StateFlow<Resource<Note>> = _noteState

    private val _saveState = MutableStateFlow<Resource<Note>>(Resource.Success(
        Note(0, "", "", Date(), Date(), "", "")
    ))
    val saveState: StateFlow<Resource<Note>> = _saveState

    fun loadNote(id: Int) {
        viewModelScope.launch {
            _noteState.value = Resource.Loading()
            _noteState.value = noteRepository.getNote(id)
        }
    }

    fun createNote(title: String, description: String) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            _saveState.value = noteRepository.createNote(title, description)
        }
    }

    fun updateNote(id: Int, title: String, description: String) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            _saveState.value = noteRepository.updateNote(id, title, description)
        }
    }
}
