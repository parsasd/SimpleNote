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

    override fun onResume() {
        super.onResume()
        // Refresh notes every time the screen is shown
        viewModel.refreshNotes()
    }

    override fun setupViews() {
        setupRecyclerView()

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEditNoteFragment)
        }

        binding.etSearch.doAfterTextChanged { text ->
            viewModel.searchNotes(text.toString())
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshNotes()
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter { note ->
            val bundle = Bundle().apply {
                putInt("noteId", note.id)
            }
            findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
        }

        binding.rvNotes.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvNotes.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
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
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToAuth.collect {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }
    }
}