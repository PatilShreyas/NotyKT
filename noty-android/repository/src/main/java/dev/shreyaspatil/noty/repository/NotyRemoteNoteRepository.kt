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
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.model.request.NoteRequest
import dev.shreyaspatil.noty.data.remote.model.response.State
import dev.shreyaspatil.noty.data.remote.util.getResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * Single source of data of noty user.
 */
@ExperimentalCoroutinesApi
@Singleton
class NotyRemoteNoteRepository @Inject internal constructor(
    private val notyService: NotyService
) : NotyNoteRepository {

    override fun getAllNotes(): Flow<ResponseResult<List<Note>>> = flow {
        val notesResponse = notyService.getAllNotes().getResponse()

        val state = when (notesResponse.status) {
            State.SUCCESS -> ResponseResult.success(notesResponse.notes)
            else -> ResponseResult.error(notesResponse.message)
        }

        emit(state)
    }.catch { emit(ResponseResult.error("Can't sync latest notes")) }

    override suspend fun addNote(title: String, note: String): ResponseResult<String> {
        return runCatching {
            val notesResponse = notyService.addNote(NoteRequest(title, note)).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
                else -> ResponseResult.error(notesResponse.message)
            }
        }.getOrElse {
            it.printStackTrace()
            (ResponseResult.error("Something went wrong!"))
        }
    }

    override suspend fun updateNote(
        noteId: String,
        title: String,
        note: String
    ): ResponseResult<String> {
        return runCatching {
            val notesResponse = notyService.updateNote(
                noteId,
                NoteRequest(title, note)
            ).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
                else -> ResponseResult.error(notesResponse.message)
            }
        }.getOrDefault(ResponseResult.error("Something went wrong!"))
    }

    override suspend fun deleteNote(noteId: String): ResponseResult<String> {
        return runCatching {
            val notesResponse = notyService.deleteNote(noteId).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
                else -> ResponseResult.error(notesResponse.message)
            }
        }.getOrDefault(ResponseResult.error("Something went wrong!"))
    }

    /** Not needed (NO-OP) **/
    override fun getNoteById(noteId: String): Flow<Note> = emptyFlow()

    /** Not needed (NO-OP) **/
    override suspend fun addNotes(notes: List<Note>) {}

    /** Not needed (NO-OP) **/
    override suspend fun deleteAllNotes() {}

    /** Not needed (NO-OP) **/
    override suspend fun updateNoteId(oldNoteId: String, newNoteId: String) {}
}
