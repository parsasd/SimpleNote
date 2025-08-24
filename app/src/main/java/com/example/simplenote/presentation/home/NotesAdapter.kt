package com.example.simplenote.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simplenote.databinding.ItemNoteBinding
import com.example.simplenote.domain.model.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note, View) -> Unit = { _, _ -> }
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            with(binding) {
                tvTitle.text = note.title
                tvDescription.text = note.description
                tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(note.updatedAt)

                root.setOnClickListener {
                    onItemClick(note)
                }
                root.setOnLongClickListener {
                    onItemLongClick(note, root)
                    true
                }
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
