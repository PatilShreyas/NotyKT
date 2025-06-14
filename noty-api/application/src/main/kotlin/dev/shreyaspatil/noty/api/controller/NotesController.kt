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
import dev.shreyaspatil.noty.api.exception.NoteNotFoundException
import dev.shreyaspatil.noty.api.exception.UnauthorizedActivityException
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.model.response.Note
import dev.shreyaspatil.noty.api.model.response.NoteResponse
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
        return try {
            val notes = noteDao.getAllByUser(user.id)

            NotesResponse.success(notes.map { Note(it.id, it.title, it.note, it.created, it.isPinned) })
        } catch (uae: UnauthorizedActivityException) {
            NotesResponse.unauthorized(uae.message)
        }
    }

    fun addNote(user: User, note: NoteRequest): NoteResponse {
        return try {
            val noteTitle = note.title.trim()
            val noteText = note.note.trim()

            validateNoteOrThrowException(noteTitle, noteText)

            val noteId = noteDao.add(user.id, noteTitle, noteText)
            NoteResponse.success(noteId)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        }
    }

    fun updateNote(user: User, noteId: String, note: NoteRequest): NoteResponse {
        return try {
            val noteTitle = note.title.trim()
            val noteText = note.note.trim()

            validateNoteOrThrowException(noteTitle, noteText)
            checkNoteExistsOrThrowException(noteId)
            checkOwnerOrThrowException(user.id, noteId)

            val id = noteDao.update(noteId, noteTitle, noteText)
            NoteResponse.success(id)
        } catch (uae: UnauthorizedActivityException) {
            NoteResponse.unauthorized(uae.message)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        } catch (nfe: NoteNotFoundException) {
            NoteResponse.notFound(nfe.message)
        }
    }

    fun deleteNote(user: User, noteId: String): NoteResponse {
        return try {
            checkNoteExistsOrThrowException(noteId)
            checkOwnerOrThrowException(user.id, noteId)

            if (noteDao.deleteById(noteId)) {
                NoteResponse.success(noteId)
            } else {
                NoteResponse.failed("Error Occurred")
            }
        } catch (uae: UnauthorizedActivityException) {
            NoteResponse.unauthorized(uae.message)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        } catch (nfe: NoteNotFoundException) {
            NoteResponse.notFound(nfe.message)
        }
    }

    fun updateNotePin(user: User, noteId: String, pinRequest: PinRequest): NoteResponse {
        return try {
            checkNoteExistsOrThrowException(noteId)
            checkOwnerOrThrowException(user.id, noteId)
            val id = noteDao.updateNotePinById(noteId, pinRequest.isPinned)
            NoteResponse.success(id)
        } catch (uae: UnauthorizedActivityException) {
            NoteResponse.unauthorized(uae.message)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        } catch (nfe: NoteNotFoundException) {
            NoteResponse.notFound(nfe.message)
        }
    }

    private fun checkNoteExistsOrThrowException(noteId: String) {
        if (!noteDao.exists(noteId)) {
            throw NoteNotFoundException("Note not exist with ID '$noteId'")
        }
    }

    private fun checkOwnerOrThrowException(userId: String, noteId: String) {
        if (!noteDao.isNoteOwnedByUser(noteId, userId)) {
            throw UnauthorizedActivityException("Access denied")
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
