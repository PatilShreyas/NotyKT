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
import dev.shreyaspatil.noty.api.model.response.Note
import dev.shreyaspatil.noty.api.model.response.NoteResponse
import dev.shreyaspatil.noty.api.model.response.NotesResponse
import dev.shreyaspatil.noty.data.dao.NoteDao
import javax.inject.Inject

/**
 * Controller for notes management
 */
class NotesController @Inject constructor(private val noteDao: NoteDao) {

    fun getNotesByUser(userId: String): NotesResponse {
        return try {
            val notes = noteDao.getNotesByUser(userId)

            NotesResponse.success(notes.map { Note(it.id, it.title, it.note, it.created) })
        } catch (uae: UnauthorizedActivityException) {
            NotesResponse.unauthorized(uae.message)
        }
    }

    fun addNote(userId: String, note: NoteRequest): NoteResponse {
        return try {
            val noteTitle = note.title.trim()
            val noteText = note.note.trim()

            validateNoteOrThrowException(noteTitle, noteText)

            val noteId = noteDao.addNote(userId, noteTitle, noteText)
            NoteResponse.success(noteId)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        } catch (uae: UnauthorizedActivityException) {
            NoteResponse.unauthorized(uae.message)
        }
    }

    fun updateNote(userId: String, noteId: String, note: NoteRequest): NoteResponse {
        return try {
            val noteTitle = note.title.trim()
            val noteText = note.note.trim()

            validateNoteOrThrowException(noteTitle, noteText)
            checkNoteExistsOrThrowException(noteId)
            checkOwnerOrThrowException(userId, noteId)

            val id = noteDao.updateNoteById(noteId, noteTitle, noteText)
            NoteResponse.success(id)
        } catch (uae: UnauthorizedActivityException) {
            NoteResponse.unauthorized(uae.message)
        } catch (bre: BadRequestException) {
            NoteResponse.failed(bre.message)
        } catch (nfe: NoteNotFoundException) {
            NoteResponse.notFound(nfe.message)
        }
    }

    fun deleteNote(userId: String, noteId: String): NoteResponse {
        return try {
            checkNoteExistsOrThrowException(noteId)
            checkOwnerOrThrowException(userId, noteId)

            if (noteDao.deleteNoteById(noteId)) {
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

    private fun checkNoteExistsOrThrowException(noteId: String) {
        if (!noteDao.isExist(noteId)) {
            throw NoteNotFoundException("Note not exist with ID '$noteId'")
        }
    }

    private fun checkOwnerOrThrowException(userId: String, noteId: String) {
        if (!noteDao.isOwner(noteId, userId)) {
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