package com.example.simplenote.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplenote.R
import com.example.simplenote.databinding.FragmentHomeBinding
import com.example.simplenote.domain.model.Note
import com.example.simplenote.presentation.auth.AuthActivity
import com.example.simplenote.presentation.base.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import com.example.simplenote.utils.Resource

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
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
        notesAdapter = NotesAdapter(
            onItemClick = { note ->
                val bundle = Bundle().apply {
                    putInt("noteId", note.id)
                }
                findNavController().navigate(
                    R.id.action_homeFragment_to_noteDetailFragment,
                    bundle
                )
            },
            onItemLongClick = { note, anchorView ->
                showNoteOptionsMenu(note, anchorView)
            }
        )
        binding.rvNotes.adapter = notesAdapter
        binding.rvNotes.layoutManager = LinearLayoutManager(context)
    }

    private fun showNoteOptionsMenu(note: Note, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_note_detail, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val bundle = Bundle().apply { putInt("noteId", note.id) }
                    findNavController().navigate(
                        R.id.action_homeFragment_to_addEditNoteFragment,
                        bundle
                    )
                    true
                }
                R.id.action_delete -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteNote(note.id)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun observeData() {
        // Use launchWhenStarted to ensure collectors are canceled when the view is destroyed.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.navigateToAuth.collect {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }
    }
}
