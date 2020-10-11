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
import dev.shreyaspatil.noty.data.local.dao.NotesDao
import dev.shreyaspatil.noty.data.local.entity.NoteEntity
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.model.request.NoteRequest
import dev.shreyaspatil.noty.data.remote.model.response.NotesResponse
import dev.shreyaspatil.noty.data.remote.model.response.State
import dev.shreyaspatil.noty.data.remote.util.getResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
@Singleton
class DefaultNotyNoteNoteRepository @Inject internal constructor(
    private val notyService: NotyService,
    private val notesDao: NotesDao
) : NotyNoteRepository {
    override fun getNoteById(noteId: String): Flow<Note> = notesDao.getNoteById(noteId)
        .map { Note(it.noteId, it.title, it.note, it.created) }

    override suspend fun getAllNotes() =
        object : NetworkBoundRepository<List<Note>, NotesResponse>() {
            override suspend fun persistData(response: NotesResponse) = response.notes
                .map { NoteEntity(it.id, it.title, it.note, it.created) }
                .let { notesDao.clearAndAddNotes(it) }

            override fun fetchFromLocal(): Flow<List<Note>> = notesDao.getAllNotes()
                .map { notes -> notes.map { Note(it.noteId, it.title, it.note, it.created) } }

            override suspend fun fetchFromRemote(): NotesResponse =
                notyService.getAllNotes().getResponse()
        }.asFlow()

    override suspend fun addNote(title: String, note: String): Flow<ResponseResult<String>> = flow {
        val notesResponse = notyService.addNote(NoteRequest(title, note)).getResponse()

        val state = when (notesResponse.status) {
            State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
            else -> ResponseResult.error<String>(notesResponse.message)
        }

        emit(state)
    }.catch {
        emit(ResponseResult.error<String>("Something went wrong!"))
    }

    override suspend fun updateNote(
        noteId: String,
        title: String,
        note: String
    ): Flow<ResponseResult<String>> = flow {
        val notesResponse = notyService.updateNote(noteId, NoteRequest(title, note)).getResponse()

        val state = when (notesResponse.status) {
            State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
            else -> ResponseResult.error<String>(notesResponse.message)
        }

        emit(state)
    }.catch {
        emit(ResponseResult.error<String>("Something went wrong!"))
    }

    override suspend fun deleteNote(noteId: String): Flow<ResponseResult<String>> = flow {
        val notesResponse = notyService.deleteNote(noteId).getResponse()

        val state = when (notesResponse.status) {
            State.SUCCESS -> ResponseResult.success(notesResponse.noteId!!)
            else -> ResponseResult.error<String>(notesResponse.message)
        }

        emit(state)
    }.catch {
        emit(ResponseResult.error<String>("Something went wrong!"))
    }

    override suspend fun deleteAllNotes() = notesDao.deleteAllNotes()
}
