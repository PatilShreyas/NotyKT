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

import com.squareup.moshi.adapter
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.utils.moshi
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.model.request.NoteRequest
import dev.shreyaspatil.noty.data.remote.model.request.NoteUpdatePinRequest
import dev.shreyaspatil.noty.data.remote.model.response.NoteResponse
import dev.shreyaspatil.noty.data.remote.model.response.NotesResponse
import dev.shreyaspatil.noty.data.remote.model.response.State
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NotyRemoteNoteRepositoryTest {
    private lateinit var service: FakeNotyService
    private lateinit var repository: NotyRemoteNoteRepository

    @BeforeEach
    fun setup() {
        service = spyk(FakeNotyService())
        repository = NotyRemoteNoteRepository(service)
    }

    @Test
    fun `getAllNotes should return notes when operation is successful`() = runTest {
        // Given
        service.returnSuccessOnGetAllNotes = true

        // When
        val response = repository.getAllNotes().first()

        // Then
        coVerify { service.getAllNotes() }
        val notes = (response as Either.Success).data
        assertEquals(1, notes.size)
        assertEquals(Note("1111", "Lorem Ipsum", "Hey there", 0), notes.first())
    }

    @Test
    fun `getAllNotes should return error when operation is unsuccessful`() = runTest {
        // Given
        service.returnSuccessOnGetAllNotes = false

        // When
        val response = repository.getAllNotes().first()

        // Then
        coVerify { service.getAllNotes() }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `addNote should return note id when inputs are valid`() = runTest {
        // When
        val response = repository.addNote("Lorem Ipsum", "Hey there!")

        // Then
        coVerify { service.addNote(NoteRequest("Lorem Ipsum", "Hey there!")) }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `addNote should return error when inputs are invalid`() = runTest {
        // When
        val response = repository.addNote("Test note", "Hey there!")

        // Then
        coVerify { service.addNote(NoteRequest("Test note", "Hey there!")) }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `updateNote should return note id when inputs are valid`() = runTest {
        // When
        val response =
            repository.updateNote(
                noteId = "1111",
                title = "Lorem Ipsum",
                note = "Hey there!"
            )

        // Then
        coVerify {
            service.updateNote(
                noteId = "1111",
                noteRequest = NoteRequest("Lorem Ipsum", "Hey there!")
            )
        }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `updateNote should return error when inputs are invalid`() = runTest {
        // When
        val response =
            repository.updateNote(
                noteId = "2222",
                title = "Lorem Ipsum",
                note = "Hey there!"
            )

        // Then
        coVerify {
            service.updateNote(
                noteId = "2222",
                noteRequest = NoteRequest("Lorem Ipsum", "Hey there!")
            )
        }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `deleteNote should return note id when inputs are valid`() = runTest {
        // When
        val response = repository.deleteNote(noteId = "1111")

        // Then
        coVerify { service.deleteNote(noteId = "1111") }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `deleteNote should return error when inputs are invalid`() = runTest {
        // When
        val response = repository.deleteNote(noteId = "2222")

        // Then
        coVerify { service.deleteNote(noteId = "2222") }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `pinNote should return note id when inputs are valid and note is pinned`() = runTest {
        // When
        val response = repository.pinNote(noteId = "1111", isPinned = true)

        // Then
        coVerify {
            service.updateNotePin(
                noteId = "1111",
                NoteUpdatePinRequest(isPinned = true)
            )
        }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `pinNote should return error when inputs are invalid and note is pinned`() = runTest {
        // When
        val response = repository.pinNote(noteId = "2222", isPinned = true)

        // Then
        coVerify {
            service.updateNotePin(
                noteId = "2222",
                NoteUpdatePinRequest(isPinned = true)
            )
        }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `pinNote should return note id when inputs are valid and note is unpinned`() = runTest {
        // When
        val response = repository.pinNote(noteId = "1111", isPinned = false)

        // Then
        coVerify {
            service.updateNotePin(
                noteId = "1111",
                NoteUpdatePinRequest(isPinned = false)
            )
        }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `pinNote should return error when inputs are invalid and note is unpinned`() = runTest {
        // When
        val response = repository.pinNote(noteId = "2222", isPinned = false)

        // Then
        coVerify {
            service.updateNotePin(
                noteId = "2222",
                NoteUpdatePinRequest(isPinned = false)
            )
        }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }
}

class FakeNotyService : NotyService {
    var returnSuccessOnGetAllNotes: Boolean = true

    override suspend fun getAllNotes(): Response<NotesResponse> {
        return fakeNotesResponse()
    }

    override suspend fun addNote(noteRequest: NoteRequest): Response<NoteResponse> {
        return if (noteRequest.title == "Lorem Ipsum") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    override suspend fun updateNote(noteId: String, noteRequest: NoteRequest): Response<NoteResponse> {
        return if (noteId == "1111") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    override suspend fun deleteNote(noteId: String): Response<NoteResponse> {
        return if (noteId == "1111") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    override suspend fun updateNotePin(noteId: String, noteRequest: NoteUpdatePinRequest): Response<NoteResponse> {
        return if (noteId == "1111") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeNoteResponse(success: Boolean): Response<NoteResponse> {
        return if (success) {
            Response.success(NoteResponse(State.SUCCESS, "Success", "1111"))
        } else {
            val response = NoteResponse(State.FAILED, "Failed to perform operation", null)
            val body =
                ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    moshi.adapter<NoteResponse>().toJson(response)
                )
            Response.error(400, body)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeNotesResponse(): Response<NotesResponse> {
        return if (returnSuccessOnGetAllNotes) {
            Response.success(
                NotesResponse(
                    status = State.SUCCESS,
                    message = "Success",
                    notes = listOf(Note("1111", "Lorem Ipsum", "Hey there", 0))
                )
            )
        } else {
            val response = NotesResponse(State.FAILED, "Failed to perform operation", emptyList())
            val body =
                ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    moshi.adapter<NotesResponse>().toJson(response)
                )
            Response.error(400, body)
        }
    }
}
