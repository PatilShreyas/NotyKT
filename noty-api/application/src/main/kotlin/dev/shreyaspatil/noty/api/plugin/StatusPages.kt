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

import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.exception.ResourceNotFoundException
import dev.shreyaspatil.noty.api.exception.UnauthorizedAccessException
import dev.shreyaspatil.noty.api.model.response.FailureResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                FailureResponse(cause.message),
            )
        }
        exception<UnauthorizedAccessException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                FailureResponse(cause.message),
            )
        }
        exception<ResourceNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                FailureResponse(cause.message),
            )
        }

        status(HttpStatusCode.InternalServerError) { call, _ ->
            call.respond(
                HttpStatusCode.InternalServerError,
                FailureResponse(FailureMessages.MESSAGE_FAILED),
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                FailureResponse(FailureMessages.MESSAGE_ACCESS_DENIED),
            )
        }
    }
}
