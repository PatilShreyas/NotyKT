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
import dev.shreyaspatil.noty.api.controller.AuthController
import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.request.AuthRequest
import dev.shreyaspatil.noty.api.plugin.controllers
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userAuthentication(authController: Lazy<AuthController> = controllers.authController()) {
    route("/auth") {
        post("/register") {
            val authRequest = authRequest()

            val authResponse = authController.get().register(
                username = authRequest.username,
                password = authRequest.password,
            )

            call.respond(authResponse)
        }

        post("/login") {
            val authRequest = authRequest()

            val authResponse = authController.get().login(
                username = authRequest.username,
                password = authRequest.password,
            )

            call.respond(authResponse)
        }
    }
}

private suspend fun RoutingContext.authRequest(): AuthRequest = runCatching { call.receive<AuthRequest>() }.getOrElse {
    throw BadRequestException(FailureMessages.MESSAGE_MISSING_CREDENTIALS)
}
