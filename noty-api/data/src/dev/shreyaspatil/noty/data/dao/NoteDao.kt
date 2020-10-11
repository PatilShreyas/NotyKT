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

package dev.shreyaspatil.noty.data.dao

import dev.shreyaspatil.noty.data.database.table.Notes
import dev.shreyaspatil.noty.data.entity.EntityNote
import dev.shreyaspatil.noty.data.entity.EntityUser
import dev.shreyaspatil.noty.data.model.Note
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

class NoteDao @Inject constructor() {
    fun addNote(userId: String, title: String, note: String): String = transaction {
        EntityNote.new {
            this.user = EntityUser[UUID.fromString(userId)]
            this.title = title
            this.note = note
        }.id.value.toString()
    }

    fun getNotesByUser(userId: String): List<Note> = transaction {
        EntityNote.find { Notes.user eq UUID.fromString(userId) }
            .sortedByDescending { it.id }
            .map { Note.fromEntity(it) }
    }

    fun updateNoteById(id: String, title: String, noteText: String): String = transaction {
        EntityNote[UUID.fromString(id)].apply {
            this.title = title
            this.note = noteText
        }.id.value.toString()
    }

    fun deleteNoteById(id: String): Boolean = transaction {
        val eNote = EntityNote.findById(UUID.fromString(id))
        eNote?.run {
            delete()
            return@transaction true
        }
        return@transaction false
    }

    fun isOwner(noteId: String, userId: String): Boolean = transaction {
        EntityNote.find {
            (Notes.id eq UUID.fromString(noteId)) and (Notes.user eq UUID.fromString(userId))
        }.firstOrNull() != null
    }

    fun isExist(id: String): Boolean = transaction {
        EntityNote.findById(UUID.fromString(id)) != null
    }
}