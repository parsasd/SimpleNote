package com.example.simplenote.domain.usecase.note

import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(title: String, description: String): Resource<Note> {
        return noteRepository.createNote(title, description)
    }
}
