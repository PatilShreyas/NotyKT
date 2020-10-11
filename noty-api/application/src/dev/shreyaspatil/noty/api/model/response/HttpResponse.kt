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

package dev.shreyaspatil.noty.api.model.response

import io.ktor.http.HttpStatusCode

/**
 * Represents HTTP response which will be exposed via API.
 */
sealed class HttpResponse<T : Response> {
    abstract val body: T
    abstract val code: HttpStatusCode

    data class Ok<T : Response>(override val body: T) : HttpResponse<T>() {
        override val code: HttpStatusCode = HttpStatusCode.OK
    }

    data class NotFound<T : Response>(override val body: T) : HttpResponse<T>() {
        override val code: HttpStatusCode = HttpStatusCode.NotFound
    }

    data class BadRequest<T : Response>(override val body: T) : HttpResponse<T>() {
        override val code: HttpStatusCode = HttpStatusCode.BadRequest
    }

    data class Unauthorized<T : Response>(override val body: T) : HttpResponse<T>() {
        override val code: HttpStatusCode = HttpStatusCode.Unauthorized
    }

    companion object {
        fun <T : Response> ok(response: T) = Ok(body = response)

        fun <T : Response> notFound(response: T) = NotFound(body = response)

        fun <T : Response> badRequest(response: T) = BadRequest(body = response)

        fun <T : Response> unauth(response: T) = Unauthorized(body = response)
    }
}

/**
 * Generates [HttpResponse] from [Response].
 */
fun generateHttpResponse(response: Response): HttpResponse<Response> {
    return when (response.status) {
        State.SUCCESS -> HttpResponse.ok(response)
        State.NOT_FOUND -> HttpResponse.notFound(response)
        State.FAILED -> HttpResponse.badRequest(response)
        State.UNAUTHORIZED -> HttpResponse.unauth(response)
    }
}