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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NotyRemoteNoteRepositoryTest {
    private lateinit var service: FakeNotyService
    private lateinit var repository: NotyRemoteNoteRepository

    @BeforeEach
    fun setUp() {
        service = spyk(FakeNotyService())
        repository = NotyRemoteNoteRepository(service)
    }

    @Test
    fun `getAllNotes with successful operation should return notes`() = runTest {
        service.returnSuccessOnGetAllNotes = true
        val response = repository.getAllNotes().first()

        coVerify { service.getAllNotes() }
        val notes = (response as Either.Success).data
        assertEquals(1, notes.size)
        assertEquals(Note("1111", "Lorem Ipsum", "Hey there", 0), notes.first())
    }

    @Test
    fun `getAllNotes with unsuccessful operation should return error`() = runTest {
        service.returnSuccessOnGetAllNotes = false
        val response = repository.getAllNotes().first()

        coVerify { service.getAllNotes() }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `addNote with valid inputs should return noteId`() = runTest {
        val response = repository.addNote("Lorem Ipsum", "Hey there!")

        coVerify { service.addNote(NoteRequest("Lorem Ipsum", "Hey there!")) }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `addNote with invalid inputs should return error`() = runTest {
        val response = repository.addNote("Test note", "Hey there!")

        coVerify { service.addNote(NoteRequest("Test note", "Hey there!")) }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `updateNote with valid inputs should return noteId`() = runTest {
        val response = repository.updateNote("1111", "Lorem Ipsum", "Hey there!")

        coVerify { service.updateNote("1111", NoteRequest("Lorem Ipsum", "Hey there!")) }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `updateNote with invalid inputs should return error`() = runTest {
        val response = repository.updateNote("2222", "Lorem Ipsum", "Hey there!")

        coVerify { service.updateNote("2222", NoteRequest("Lorem Ipsum", "Hey there!")) }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `deleteNote with valid input should return noteId`() = runTest {
        val response = repository.deleteNote("1111")

        coVerify { service.deleteNote("1111") }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `deleteNote with invalid input should return error`() = runTest {
        val response = repository.deleteNote("2222")

        coVerify { service.deleteNote("2222") }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `pinNote with valid input and pin true should return noteId`() = runTest {
        val response = repository.pinNote("1111", true)

        coVerify { service.updateNotePin("1111", NoteUpdatePinRequest(isPinned = true)) }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `pinNote with invalid input and pin true should return error`() = runTest {
        val response = repository.pinNote("2222", true)

        coVerify { service.updateNotePin("2222", NoteUpdatePinRequest(isPinned = true)) }
        val message = (response as Either.Error).message
        assertEquals("Failed to perform operation", message)
    }

    @Test
    fun `pinNote with valid input and pin false should return noteId`() = runTest {
        val response = repository.pinNote("1111", false)

        coVerify { service.updateNotePin("1111", NoteUpdatePinRequest(isPinned = false)) }
        val id = (response as Either.Success).data
        assertEquals("1111", id)
    }

    @Test
    fun `pinNote with invalid input and pin false should return error`() = runTest {
        val response = repository.pinNote("2222", false)

        coVerify { service.updateNotePin("2222", NoteUpdatePinRequest(isPinned = false)) }
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

    override suspend fun updateNote(
        noteId: String,
        noteRequest: NoteRequest,
    ): Response<NoteResponse> {
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

    override suspend fun updateNotePin(
        noteId: String,
        noteRequest: NoteUpdatePinRequest,
    ): Response<NoteResponse> {
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
                    moshi.adapter<NoteResponse>().toJson(response),
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
                    notes = listOf(Note("1111", "Lorem Ipsum", "Hey there", 0)),
                ),
            )
        } else {
            val response = NotesResponse(State.FAILED, "Failed to perform operation", emptyList())
            val body =
                ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    moshi.adapter<NotesResponse>().toJson(response),
                )
            Response.error(400, body)
        }
    }
}
