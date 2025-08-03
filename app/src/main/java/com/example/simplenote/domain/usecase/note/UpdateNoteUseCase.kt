package com.example.simplenote.domain.usecase.note

import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(id: Int, title: String, description: String): Resource<Note> {
        return noteRepository.updateNote(id, title, description)
    }
}
