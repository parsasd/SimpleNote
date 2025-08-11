package com.example.simplenote.presentation.note.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentAddEditNoteBinding
import com.example.simplenote.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment<FragmentAddEditNoteBinding>() {

    private val viewModel: AddEditNoteViewModel by viewModels()
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = it.getInt("noteId", 0)
            if (id != 0) {
                noteId = id
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAddEditNoteBinding.inflate(inflater, container, false)

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.toolbar.title = if (noteId == null) "Add Note" else "Edit Note"
        noteId?.let { viewModel.loadNote(it) }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            if (validateInput(title, description)) {
                viewModel.saveNote(noteId, title, description)
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            viewModel.noteLoadState.collect { state ->
                binding.progressBar.isVisible = state is AddEditNoteViewModel.NoteLoadState.Loading
                binding.btnSave.isEnabled = state !is AddEditNoteViewModel.NoteLoadState.Loading

                when (state) {
                    is AddEditNoteViewModel.NoteLoadState.Success -> {
                        binding.etTitle.setText(state.note.title)
                        binding.etDescription.setText(state.note.description)
                    }
                    is AddEditNoteViewModel.NoteLoadState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.noteSaveState.collect { state ->
                binding.progressBar.isVisible = state is AddEditNoteViewModel.NoteSaveState.Loading
                binding.btnSave.isEnabled = state !is AddEditNoteViewModel.NoteSaveState.Loading

                when (state) {
                    is AddEditNoteViewModel.NoteSaveState.Success -> {
                        val message = if (noteId == null) "Note created successfully" else "Note updated successfully"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is AddEditNoteViewModel.NoteSaveState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun validateInput(title: String, description: String): Boolean {
        binding.tilTitle.error = if (title.isBlank()) "Title is required" else null
        binding.tilDescription.error = if (description.isBlank()) "Description is required" else null
        return title.isNotBlank() && description.isNotBlank()
    }
}