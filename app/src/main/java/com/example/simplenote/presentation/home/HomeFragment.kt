package com.example.simplenote.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplenote.R
import com.example.simplenote.databinding.FragmentHomeBinding
import com.example.simplenote.presentation.auth.AuthActivity
import com.example.simplenote.presentation.base.BaseFragment
import com.example.simplenote.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun setupViews() {
        setupRecyclerView()

        // Set up click listener for the Floating Action Button to add a new note
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditNoteFragment)
        }

        // Set up text change listener for the search input field
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.searchNotes(text.toString())
        }

        // Set up refresh listener for the SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshNotes()
        }
    }

    /**
     * Initializes and sets up the RecyclerView for displaying notes.
     */
    private fun setupRecyclerView() {
        // Initialize the adapter with a click listener for individual notes
        notesAdapter = NotesAdapter { note ->
            // Create a bundle to pass the note ID to the NoteDetailFragment
            val bundle = Bundle().apply {
                putInt("noteId", note.id)
            }
            // Navigate to the NoteDetailFragment
            findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
        }

        // Apply the adapter and layout manager to the RecyclerView
        binding.rvNotes.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun observeData() {
        // Observe the notes flow from the ViewModel
        lifecycleScope.launch {
            viewModel.notes.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show progress bar and hide other views when loading
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvNotes.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        // Hide progress bar and stop refresh animation on success
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false

                        // Update the RecyclerView with the list of notes
                        resource.data?.let { notes ->
                            if (notes.isEmpty()) {
                                // Show empty state message if no notes are available
                                binding.tvEmptyState.visibility = View.VISIBLE
                                binding.rvNotes.visibility = View.GONE
                            } else {
                                // Show notes and hide empty state
                                binding.tvEmptyState.visibility = View.GONE
                                binding.rvNotes.visibility = View.VISIBLE
                                notesAdapter.submitList(notes)
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Hide progress bar and stop refresh animation on error
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        // Show error message to the user
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()

                        // Even on error, if some data was previously loaded, display it
                        resource.data?.let { notes ->
                            if (notes.isEmpty()) {
                                binding.tvEmptyState.visibility = View.VISIBLE
                                binding.rvNotes.visibility = View.GONE
                            } else {
                                binding.tvEmptyState.visibility = View.GONE
                                binding.rvNotes.visibility = View.VISIBLE
                                notesAdapter.submitList(notes)
                            }
                        }
                    }
                }
            }
        }

        // --- IMPORTANT ADDITION FOR NAVIGATION ---
        // Observe the navigateToAuth flow from the ViewModel.
        // This will trigger navigation to AuthActivity if authentication issues are detected
        // by the ViewModel (e.g., after a failed token refresh).
        lifecycleScope.launch {
            viewModel.navigateToAuth.collect {
                // Start AuthActivity and finish MainActivity to prevent going back
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }
    }
}
