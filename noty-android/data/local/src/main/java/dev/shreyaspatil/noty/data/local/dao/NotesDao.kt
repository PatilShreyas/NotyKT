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

package dev.shreyaspatil.noty.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.shreyaspatil.noty.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    /**
     * The return type of this method is nullable because internally it throws an error if
     * entity doesn't exist.
     *
     * Official docs says
     *
     * * When the return type is Flow<T>, querying an empty table throws a null pointer exception.
     * * When the return type is Flow<T?>, querying an empty table emits a null value.
     * * When the return type is Flow<List<T>>, querying an empty table emits an empty list.
     *
     * Refer: https://developer.android.com/reference/androidx/room/Query
     */
    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes ORDER BY isPinned = 1 DESC, created DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert
    suspend fun addNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET title = :title, note = :note WHERE noteId = :noteId")
    suspend fun updateNoteById(noteId: String, title: String, note: String)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("UPDATE notes SET noteId = :newNoteId WHERE noteId = :oldNoteId")
    fun updateNoteId(oldNoteId: String, newNoteId: String)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE noteId = :noteId")
    suspend fun updateNotePin(noteId: String, isPinned: Boolean)
}
