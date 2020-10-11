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

import dev.shreyaspatil.noty.api.controller.AuthController
import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.request.AuthRequest
import dev.shreyaspatil.noty.api.model.response.generateHttpResponse
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalAPI
fun Route.auth(authController: AuthController) {

    route("/auth") {
        post("/register") {
            val authRequest = runCatching { call.receive<AuthRequest>() }.getOrElse {
                throw BadRequestException(FailureMessages.MESSAGE_MISSING_CREDENTIALS)
            }

            val authResponse = authController.register(authRequest.username, authRequest.password)
            val response = generateHttpResponse(authResponse)

            call.respond(response.code, response.body)
        }

        post("/login") {
            val authRequest = runCatching { call.receive<AuthRequest>() }.getOrElse {
                throw BadRequestException(FailureMessages.MESSAGE_MISSING_CREDENTIALS)
            }

            val authResponse = authController.login(authRequest.username, authRequest.password)
            val response = generateHttpResponse(authResponse)

            call.respond(response.code, response.body)
        }
    }
}