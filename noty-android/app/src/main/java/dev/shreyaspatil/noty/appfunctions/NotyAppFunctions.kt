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

package dev.shreyaspatil.noty.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.di.LocalRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * A note.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class Note(
    /** The note's identifier */
    val id: String,
    /** The note's title */
    val title: String,
    /** The note's content */
    val content: String,
)

class NotyAppFunctions
    @Inject
    constructor(
        @LocalRepository private val repository: NotyNoteRepository,
    ) {
        /**
         * Lists all available notes.
         *
         * @param appFunctionContext The context in which the AppFunction is executed.
         */
        @AppFunction(isDescribedByKDoc = true)
        suspend fun listNotes(appFunctionContext: AppFunctionContext): List<Note> =
            repository.getAllNotes().first().getOrNull()?.let {
                it.map { Note(it.id, it.title, it.note) }
            } ?: emptyList()

        /**
         * Adds a new note to the app.
         *
         * @param appFunctionContext The context in which the AppFunction is executed.
         * @param title The title of the note.
         * @param content The note's content.
         */
        @AppFunction(isDescribedByKDoc = true)
        suspend fun createNote(
            appFunctionContext: AppFunctionContext,
            title: String,
            content: String,
        ): Note? =
            repository.addNote(title, content).getOrNull()?.let {
                Note(it, title, content)
            }

        /**
         * Edits a single note.
         *
         * @param appFunctionContext The context in which the AppFunction is executed.
         * @param noteId The target note's ID.
         * @param title The note's title if it should be updated.
         * @param content The new content if it should be updated.
         */
        @AppFunction(isDescribedByKDoc = true)
        suspend fun editNote(
            appFunctionContext: AppFunctionContext,
            noteId: String,
            title: String?,
            content: String?,
        ): Note? {
            val updatedNote =
                repository
                    .getNoteById(noteId)
                    .first()
                    .let { note -> title?.let { note.copy(title = it) } ?: note }
                    .let { note -> content?.let { note.copy(note = it) } ?: note }

            val result = repository.updateNote(noteId, updatedNote.title, updatedNote.note)

            return result.getOrNull()?.let { Note(noteId, updatedNote.title, updatedNote.note) }
        }

        /**
         * Deletes a single note.
         *
         * @param appFunctionContext The context in which the AppFunction is executed.
         * @param noteId The target note's ID.
         *
         * @return Whether the note was deleted or not.
         */
        @AppFunction(isDescribedByKDoc = true)
        suspend fun deleteNote(
            appFunctionContext: AppFunctionContext,
            noteId: String,
        ): Boolean = repository.deleteNote(noteId).getOrNull() != null
    }
