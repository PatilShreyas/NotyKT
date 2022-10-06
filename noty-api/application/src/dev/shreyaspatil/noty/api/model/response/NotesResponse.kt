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

package dev.shreyaspatil.noty.api.model.response

import kotlinx.serialization.Serializable

/**
 * Response model used for exposing a note.
 */
@Serializable
data class Note(val id: String, val title: String, val note: String, val created: Long, val isPinned: Boolean)

/**
 * Response model used for exposing list of notes in API.
 */
@Serializable
data class NotesResponse(
    override val status: State,
    override val message: String,
    val notes: List<Note> = emptyList()

) : Response {
    companion object {
        fun unauthorized(message: String) = NotesResponse(
            State.UNAUTHORIZED,
            message
        )

        fun success(notes: List<Note>) = NotesResponse(
            State.SUCCESS,
            "Task successful",
            notes
        )
    }
}

/**
 * Response model used for exposing operation status performed on notes via API.
 * For e.g. Creating new note, deleting or updating note.
 */
@Serializable
data class NoteResponse(
    override val status: State,
    override val message: String,
    val noteId: String? = null
) : Response {
    companion object {
        fun unauthorized(message: String) = NoteResponse(
            State.UNAUTHORIZED,
            message
        )

        fun failed(message: String) = NoteResponse(
            State.FAILED,
            message
        )

        fun notFound(message: String) = NoteResponse(
            State.NOT_FOUND,
            message
        )

        fun success(id: String) = NoteResponse(
            State.SUCCESS,
            "Task successful",
            id
        )
    }
}