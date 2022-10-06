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

package dev.shreyaspatil.noty.api

import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.request.AuthRequest
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.model.response.AuthResponse
import dev.shreyaspatil.noty.api.model.response.NoteResponse
import dev.shreyaspatil.noty.api.model.response.NotesResponse
import dev.shreyaspatil.noty.api.model.response.State
import dev.shreyaspatil.noty.api.testutils.toJson
import dev.shreyaspatil.noty.api.testutils.toModel
import dev.shreyaspatil.noty.application.testutils.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.matchers.string.shouldInclude
import io.ktor.config.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

@Suppress("unused")
@KtorExperimentalAPI
class ApplicationTest : AnnotationSpec() {

    private val sqlContainer = DatabaseContainer()

    @BeforeClass
    fun setup() {
        sqlContainer.start()
    }

    @Test
    fun whenPassedInvalidCredentials_responseStateShouldBeFailed() = testApp {
        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()

            body.status shouldBe State.FAILED
            body.token shouldBe null
        }
    }

    @Test
    fun whenRegistrationIsSuccessful_shouldBeAbleToLogin() = testApp {
        post("/auth/register", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()

            body.status shouldBe State.SUCCESS
            body.message shouldBe "Registration successful"
            body.token shouldNotBe null
        }

        post("/auth/login", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()

            body.status shouldBe State.SUCCESS
            body.message shouldBe "Login successful"
            body.token shouldNotBe null
        }
    }

    @Test
    fun whenRegistrationIsSuccessful_whenTryingToRegisterAgainWithSameUsername_shouldRespondFailedState() = testApp {
        val authRequest = AuthRequest("iuser", "test1234").toJson()
        post("/auth/register", authRequest)

        // Try to create again. It should show username not available message.
        post("/auth/register", authRequest).content.toModel<AuthResponse>().let { response ->
            response.status shouldBe State.FAILED
            response.token shouldBe null
            response.message shouldBe "Username is not available"
        }
    }

    @Test
    fun whenCredentialsAreIllegal_shouldRespondFailedStateWithMessage() = testApp {
        post("/auth/register", AuthRequest("test#", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "No special characters allowed in username"
            body.token shouldBe null
        }

        post("/auth/register", AuthRequest("hi", "hi@12341").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "Username should be of min 4 and max 30 character in length"
            body.token shouldBe null
        }

        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "Password should be of min 8 and max 50 character in length"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("test#", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "No special characters allowed in username"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("hi", "hi@12341").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "Username should be of min 4 and max 30 character in length"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.content.toModel()
            body.status shouldBe State.FAILED
            body.message shouldBe "Password should be of min 8 and max 50 character in length"
            body.token shouldBe null
        }
    }

    @Test
    fun whenProvidedInvalidCredentials_shouldFailAndShowError() = testApp {
        post(
            "/auth/login",
            AuthRequest("usernotexists", "test1234").toJson()
        ).content.toModel<AuthResponse>().let {
            it.status shouldBe State.UNAUTHORIZED
            it.message shouldBe "Invalid credentials"
            it.token shouldBe null
        }
    }

    @Test
    fun authorizationKeyIsNotProvided_whenNotesAreRetrieved_shouldGetUnauthResponse() = testApp {
        get("/notes").content shouldInclude "UNAUTHORIZED"
    }

    @Test
    fun whenProvidedInvalidAuthBody_shouldThrowException() = testApp {
        shouldThrow<BadRequestException> {
            post("/auth/register", null)
        }.let {
            it.message shouldContainIgnoringCase FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }

        shouldThrow<BadRequestException> {
            post("/auth/login", null)
        }.let {
            it.message shouldContainIgnoringCase FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }
    }

    @Test
    fun whenProvidedInvalidNoteBody_shouldThrowException() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("newnoteuser", "newnoteuser1234").toJson()
        ).content.toModel<AuthResponse>().token

        shouldThrow<BadRequestException> {
            post("note/new", null, "Bearer $token")
        }.let {
            it.message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }

        shouldThrow<BadRequestException> {
            put("note/testnote", null, "Bearer $token")
        }.let {
            it.message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }
    }

    @Test
    fun whenUserIsAuthenticated_shouldBeAbleToAddUpdateDeleteNotes() = testApp {
        // Create user
        val authResponse = post(
            "/auth/register",
            AuthRequest("notemaster", "notemaster").toJson()
        ).content.toModel<AuthResponse>()

        // Create note
        val newNoteJson = NoteRequest("Hey test", "This is note text").toJson()

        val newNoteResponse = post(
            "/note/new",
            newNoteJson,
            "Bearer ${authResponse.token}"
        ).content.toModel<NoteResponse>()

        newNoteResponse.status shouldBe State.SUCCESS
        newNoteResponse.noteId shouldNotBe null

        // Get Notes
        get("/notes", "Bearer ${authResponse.token}").content.toModel<NotesResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.notes shouldHaveSize 1
            response.notes[0].let {
                it.id shouldBe newNoteResponse.noteId
                it.title shouldBe "Hey test"
                it.note shouldBe "This is note text"
                it.created shouldNotBe null
                it.isPinned shouldBe false
            }
        }

        // Update note
        val updateRequest = NoteRequest("Hey update", "This is updated body").toJson()
        put(
            "/note/${newNoteResponse.noteId}",
            updateRequest,
            "Bearer ${authResponse.token}"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.noteId shouldNotBe null
        }

        // pin note
        val pinRequest = PinRequest(isPinned = true).toJson()
        put(
            "/note/${newNoteResponse.noteId}/pin",
            pinRequest,
            "Bearer ${authResponse.token}"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.noteId shouldNotBe null
        }

        get("/notes", "Bearer ${authResponse.token}").content.toModel<NotesResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.notes shouldHaveSize 1
            response.notes[0].let {
                it.id shouldBe newNoteResponse.noteId
                it.title shouldBe "Hey update"
                it.note shouldBe "This is updated body"
                it.created shouldNotBe null
                it.isPinned shouldBe true
            }
        }

        val unpinRequest = PinRequest(isPinned = false).toJson()
        put(
            "/note/${newNoteResponse.noteId}/pin",
            unpinRequest,
            "Bearer ${authResponse.token}"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.noteId shouldNotBe null
        }

        get("/notes", "Bearer ${authResponse.token}").content.toModel<NotesResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.notes shouldHaveSize 1
            response.notes[0].let {
                it.id shouldBe newNoteResponse.noteId
                it.title shouldBe "Hey update"
                it.note shouldBe "This is updated body"
                it.created shouldNotBe null
                it.isPinned shouldBe false
            }
        }

        // Delete note
        delete(
            "/note/${newNoteResponse.noteId}",
            "Bearer ${authResponse.token}"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.noteId shouldNotBe null
        }

        // Get empty notes
        get("/notes", "Bearer ${authResponse.token}").content.toModel<NotesResponse>().let { response ->
            response.status shouldBe State.SUCCESS
            response.notes shouldHaveSize 0
        }
    }

    @Test
    fun whenNoteIsCreatedByUserA_shouldNotBeAccessibleByUserB() = testApp {
        // Create User A
        val userTokenA = post(
            "/auth/register",
            AuthRequest("userA", "userA1234").toJson()
        ).content.toModel<AuthResponse>().token

        // Create User B
        val userTokenB = post(
            "/auth/register",
            AuthRequest("userB", "userB1234").toJson()
        ).content.toModel<AuthResponse>().token

        // User A creates note
        val noteRequest = NoteRequest("Hey test", "This is note text").toJson()
        val noteId = post(
            "/note/new",
            noteRequest,
            "Bearer $userTokenA"
        ).content.toModel<NoteResponse>().noteId

        // User B tries to delete note created by user A
        // Should show access denied (Unauthorized access) message.
        delete(
            "/note/$noteId",
            "Bearer $userTokenB"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.UNAUTHORIZED
            response.message shouldBe "Access denied"
            response.noteId shouldBe null
        }
    }

    @Test
    fun whenNoteRequestIsInvalid_shouldRespondFailedStateWithMessage() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("usertest", "userA1234").toJson()
        ).content.toModel<AuthResponse>().token
        println(token)

        // Create note with invalid title (Whitespaces)
        val noteRequest1 = NoteRequest("      Hi       ", "This is note text").toJson()
        post(
            "/note/new",
            noteRequest1,
            "Bearer $token"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.FAILED
            response.noteId shouldBe null
            response.message shouldBe "Title should be of min 4 and max 30 character in length"
        }

        // Create note with invalid note text (Whitespaces)
        val noteRequest2 = NoteRequest("Hi there!", "            ").toJson()
        post(
            "/note/new",
            noteRequest2,
            "Bearer $token"
        ).content.toModel<NoteResponse>().let { response ->
            response.status shouldBe State.FAILED
            response.noteId shouldBe null
            response.message shouldBe "Title and Note should not be blank"
        }
    }

    @Test
    fun whenNoteNotExists_requestedUpdateDelete_shouldShowErrorMessage() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("usernotenotexist", "test1234").toJson()
        ).content.toModel<AuthResponse>().token

        val noteId = UUID.randomUUID().toString()
        put(
            "note/$noteId",
            NoteRequest("Title", "Body").toJson(),
            "Bearer $token"
        ).content.toModel<NoteResponse>().let {
            it.status shouldBe State.NOT_FOUND
            it.message shouldBe "Note not exist with ID '$noteId'"
            it.noteId shouldBe null
        }

        delete(
            "note/$noteId",
            "Bearer $token"
        ).content.toModel<NoteResponse>().let {
            it.status shouldBe State.NOT_FOUND
            it.message shouldBe "Note not exist with ID '$noteId'"
            it.noteId shouldBe null
        }
    }

    fun testApp(test: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            {
                (environment.config as MapApplicationConfig).apply {
                    // Set here the properties
                    put("key.secret", UUID.randomUUID().toString())
                    put("database.host", sqlContainer.host)
                    put("database.port", sqlContainer.firstMappedPort.toString())
                    put("database.name", sqlContainer.databaseName)
                    put("database.user", sqlContainer.username)
                    put("database.password", sqlContainer.password)
                }
                module()
            },
            test
        )
    }

    @AfterClass
    fun cleanup() {
        sqlContainer.stop()
    }

    inner class DatabaseContainer : PostgreSQLContainer<DatabaseContainer>()
}

data class FailedResponse(
    val status: String,
    val message: String
)