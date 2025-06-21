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

package dev.shreyaspatil.noty.repository

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.model.request.NoteRequest
import dev.shreyaspatil.noty.data.remote.util.either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Source of data of notes from network
 */
@Singleton
class NotyRemoteNoteRepository
    @Inject
    internal constructor(
        private val notyService: NotyService,
    ) : NotyNoteRepository {
        override fun getAllNotes(): Flow<Either<List<Note>>> =
            flow {
                emit(notyService.getAllNotes().either { it.notes })
            }.catch { emit(Either.error("Can't sync latest notes")) }

        override suspend fun addNote(
            title: String,
            note: String,
        ): Either<String> {
            return notyService.addNote(NoteRequest(title, note)).either { it.noteId!! }
        }

        override suspend fun updateNote(
            noteId: String,
            title: String,
            note: String,
        ): Either<String> {
            return notyService.updateNote(noteId, NoteRequest(title, note)).either { it.noteId!! }
        }

        override suspend fun deleteNote(noteId: String): Either<String> {
            return notyService.deleteNote(noteId).either { it.noteId!! }
        }

        override suspend fun pinNote(
            noteId: String,
            isPinned: Boolean,
        ): Either<String> {
            val response =
                with(notyService) {
                    if (isPinned) pinNote(noteId) else unpinNote(noteId)
                }
            return response.either { it.noteId!! }
        }

        /** Not needed (NO-OP) **/
        override fun getNoteById(noteId: String): Flow<Note> = emptyFlow()

        /** Not needed (NO-OP) **/
        override suspend fun addNotes(notes: List<Note>) {}

        /** Not needed (NO-OP) **/
        override suspend fun deleteAllNotes() {}

        /** Not needed (NO-OP) **/
        override suspend fun updateNoteId(
            oldNoteId: String,
            newNoteId: String,
        ) {
        }
    }
