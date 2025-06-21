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

package dev.shreyaspatil.noty.api.controller

import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.exception.ResourceNotFoundException
import dev.shreyaspatil.noty.api.exception.UnauthorizedAccessException
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.response.Note
import dev.shreyaspatil.noty.api.model.response.NoteTaskResponse
import dev.shreyaspatil.noty.api.model.response.NotesResponse
import dev.shreyaspatil.noty.data.dao.NoteDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller for notes management
 */
@Singleton
class NotesController @Inject constructor(private val noteDao: NoteDao) {

    /**
     * Get all notes for a specific user
     */
    fun getNotesByUser(userId: String): NotesResponse {
        val notes = noteDao.getAllByUser(userId)

        return NotesResponse(
            notes.map {
                Note(
                    id = it.id,
                    title = it.title,
                    note = it.note,
                    created = it.created,
                    isPinned = it.isPinned,
                )
            },
        )
    }

    /**
     * Add a new note
     */
    fun addNote(userId: String, note: NoteRequest): NoteTaskResponse {
        val noteTitle = note.title.trim()
        val noteText = note.note.trim()

        return withValidatedNote(noteTitle, noteText) {
            val noteId = noteDao.add(userId = userId, title = noteTitle, note = noteText)
            NoteTaskResponse(noteId)
        }
    }

    /**
     * Update an existing note
     */
    fun updateNote(userId: String, noteId: String, note: NoteRequest): NoteTaskResponse {
        val noteTitle = note.title.trim()
        val noteText = note.note.trim()

        return withValidatedNote(noteTitle, noteText) {
            withExistingNote(noteId) {
                withAuthorizedUser(userId, noteId) {
                    val id = noteDao.update(noteId, noteTitle, noteText)
                    NoteTaskResponse(id)
                }
            }
        }
    }

    /**
     * Delete a note
     */
    fun deleteNote(userId: String, noteId: String): NoteTaskResponse {
        return withExistingNote(noteId) {
            withAuthorizedUser(userId, noteId) {
                if (!noteDao.deleteById(noteId)) {
                    error("Error occurred while deleting a note")
                }
                NoteTaskResponse(noteId)
            }
        }
    }

    /**
     * Update pin status of a note
     */
    fun pinNote(userId: String, noteId: String): NoteTaskResponse {
        return updateNotePin(userId, noteId, true)
    }

    fun unpinNote(userId: String, noteId: String): NoteTaskResponse {
        return updateNotePin(userId, noteId, false)
    }

    private fun updateNotePin(
        userId: String,
        noteId: String,
        isPinned: Boolean
    ): NoteTaskResponse {
        return withExistingNote(noteId) {
            return@withExistingNote withAuthorizedUser(userId, noteId) {
                val id = noteDao.updateNotePinById(id = noteId, isPinned = isPinned)
                return@withAuthorizedUser NoteTaskResponse(id)
            }
        }
    }

    /**
     * Higher-order function to validate note content
     */
    private fun <T> withValidatedNote(title: String, note: String, block: () -> T): T {
        validateNoteOrThrowException(title, note)
        return block()
    }

    /**
     * Higher-order function to check if note exists
     */
    private fun <T> withExistingNote(noteId: String, block: () -> T): T {
        val exists = runCatching { noteDao.exists(noteId) }.getOrDefault(false)
        if (!exists) {
            throw ResourceNotFoundException("Note not exist with ID '$noteId'")
        }
        return block()
    }

    /**
     * Higher-order function to check if user is authorized to access the note
     */
    private fun <T> withAuthorizedUser(userId: String, noteId: String, block: () -> T): T {
        if (!noteDao.isNoteOwnedByUser(noteId, userId)) {
            throw UnauthorizedAccessException(FailureMessages.MESSAGE_ACCESS_DENIED)
        }
        return block()
    }

    /**
     * Validate note content or throw exception
     */
    private fun validateNoteOrThrowException(title: String, note: String) {
        val message = when {
            (title.isBlank() or note.isBlank()) -> "Title and Note should not be blank"
            (title.length !in (4..30)) -> "Title should be of min 4 and max 30 character in length"
            else -> return
        }

        throw BadRequestException(message)
    }
}
