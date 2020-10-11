/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shreyaspatil.noty.view.notes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.databinding.ItemNoteBinding
import kotlinx.android.synthetic.main.item_note.view.*

class NotesListAdapter(
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Note, NotesListAdapter.NoteViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteViewHolder(
        ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), onNoteClick)
    }

    inner class NoteViewHolder(itemView: ItemNoteBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(note: Note, onNoteClick: (Note) -> Unit) {
            itemView.run {
                textTitle.text = note.title
                textNote.text = note.note
                rootView.setOnClickListener { onNoteClick(note) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        }
    }
}
