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
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.model.response.Note
import dev.shreyaspatil.noty.api.model.response.NoteTaskResponse
import dev.shreyaspatil.noty.api.model.response.NotesResponse
import dev.shreyaspatil.noty.data.dao.NoteDao
import dev.shreyaspatil.noty.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller for notes management
 */
@Singleton
class NotesController @Inject constructor(private val noteDao: NoteDao) {

    fun getNotesByUser(user: User): NotesResponse {
        val notes = noteDao.getAllByUser(user.id)

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

    fun addNote(user: User, note: NoteRequest): NoteTaskResponse {
        val noteTitle = note.title.trim()
        val noteText = note.note.trim()

        validateNoteOrThrowException(noteTitle, noteText)

        val noteId = noteDao.add(userId = user.id, title = noteTitle, note = noteText)
        return NoteTaskResponse(noteId)
    }

    fun updateNote(user: User, noteId: String, note: NoteRequest): NoteTaskResponse {
        val noteTitle = note.title.trim()
        val noteText = note.note.trim()

        validateNoteOrThrowException(noteTitle, noteText)
        checkNoteExistsOrThrowException(noteId)
        checkOwnerOrThrowException(user.id, noteId)

        val id = noteDao.update(noteId, noteTitle, noteText)
        return NoteTaskResponse(id)
    }

    fun deleteNote(user: User, noteId: String): NoteTaskResponse {
        checkNoteExistsOrThrowException(noteId)
        checkOwnerOrThrowException(user.id, noteId)

        if (!noteDao.deleteById(noteId)) {
            error("Error occurred while deleting a note")
        }

        return NoteTaskResponse(noteId)
    }

    fun updateNotePin(user: User, noteId: String, pinRequest: PinRequest): NoteTaskResponse {
        checkNoteExistsOrThrowException(noteId)
        checkOwnerOrThrowException(user.id, noteId)
        val id = noteDao.updateNotePinById(noteId, pinRequest.isPinned)
        return NoteTaskResponse(id)
    }

    private fun checkNoteExistsOrThrowException(noteId: String) {
        if (!noteDao.exists(noteId)) {
            throw ResourceNotFoundException("Note not exist with ID '$noteId'")
        }
    }

    private fun checkOwnerOrThrowException(userId: String, noteId: String) {
        if (!noteDao.isNoteOwnedByUser(noteId, userId)) {
            throw UnauthorizedAccessException(FailureMessages.MESSAGE_ACCESS_DENIED)
        }
    }

    private fun validateNoteOrThrowException(title: String, note: String) {
        val message = when {
            (title.isBlank() or note.isBlank()) -> "Title and Note should not be blank"
            (title.length !in (4..30)) -> "Title should be of min 4 and max 30 character in length"
            else -> return
        }

        throw BadRequestException(message)
    }
}
