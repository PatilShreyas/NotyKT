/*
 * Copyright 2021 Shreyas Patil
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

package dev.shreyaspatil.noty.api.plugin

import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.response.FailureResponse
import dev.shreyaspatil.noty.api.model.response.State
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.BadRequestException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> {
            call.respond(HttpStatusCode.BadRequest, FailureResponse(State.FAILED, it.message ?: "Bad Request"))
        }

        status(HttpStatusCode.InternalServerError) {
            call.respond(it, FailureResponse(State.FAILED, FailureMessages.MESSAGE_FAILED))
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(it, FailureResponse(State.UNAUTHORIZED, FailureMessages.MESSAGE_ACCESS_DENIED))
        }
    }
}