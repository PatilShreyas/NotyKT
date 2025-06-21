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

package dev.shreyaspatil.noty.api.route

import dagger.Lazy
import dev.shreyaspatil.noty.api.auth.principal.UserPrincipal
import dev.shreyaspatil.noty.api.controller.NotesController
import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.exception.UnauthorizedAccessException
import dev.shreyaspatil.noty.api.model.request.NoteRequest
import dev.shreyaspatil.noty.api.model.request.PinRequest
import dev.shreyaspatil.noty.api.plugin.controllers
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.notes(notesController: Lazy<NotesController> = controllers.notesController()) {
    authenticate {
        get("/notes") {
            val principal = userPrincipal()

            val notesResponse = notesController.get().getNotesByUser(principal.user)

            call.respond(notesResponse)
        }

        route("/note/") {
            post("/new") {
                val noteRequest = runCatching { call.receive<NoteRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_NOTE_DETAILS)
                }

                val principal = userPrincipal()

                val noteResponse = notesController.get().addNote(principal.user, noteRequest)

                call.respond(noteResponse)
            }

            put("/{id}") {
                val noteId = call.parameters["id"] ?: return@put
                val noteRequest = runCatching { call.receive<NoteRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_NOTE_DETAILS)
                }

                val principal = userPrincipal()

                val noteResponse = notesController.get().updateNote(principal.user, noteId, noteRequest)

                call.respond(noteResponse)
            }

            delete("/{id}") {
                val noteId = call.parameters["id"] ?: return@delete
                val principal = userPrincipal()

                val noteResponse = notesController.get().deleteNote(principal.user, noteId)

                call.respond(noteResponse)
            }

            patch("/{id}/pin") {
                val noteId = call.parameters["id"] ?: return@patch
                val pinRequest = runCatching { call.receive<PinRequest>() }.getOrElse {
                    throw BadRequestException(FailureMessages.MESSAGE_MISSING_PIN_DETAILS)
                }

                val principal = userPrincipal()

                val noteResponse = notesController.get().updateNotePin(principal.user, noteId, pinRequest)

                call.respond(noteResponse)
            }
        }
    }
}

private fun RoutingContext.userPrincipal(): UserPrincipal = (
    call.principal<UserPrincipal>()
        ?: throw UnauthorizedAccessException(FailureMessages.MESSAGE_ACCESS_DENIED)
    )
