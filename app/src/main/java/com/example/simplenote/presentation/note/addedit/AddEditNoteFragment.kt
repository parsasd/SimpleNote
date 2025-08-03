package com.example.simplenote.presentation.note.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentAddEditNoteBinding
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment<FragmentAddEditNoteBinding>() {

    private val viewModel: AddEditNoteViewModel by viewModels()
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve noteId from arguments, if it exists and is not 0
        noteId = arguments?.getInt("noteId")?.takeIf { it != 0 }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAddEditNoteBinding.inflate(inflater, container, false)

    override fun setupViews() {
        // Set up navigation icon click listener to go back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set toolbar title based on whether it's an add or edit operation
        noteId?.let { id ->
            binding.toolbar.title = "Edit Note"
            viewModel.loadNote(id) // Load note data if in edit mode
        } ?: run {
            binding.toolbar.title = "Add Note"
        }

        // Set up click listener for the save button
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()

            // Validate input before proceeding
            if (validateInput(title, description)) {
                if (noteId != null) {
                    viewModel.updateNote(noteId!!, title, description) // Update existing note
                } else {
                    viewModel.createNote(title, description) // Create new note
                }
            }
        }
    }

    override fun observeData() {
        // Observe the noteState for loading existing note data
        lifecycleScope.launch {
            viewModel.noteState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSave.isEnabled = false // Disable save button during loading
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true // Enable save button after loading

                        // Populate fields if data is available and it's an edit operation
                        resource.data?.let { note ->
                            if (noteId != null) { // Only set text if we are editing an existing note
                                binding.etTitle.setText(note.title)
                                binding.etDescription.setText(note.description)
                            }
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true // Enable save button on error
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        // If there's an error loading an existing note, navigate back
                        if (noteId != null) {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }

        // Observe the saveState for create/update note operations
        lifecycleScope.launch {
            viewModel.saveState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSave.isEnabled = false // Disable save button during saving
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true // Enable save button after saving
                        Toast.makeText(
                            context,
                            if (noteId != null) "Note updated successfully" else "Note created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp() // Navigate back after successful save
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true // Enable save button on error
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Validates the input fields (title and description).
     * @return true if inputs are valid, false otherwise.
     */
    private fun validateInput(title: String, description: String): Boolean {
        var isValid = true
        if (title.isEmpty()) {
            binding.tilTitle.error = "Title is required"
            isValid = false
        } else {
            binding.tilTitle.error = null
        }
        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            isValid = false
        } else {
            binding.tilDescription.error = null
        }
        return isValid
    }
}
