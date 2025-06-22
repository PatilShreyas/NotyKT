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

package dev.shreyaspatil.noty.composeapp.fake.service

import dev.shreyaspatil.noty.composeapp.testUtil.successResponse
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.model.request.NoteRequest
import dev.shreyaspatil.noty.data.remote.model.response.NoteResponse
import dev.shreyaspatil.noty.data.remote.model.response.NotesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Path
import javax.inject.Inject

/**
 * Fake implementation for remote note Service.
 */
class FakeNotyService
    @Inject
    constructor() : NotyService {
        override suspend fun getAllNotes(): Response<NotesResponse> {
            return successResponse(NotesResponse(message = "", notes = emptyList()))
        }

        override suspend fun addNote(
            @Body noteRequest: NoteRequest,
        ): Response<NoteResponse> {
            // Do nothing, just return success
            return successNoteResponse()
        }

        override suspend fun updateNote(
            @Path(value = "noteId") noteId: String,
            @Body noteRequest: NoteRequest,
        ): Response<NoteResponse> {
            // Do nothing, just return success
            return successNoteResponse()
        }

        override suspend fun deleteNote(noteId: String): Response<NoteResponse> {
            // Do nothing, just return success
            return successNoteResponse()
        }

        override suspend fun pinNote(noteId: String): Response<NoteResponse> {
            // Do nothing, just return success
            return successNoteResponse()
        }

        override suspend fun unpinNote(noteId: String): Response<NoteResponse> {
            // Do nothing, just return success
            return successNoteResponse()
        }

        private fun successNoteResponse() = successResponse(NoteResponse(message = "", noteId = ""))
    }
