// File: presentation/note/detail/NoteDetailFragment.kt
package com.example.simplenote.presentation.note.detail

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.R
import com.example.simplenote.databinding.FragmentNoteDetailBinding // Added import
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.utils.Resource
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val bundle = Bundle().apply {
                        putInt("noteId", noteId)
                    }
                    findNavController().navigate(
                        R.id.action_noteDetailFragment_to_addEditNoteFragment,
                        bundle
                    )
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
        viewModel.loadNote(noteId)

        lifecycleScope.launch {
            viewModel.noteState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE

                        resource.data?.let { note ->
                            binding.tvTitle.text = note.title
                            binding.tvDescription.text = note.description
                            binding.tvLastEdited.text = "Last edited on ${
                                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    .format(note.updatedAt)
                            }"
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deleteState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
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
