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

import com.fasterxml.jackson.databind.SerializationFeature
import dev.shreyaspatil.noty.api.auth.NotyJWT
import dev.shreyaspatil.noty.api.di.DaggerControllerComp
import dev.shreyaspatil.noty.api.exception.FailureMessages
import dev.shreyaspatil.noty.api.model.response.Response
import dev.shreyaspatil.noty.api.model.response.State
import dev.shreyaspatil.noty.api.route.auth
import dev.shreyaspatil.noty.api.route.notes
import dev.shreyaspatil.noty.api.utils.KeyProvider
import dev.shreyaspatil.noty.data.dao.UserDao
import dev.shreyaspatil.noty.data.database.initDatabase
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.module() {

    with(Config(environment.config)) {
        initWithSecret(SECRET_KEY)
        initDatabase(
            host = DATABASE_HOST,
            port = DATABASE_PORT,
            databaseName = DATABASE_NAME,
            user = DATABASE_USER,
            password = DATABASE_PASSWORD
        )
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }

    // JWT Authentication
    install(Authentication) {
        jwt {
            verifier(NotyJWT.instance.verifier)
            validate {
                val claim = it.payload.getClaim(NotyJWT.ClAIM).asString()
                if (claim.let(UserDao()::isUserExists)) {
                    UserIdPrincipal(claim)
                } else {
                    null
                }
            }
        }
    }

    install(StatusPages) {
        exception<BadRequestException> {
            call.respond(HttpStatusCode.BadRequest, getResponse(State.FAILED, it.message ?: "Bad Request"))
        }

        status(HttpStatusCode.InternalServerError) {
            call.respond(it, getResponse(State.FAILED, FailureMessages.MESSAGE_FAILED))
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(it, getResponse(State.UNAUTHORIZED, FailureMessages.MESSAGE_ACCESS_DENIED))
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val controllers = DaggerControllerComp.create()

    routing {
        auth(controllers.authController())
        notes(controllers.notesController())
    }
}

fun initWithSecret(secretKey: String) {
    NotyJWT.initialize(secretKey)
    KeyProvider.initialize(secretKey)
}

fun getResponse(state: State, message: String): Response {
    return object : Response {
        override val status: State = state
        override val message: String = message
    }
}