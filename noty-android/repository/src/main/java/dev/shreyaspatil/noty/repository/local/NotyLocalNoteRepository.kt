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

package dev.shreyaspatil.noty.repository.local

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.data.local.dao.NotesDao
import dev.shreyaspatil.noty.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class NotyLocalNoteRepository @Inject constructor(
    private val notesDao: NotesDao
) : NotyNoteRepository {

    override fun getNoteById(noteId: String): Flow<Note> = notesDao.getNoteById(noteId)
        .map { Note(it.noteId, it.title, it.note, it.created) }

    override fun getAllNotes(): Flow<ResponseResult<List<Note>>> = notesDao.getAllNotes()
        .map { notes -> notes.map { Note(it.noteId, it.title, it.note, it.created) } }
        .transform { notes -> emit(ResponseResult.success(notes)) }
        .catch { emit(ResponseResult.success(emptyList())) }

    override suspend fun addNote(
        title: String,
        note: String
    ): ResponseResult<String> = runCatching {
        val tempNoteId = NotyNoteRepository.generateTemporaryId()
        notesDao.addNote(
            NoteEntity(
                tempNoteId,
                title,
                note,
                System.currentTimeMillis()
            )
        )
        ResponseResult.success(tempNoteId)
    }.getOrDefault(ResponseResult.error("Unable to create a new note"))

    override suspend fun addNotes(notes: List<Note>) = notes.map {
        NoteEntity(it.id, it.title, it.note, it.created)
    }.let {
        notesDao.addNotes(it)
    }

    override suspend fun updateNote(
        noteId: String,
        title: String,
        note: String
    ): ResponseResult<String> = runCatching {
        notesDao.updateNoteById(noteId, title, note)
        ResponseResult.success(noteId)
    }.getOrDefault(ResponseResult.error("Unable to update a note"))

    override suspend fun deleteNote(noteId: String): ResponseResult<String> = runCatching {
        notesDao.deleteNoteById(noteId)
        ResponseResult.success(noteId)
    }.getOrDefault(ResponseResult.error("Unable to delete a note"))

    override suspend fun deleteAllNotes() = notesDao.deleteAllNotes()

    override suspend fun updateNoteId(oldNoteId: String, newNoteId: String) =
        notesDao.updateNoteId(oldNoteId, newNoteId)
}
