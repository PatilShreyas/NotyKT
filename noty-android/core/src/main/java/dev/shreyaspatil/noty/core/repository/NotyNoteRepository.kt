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

package dev.shreyaspatil.noty.core.repository

import dev.shreyaspatil.noty.core.model.Note
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Repository for notes.
 */
@Singleton
interface NotyNoteRepository {

    /**
     * Returns a note
     *
     * @param noteId A note ID.
     */
    fun getNoteById(noteId: String): Flow<Note>

    /**
     * Returns all notes.
     */
    fun getAllNotes(): Flow<ResponseResult<List<Note>>>

    /**
     * Adds a new note
     *
     * @param title Title of a note
     * @param note Body of a note
     */
    suspend fun addNote(title: String, note: String): ResponseResult<String>

    /**
     * Adds a list of notes. Replaces notes if already exists
     */
    suspend fun addNotes(notes: List<Note>)

    /**
     * Updates a new note having ID [noteId]
     *
     * @param noteId The Note ID
     * @param title Title of a note
     * @param note Body of a note
     */
    suspend fun updateNote(
        noteId: String,
        title: String,
        note: String
    ): ResponseResult<String>

    /**
     * Deletes a new note having ID [noteId]
     */
    suspend fun deleteNote(noteId: String): ResponseResult<String>

    /**
     * Deletes all notes.
     */
    suspend fun deleteAllNotes()

    /**
     * Updates ID of a note
     */
    suspend fun updateNoteId(oldNoteId: String, newNoteId: String)
}
