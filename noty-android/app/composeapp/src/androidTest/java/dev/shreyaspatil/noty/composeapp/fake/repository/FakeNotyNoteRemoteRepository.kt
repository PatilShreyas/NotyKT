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

package dev.shreyaspatil.noty.composeapp.fake.repository

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

/**
 * Fake implementation for remote note repository.
 *
 * This stores notes in memory
 */
class FakeNotyNoteRemoteRepository @Inject constructor() : NotyNoteRepository {
    val notes = mutableMapOf<String, Note>()
    val allNotes = MutableStateFlow(notes.values.toList())

    override fun getNoteById(noteId: String): Flow<Note> {
        return flowOf(notes[noteId]).filterNotNull()
    }

    override fun getAllNotes(): Flow<Either<List<Note>>> {
        return allNotes.map { Either.success(it) }
    }

    override suspend fun addNote(title: String, note: String): Either<String> {
        val id = UUID.randomUUID().toString()
        val newNote = Note(
            id = id,
            title = title,
            note = note,
            created = System.currentTimeMillis()
        )
        notes[id] = newNote
        refreshNotes()

        return Either.success(id)
    }

    override suspend fun addNotes(notes: List<Note>) {
        notes.forEach { newNote -> this.notes[newNote.id] = newNote }
        refreshNotes()
    }

    override suspend fun updateNote(noteId: String, title: String, note: String): Either<String> {
        val existingNote = notes[noteId] ?: return Either.error("Note not exist")
        val updatedNote = existingNote.copy(title = title, note = note)
        notes[noteId] = updatedNote
        refreshNotes()

        return Either.error(noteId)
    }

    override suspend fun deleteNote(noteId: String): Either<String> {
        val deleted = notes.remove(noteId)
        refreshNotes()

        return if (deleted != null) Either.success(deleted.id) else Either.error("Not exist")
    }

    override suspend fun deleteAllNotes() {
        notes.clear()
        refreshNotes()
    }

    override suspend fun updateNoteId(oldNoteId: String, newNoteId: String) {
        val oldNote = notes.remove(oldNoteId)
        if (oldNote != null) {
            val updatedNote = oldNote.copy(id = newNoteId)
            notes[newNoteId] = updatedNote
        }
        refreshNotes()
    }

    private fun refreshNotes() {
        allNotes.value = notes.values.toList()
    }
}
