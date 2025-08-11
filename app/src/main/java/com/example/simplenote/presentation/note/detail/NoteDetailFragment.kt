package com.example.simplenote.presentation.note.detail

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.R
import com.example.simplenote.databinding.FragmentNoteDetailBinding
import com.example.simplenote.presentation.base.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment<FragmentNoteDetailBinding>() {

    private val viewModel: NoteDetailViewModel by viewModels()
    private var noteId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        noteId = arguments?.getInt("noteId") ?: 0
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNoteDetailBinding.inflate(inflater, container, false)

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val bundle = Bundle().apply { putInt("noteId", noteId) }
                    findNavController().navigate(R.id.action_noteDetailFragment_to_addEditNoteFragment, bundle)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    override fun observeData() {
        if (noteId != 0) {
            viewModel.loadNote(noteId)
        }

        lifecycleScope.launch {
            viewModel.noteState.collect { state ->
                binding.progressBar.isVisible = state is NoteDetailViewModel.NoteDetailState.Loading
                binding.contentLayout.isVisible = state is NoteDetailViewModel.NoteDetailState.Success

                if (state is NoteDetailViewModel.NoteDetailState.Success) {
                    val note = state.note
                    binding.tvTitle.text = note.title
                    binding.tvDescription.text = note.description
                    binding.tvLastEdited.text = "Last edited on ${
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(note.updatedAt)
                    }"
                } else if (state is NoteDetailViewModel.NoteDetailState.Error) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                if (state is NoteDetailViewModel.DeleteState.Success) {
                    Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else if (state is NoteDetailViewModel.DeleteState.Error) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNote(noteId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}