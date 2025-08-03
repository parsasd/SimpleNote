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
    
    private val _noteState = MutableStateFlow<Resource<Note>>(Resource.Loading())
    val noteState: StateFlow<Resource<Note>> = _noteState
    
    private val _deleteState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val deleteState: StateFlow<Resource<Unit>> = _deleteState
    
    fun loadNote(id: Int) {
        viewModelScope.launch {
            _noteState.value = Resource.Loading()
            _noteState.value = noteRepository.getNote(id)
        }
    }
    
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            _deleteState.value = noteRepository.deleteNote(id)
        }
    }
}
