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
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface NoteDao {
    fun add(userId: String, title: String, note: String): String
    fun getAllByUser(userId: String): List<Note>
    fun update(id: String, title: String, note: String): String
    fun deleteById(id: String): Boolean
    fun isNoteOwnedByUser(id: String, userId: String): Boolean
    fun exists(id: String): Boolean
    fun updateNotePinById(id: String, isPinned: Boolean): String
}

@Singleton
class NoteDaoImpl @Inject constructor() : NoteDao {
    override fun add(userId: String, title: String, note: String): String = transaction {
        EntityNote.new {
            this.user = EntityUser[UUID.fromString(userId)]
            this.title = title
            this.note = note
        }.id.value.toString()
    }

    override fun getAllByUser(userId: String): List<Note> = transaction {
        EntityNote.find { Notes.user eq UUID.fromString(userId) }
            .sortedWith(compareBy({ it.isPinned }, { it.updated }))
            .reversed()
            .map { Note.fromEntity(it) }
    }

    override fun update(id: String, title: String, note: String): String = transaction {
        EntityNote[UUID.fromString(id)].apply {
            this.title = title
            this.note = note
        }.id.value.toString()
    }

    override fun deleteById(id: String): Boolean = transaction {
        val eNote = EntityNote.findById(UUID.fromString(id))
        eNote?.run {
            delete()
            return@transaction true
        }
        return@transaction false
    }

    override fun isNoteOwnedByUser(id: String, userId: String): Boolean = transaction {
        EntityNote.find {
            (Notes.id eq UUID.fromString(id)) and (Notes.user eq UUID.fromString(userId))
        }.firstOrNull() != null
    }

    override fun exists(id: String): Boolean = transaction {
        EntityNote.findById(UUID.fromString(id)) != null
    }

    override fun updateNotePinById(id: String, isPinned: Boolean): String = transaction {
        EntityNote[UUID.fromString(id)].apply {
            this.isPinned = isPinned
            this.updated = DateTime.now()
        }.id.value.toString()
    }
}