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

@Singleton
interface NotyNoteRepository {
    fun getNoteById(noteId: String): Flow<Note>
    suspend fun getAllNotes(): Flow<ResponseResult<List<Note>>>
    suspend fun addNote(title: String, note: String): Flow<ResponseResult<String>>
    suspend fun updateNote(
        noteId: String,
        title: String,
        note: String
    ): Flow<ResponseResult<String>>

    suspend fun deleteNote(noteId: String): Flow<ResponseResult<String>>
    suspend fun deleteAllNotes()
}
