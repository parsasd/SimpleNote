package com.example.simplenote.presentation.note.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.util.Locale

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment<FragmentNoteDetailBinding>() {

    private val viewModel: NoteDetailViewModel by viewModels()
    private var noteId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = arguments?.getInt("noteId") ?: 0
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNoteDetailBinding {
        return FragmentNoteDetailBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        // Inflate the toolbar menu programmatically so the Edit and Delete actions appear.
        binding.toolbar.inflateMenu(R.menu.menu_note_detail)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // Handle toolbar menu item clicks for editing and deleting notes.
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    // Navigate to AddEditNoteFragment with this note’s ID.
                    val bundle = Bundle().apply { putInt("noteId", noteId) }
                    findNavController().navigate(
                        R.id.action_noteDetailFragment_to_addEditNoteFragment,
                        bundle
                    )
                    true
                }
                R.id.action_delete -> {
                    // Show a confirmation dialog before deleting.
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    override fun observeData() {
        // If noteId is non-zero, load the note from either the API or local database.
        if (noteId != 0) {
            viewModel.loadNote(noteId)
        }

        // Observe the note details state to populate the UI and show/hide progress.
        lifecycleScope.launch {
            viewModel.noteState.collect { state ->
                binding.progressBar.isVisible = state is NoteDetailViewModel.NoteDetailState.Loading
                binding.contentLayout.isVisible = state is NoteDetailViewModel.NoteDetailState.Success

                when (state) {
                    is NoteDetailViewModel.NoteDetailState.Success -> {
                        val note = state.note
                        binding.tvTitle.text = note.title
                        binding.tvDescription.text = note.description
                        val formattedDate = SimpleDateFormat(
                            "MMM dd, yyyy HH:mm",
                            Locale.getDefault()
                        ).format(note.updatedAt)
                        binding.tvLastEdited.text = "Last edited on $formattedDate"
                    }
                    is NoteDetailViewModel.NoteDetailState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        // Observe the delete state to handle successful or failed deletions.
        lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                when (state) {
                    is NoteDetailViewModel.DeleteState.Success -> {
                        Toast.makeText(requireContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is NoteDetailViewModel.DeleteState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
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
