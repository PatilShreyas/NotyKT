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

import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.request.AuthRequest
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.model.response.AuthResponse
import dev.shreyaspatil.noty.api.model.response.FailureResponse
import dev.shreyaspatil.noty.api.model.response.NoteTaskResponse
import dev.shreyaspatil.noty.api.model.response.NotesResponse
import dev.shreyaspatil.noty.api.testutils.delete
import dev.shreyaspatil.noty.api.testutils.get
import dev.shreyaspatil.noty.api.testutils.patch
import dev.shreyaspatil.noty.api.testutils.post
import dev.shreyaspatil.noty.api.testutils.put
import dev.shreyaspatil.noty.api.testutils.toJson
import dev.shreyaspatil.noty.api.testutils.toModel
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID

@Suppress("unused")
class ApplicationTest : AnnotationSpec() {

    private val sqlContainer = DatabaseContainer()

    @BeforeClass
    fun setup() {
        sqlContainer.start()
    }

    @Test
    fun whenPassedInvalidCredentials_responseStateShouldBeFailed() = testApp {
        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.token shouldBe null
        }
    }

    @Test
    fun whenRegistrationIsSuccessful_shouldBeAbleToLogin() = testApp {
        post("/auth/register", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.OK
            body.message shouldBe "Registration successful"
            body.token shouldNotBe null
        }

        post("/auth/login", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.OK
            body.message shouldBe "Login successful"
            body.token shouldNotBe null
        }
    }

    @Test
    fun whenRegistrationIsSuccessful_whenTryingToRegisterAgainWithSameUsername_shouldRespondFailedState() = testApp {
        val authRequest = AuthRequest("iuser", "test1234").toJson()
        post("/auth/register", authRequest)

        // Try to create again. It should show username not available message.
        post("/auth/register", authRequest).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.token shouldBe null
            body.message shouldBe "Username is not available"
        }
    }

    @Test
    fun whenCredentialsAreIllegal_shouldRespondFailedStateWithMessage() = testApp {
        post("/auth/register", AuthRequest("test#", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "No special characters allowed in username"
            body.token shouldBe null
        }

        post("/auth/register", AuthRequest("hi", "hi@12341").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Username should be of min 4 and max 30 character in length"
            body.token shouldBe null
        }

        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Password should be of min 8 and max 50 character in length"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("test#", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "No special characters allowed in username"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("hi", "hi@12341").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Username should be of min 4 and max 30 character in length"
            body.token shouldBe null
        }

        post("/auth/login", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.toModel()
            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Password should be of min 8 and max 50 character in length"
            body.token shouldBe null
        }
    }

    @Test
    fun whenProvidedInvalidCredentials_shouldFailAndShowError() = testApp {
        post(
            "/auth/login",
            AuthRequest("usernotexists", "test1234").toJson(),
        ).let {
            val body = it.toModel<AuthResponse>()
            it.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Invalid credentials"
            body.token shouldBe null
        }
    }

    @Test
    fun authorizationKeyIsNotProvided_whenNotesAreRetrieved_shouldGetUnauthResponse() = testApp {
        get("/notes")
            .let { response ->
                response.status shouldBe HttpStatusCode.Unauthorized
                response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_ACCESS_DENIED
            }
    }

    @Test
    fun whenProvidedInvalidAuthBody_shouldThrowException() = testApp {
        post("/auth/register", null).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }
    }

    @Test
    fun whenProvidedInvalidAuthBodyForLogin_shouldThrowException() = testApp {
        post("/auth/login", null).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }
    }

    @Test
    fun whenProvidedInvalidNoteBody_shouldThrowException() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("newnoteuser", "newnoteuser1234").toJson(),
        ).toModel<AuthResponse>().token

        post("note/new", null, "Bearer $token").let {
            it.status shouldBe HttpStatusCode.BadRequest
            it.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }

        put("note/testnote", null, "Bearer $token").let {
            it.status shouldBe HttpStatusCode.BadRequest
            it.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }
    }

    @Test
    fun whenUserIsAuthenticated_shouldBeAbleToAddUpdateDeleteNotes() = testApp {
        // Create user
        val authResponse = post(
            "/auth/register",
            AuthRequest("notemaster", "notemaster").toJson(),
        ).toModel<AuthResponse>()

        // Create note
        val newNoteJson = NoteRequest("Hey test", "This is note text").toJson()

        val newNoteTaskHttpResponse = post(
            "/note/new",
            newNoteJson,
            "Bearer ${authResponse.token}",
        )
        val newNoteTaskResponse = newNoteTaskHttpResponse.toModel<NoteTaskResponse>()
        newNoteTaskHttpResponse.status shouldBe HttpStatusCode.OK
        newNoteTaskResponse.noteId shouldNotBe null

        // Get Notes
        get("/notes", "Bearer ${authResponse.token}").let { response ->
            val body = response.toModel<NotesResponse>()
            response.status shouldBe HttpStatusCode.OK
            body.notes[0].let {
                it.id shouldBe newNoteTaskResponse.noteId
                it.title shouldBe "Hey test"
                it.note shouldBe "This is note text"
                it.created shouldNotBe null
                it.isPinned shouldBe false
            }
        }

        // Update note
        val updateRequest = NoteRequest("Hey update", "This is updated body").toJson()
        put(
            "/note/${newNoteTaskResponse.noteId}",
            updateRequest,
            "Bearer ${authResponse.token}",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }

        // pin note
        val pinRequest = PinRequest(isPinned = true).toJson()
        patch(
            "/note/${newNoteTaskResponse.noteId}/pin",
            pinRequest,
            "Bearer ${authResponse.token}",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }

        get("/notes", "Bearer ${authResponse.token}").let { response ->
            val body = response.toModel<NotesResponse>()
            response.status shouldBe HttpStatusCode.OK
            body.notes shouldHaveSize 1
            body.notes[0].let {
                it.id shouldBe newNoteTaskResponse.noteId
                it.title shouldBe "Hey update"
                it.note shouldBe "This is updated body"
                it.created shouldNotBe null
                it.isPinned shouldBe true
            }
        }

        val unpinRequest = PinRequest(isPinned = false).toJson()
        patch(
            "/note/${newNoteTaskResponse.noteId}/pin",
            unpinRequest,
            "Bearer ${authResponse.token}",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }

        get("/notes", "Bearer ${authResponse.token}").let { response ->
            response.status shouldBe HttpStatusCode.OK
            val body = response.toModel<NotesResponse>()
            body.notes shouldHaveSize 1
            body.notes[0].let {
                it.id shouldBe newNoteTaskResponse.noteId
                it.title shouldBe "Hey update"
                it.note shouldBe "This is updated body"
                it.created shouldNotBe null
                it.isPinned shouldBe false
            }
        }

        // Delete note
        delete(
            "/note/${newNoteTaskResponse.noteId}",
            "Bearer ${authResponse.token}",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }

        // Get empty notes
        get("/notes", "Bearer ${authResponse.token}").let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NotesResponse>().notes shouldHaveSize 0
        }
    }

    @Test
    fun whenNoteIsCreatedByUserA_shouldNotBeAccessibleByUserB() = testApp {
        // Create User A
        val userTokenA = post(
            "/auth/register",
            AuthRequest("userA", "userA1234").toJson(),
        ).toModel<AuthResponse>().token

        // Create User B
        val userTokenB = post(
            "/auth/register",
            AuthRequest("userB", "userB1234").toJson(),
        ).toModel<AuthResponse>().token

        // User A creates note
        val noteRequest = NoteRequest("Hey test", "This is note text").toJson()
        val noteId = post(
            "/note/new",
            noteRequest,
            "Bearer $userTokenA",
        ).toModel<NoteTaskResponse>().noteId

        // User B tries to delete note created by user A
        // Should show access denied (Unauthorized access) message.
        delete(
            "/note/$noteId",
            "Bearer $userTokenB",
        ).let { response ->
            response.status shouldBe HttpStatusCode.Unauthorized
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_ACCESS_DENIED
        }
    }

    @Test
    fun whenNoteRequestIsInvalid_shouldRespondFailedStateWithMessage() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("usertest", "userA1234").toJson(),
        ).toModel<AuthResponse>().token
        println(token)

        // Create note with invalid title (Whitespaces)
        val noteRequest1 = NoteRequest("      Hi       ", "This is note text").toJson()
        post(
            "/note/new",
            noteRequest1,
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            val body = response.toModel<NoteTaskResponse>()
            body.noteId shouldBe null
            body.message shouldBe "Title should be of min 4 and max 30 character in length"
        }

        // Create note with invalid note text (Whitespaces)
        val noteRequest2 = NoteRequest("Hi there!", "            ").toJson()
        post(
            "/note/new",
            noteRequest2,
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            val body = response.toModel<NoteTaskResponse>()
            body.noteId shouldBe null
            body.message shouldBe "Title and Note should not be blank"
        }
    }

    @Test
    fun whenNoteNotExists_requestedUpdateDelete_shouldShowErrorMessage() = testApp {
        val token = post(
            "/auth/register",
            AuthRequest("usernotenotexist", "test1234").toJson(),
        ).toModel<AuthResponse>().token

        val noteId = UUID.randomUUID().toString()
        put(
            "note/$noteId",
            NoteRequest("Lorem ipsum", "This is body of the note").toJson(),
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.NotFound

            response.toModel<NoteTaskResponse>().let { response ->
                response.message shouldBe "Note not exist with ID '$noteId'"
                response.noteId shouldBe null
            }
        }

        delete(
            "note/$noteId",
            "Bearer $token",
        ).let {
            it.status shouldBe HttpStatusCode.NotFound

            val body = it.toModel<NoteTaskResponse>()
            body.message shouldBe "Note not exist with ID '$noteId'"
            body.noteId shouldBe null
        }
    }

    fun testApp(test: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "key.secret" to UUID.randomUUID().toString(),
                    "database.host" to sqlContainer.host,
                    "database.port" to sqlContainer.firstMappedPort.toString(),
                    "database.name" to sqlContainer.databaseName,
                    "database.user" to sqlContainer.username,
                    "database.maxPoolSize" to "3",
                    "database.driver" to sqlContainer.driverClassName,
                    "database.password" to sqlContainer.password,
                )
            }
            application {
                module()
            }
            test()
        }
    }

    @AfterClass
    fun cleanup() {
        sqlContainer.stop()
    }

    inner class DatabaseContainer : PostgreSQLContainer<DatabaseContainer>("postgres")
}
