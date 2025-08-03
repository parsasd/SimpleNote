package com.example.simplenote.domain.usecase.note

import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<Note>>> {
        return noteRepository.searchNotes(query)
    }
}
