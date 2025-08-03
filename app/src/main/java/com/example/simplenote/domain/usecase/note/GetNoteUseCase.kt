package com.example.simplenote.domain.usecase.note

import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Resource<Note> {
        return noteRepository.getNote(id)
    }
}
