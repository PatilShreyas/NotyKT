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

/**
 * Integration tests for the Noty API application.
 * Tests authentication, note creation, retrieval, updates, and deletion.
 */
@Suppress("unused")
class ApplicationTest : AnnotationSpec() {

    // Test database container
    private val sqlContainer = DatabaseContainer()

    @BeforeClass
    fun setup() {
        sqlContainer.start()
    }

    @AfterClass
    fun cleanup() {
        sqlContainer.stop()
    }

    //region Authentication Tests

    @Test
    fun `when invalid credentials provided, registration should fail`() = testApp {
        // Invalid credentials (short password)
        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.token shouldBe null
        }
    }

    @Test
    fun `when valid credentials provided, registration and login should succeed`() = testApp {
        // Register with valid credentials
        post("/auth/register", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.OK
            body.message shouldBe "Registration successful"
            body.token shouldNotBe null
        }

        // Login with same credentials
        post("/auth/login", AuthRequest("test", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.OK
            body.message shouldBe "Login successful"
            body.token shouldNotBe null
        }
    }

    @Test
    fun `when username already exists, registration should fail`() = testApp {
        val authRequest = AuthRequest("iuser", "test1234").toJson()

        // First registration should succeed
        post("/auth/register", authRequest)

        // Second registration with same username should fail
        post("/auth/register", authRequest).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.token shouldBe null
            body.message shouldBe "Username is not available"
        }
    }

    @Test
    fun `when credentials are illegal, auth endpoints should respond with appropriate error messages`() = testApp {
        // Test various invalid credential scenarios

        // Invalid username with special characters
        post("/auth/register", AuthRequest("test#", "test1234").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "No special characters allowed in username"
            body.token shouldBe null
        }

        // Username too short
        post("/auth/register", AuthRequest("hi", "hi@12341").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Username should be of min 4 and max 30 character in length"
            body.token shouldBe null
        }

        // Password too short
        post("/auth/register", AuthRequest("test", "test").toJson()).let { response ->
            val body: AuthResponse = response.toModel()

            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Password should be of min 8 and max 50 character in length"
            body.token shouldBe null
        }

        // Same validations for login endpoint
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
    fun `when user does not exist, login should fail`() = testApp {
        post(
            "/auth/login",
            AuthRequest("usernotexists", "test1234").toJson(),
        ).let { response ->
            val body = response.toModel<AuthResponse>()

            response.status shouldBe HttpStatusCode.BadRequest
            body.message shouldBe "Invalid credentials"
            body.token shouldBe null
        }
    }

    @Test
    fun `when auth request body is missing, register should fail with appropriate message`() = testApp {
        post("/auth/register", null).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }
    }

    @Test
    fun `when auth request body is missing, login should fail with appropriate message`() = testApp {
        post("/auth/login", null).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_CREDENTIALS
        }
    }

    //endregion

    //region Authorization Tests

    @Test
    fun `when authorization token is missing, notes endpoint should return unauthorized`() = testApp {
        get("/notes/").let { response ->
            response.status shouldBe HttpStatusCode.Unauthorized
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_ACCESS_DENIED
        }
    }

    //endregion

    //region Note Management Tests

    @Test
    fun `when note request body is missing, note operations should fail with appropriate message`() = testApp {
        val token = registerUser("newnoteuser", "newnoteuser1234")

        // Create note with missing body
        post("notes/", null, "Bearer $token").let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }

        // Update note with missing body
        put("notes/testnote", null, "Bearer $token").let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_MISSING_NOTE_DETAILS
        }
    }

    @Test
    fun `when authenticated, user should be able to perform all note operations`() = testApp {
        // Create user and get token
        val token = registerUser("notemaster", "notemaster")

        // Create a new note
        val noteId = createNote(
            token = token,
            title = "Hey test",
            content = "This is note text",
        )

        // Verify note was created
        get("/notes/", "Bearer $token").let { response ->
            val body = response.toModel<NotesResponse>()

            response.status shouldBe HttpStatusCode.OK
            body.notes[0].let { note ->
                note.id shouldBe noteId
                note.title shouldBe "Hey test"
                note.note shouldBe "This is note text"
                note.created shouldNotBe null
                note.isPinned shouldBe false
            }
        }

        // Update the note
        updateNote(
            token = token,
            noteId = noteId,
            title = "Hey update",
            content = "This is updated body",
        )

        // Pin the note
        pinNote(token, noteId, true)

        // Verify note was updated and pinned
        get("/notes/", "Bearer $token").let { response ->
            val body = response.toModel<NotesResponse>()

            response.status shouldBe HttpStatusCode.OK
            body.notes shouldHaveSize 1
            body.notes[0].let { note ->
                note.id shouldBe noteId
                note.title shouldBe "Hey update"
                note.note shouldBe "This is updated body"
                note.created shouldNotBe null
                note.isPinned shouldBe true
            }
        }

        // Unpin the note
        pinNote(token, noteId, false)

        // Verify note was unpinned
        get("/notes/", "Bearer $token").let { response ->
            response.status shouldBe HttpStatusCode.OK
            val body = response.toModel<NotesResponse>()

            body.notes shouldHaveSize 1
            body.notes[0].let { note ->
                note.id shouldBe noteId
                note.title shouldBe "Hey update"
                note.note shouldBe "This is updated body"
                note.created shouldNotBe null
                note.isPinned shouldBe false
            }
        }

        // Delete the note
        deleteNote(token, noteId)

        // Verify note was deleted
        get("/notes/", "Bearer $token").let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NotesResponse>().notes shouldHaveSize 0
        }
    }

    @Test
    fun `when note created by one user, it should not be accessible by another user`() = testApp {
        // Create two users
        val userTokenA = registerUser("userA", "userA1234")
        val userTokenB = registerUser("userB", "userB1234")

        // User A creates a note
        val noteId = createNote(
            token = userTokenA,
            title = "Hey test",
            content = "This is note text",
        )

        // User B tries to delete User A's note
        delete(
            "/notes/$noteId",
            "Bearer $userTokenB",
        ).let { response ->
            response.status shouldBe HttpStatusCode.Unauthorized
            response.toModel<FailureResponse>().message shouldBe FailureMessages.MESSAGE_ACCESS_DENIED
        }
    }

    @Test
    fun `when note request is invalid, note creation should fail with appropriate message`() = testApp {
        val token = registerUser("usertest", "userA1234")

        // Test invalid title (whitespace padding)
        val noteRequest1 = NoteRequest("      Hi       ", "This is note text").toJson()
        post(
            "/notes/",
            noteRequest1,
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.BadRequest
            val body = response.toModel<NoteTaskResponse>()
            body.noteId shouldBe null
            body.message shouldBe "Title should be of min 4 and max 30 character in length"
        }

        // Test invalid note content (whitespace only)
        val noteRequest2 = NoteRequest("Hi there!", "            ").toJson()
        post(
            "/notes/",
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
    fun `when note does not exist, update and delete operations should fail with appropriate message`() = testApp {
        val token = registerUser("usernotenotexist", "test1234")
        val nonExistentNoteId = UUID.randomUUID().toString()

        // Try to update non-existent note
        put(
            "notes/$nonExistentNoteId",
            NoteRequest("Lorem ipsum", "This is body of the note").toJson(),
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.NotFound

            response.toModel<NoteTaskResponse>().let { responseBody ->
                responseBody.message shouldBe "Note not exist with ID '$nonExistentNoteId'"
                responseBody.noteId shouldBe null
            }
        }

        // Try to delete non-existent note
        delete(
            "notes/$nonExistentNoteId",
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.NotFound

            val body = response.toModel<NoteTaskResponse>()
            body.message shouldBe "Note not exist with ID '$nonExistentNoteId'"
            body.noteId shouldBe null
        }
    }

    //endregion

    //region Helper Methods

    /**
     * Registers a new user and returns the authentication token.
     */
    private suspend fun ApplicationTestBuilder.registerUser(username: String, password: String): String {
        return post(
            "/auth/register",
            AuthRequest(username, password).toJson(),
        ).toModel<AuthResponse>().token!!
    }

    /**
     * Creates a new note and returns the note ID.
     */
    private suspend fun ApplicationTestBuilder.createNote(token: String, title: String, content: String): String {
        val newNoteJson = NoteRequest(title, content).toJson()
        val response = post(
            "/notes/",
            newNoteJson,
            "Bearer $token",
        )
        response.status shouldBe HttpStatusCode.OK
        return response.toModel<NoteTaskResponse>().noteId!!
    }

    /**
     * Updates an existing note.
     */
    private suspend fun ApplicationTestBuilder.updateNote(
        token: String,
        noteId: String,
        title: String,
        content: String,
    ) {
        val updateRequest = NoteRequest(title, content).toJson()
        put(
            "/notes/$noteId",
            updateRequest,
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }
    }

    /**
     * Pins or unpins a note.
     */
    private suspend fun ApplicationTestBuilder.pinNote(token: String, noteId: String, isPinned: Boolean) {
        val pinRequest = PinRequest(isPinned).toJson()
        patch(
            "/notes/$noteId",
            pinRequest,
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }
    }

    /**
     * Deletes a note.
     */
    private suspend fun ApplicationTestBuilder.deleteNote(token: String, noteId: String) {
        delete(
            "/notes/$noteId",
            "Bearer $token",
        ).let { response ->
            response.status shouldBe HttpStatusCode.OK
            response.toModel<NoteTaskResponse>().noteId shouldNotBe null
        }
    }

    /**
     * Sets up and runs a test application with the configured database.
     */
    private fun testApp(test: suspend ApplicationTestBuilder.() -> Unit) {
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

    /**
     * PostgreSQL container for integration tests.
     */
    inner class DatabaseContainer : PostgreSQLContainer<DatabaseContainer>("postgres")

    //endregion
}
