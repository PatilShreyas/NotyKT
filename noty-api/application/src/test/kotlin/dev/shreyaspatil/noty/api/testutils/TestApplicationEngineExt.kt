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

package dev.shreyaspatil.noty.api.testutils

import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.ApplicationTestBuilder

suspend fun ApplicationTestBuilder.get(url: String, token: String? = null) =
    getResponse(HttpMethod.Get, url, null, token)

suspend fun ApplicationTestBuilder.put(url: String, body: String?, token: String? = null) =
    getResponse(HttpMethod.Put, url, body, token)

suspend fun ApplicationTestBuilder.patch(url: String, body: String?, token: String? = null) =
    getResponse(HttpMethod.Patch, url, body, token)

suspend fun ApplicationTestBuilder.post(url: String, body: String?, token: String? = null) =
    getResponse(HttpMethod.Post, url, body, token)

suspend fun ApplicationTestBuilder.delete(url: String, token: String? = null) =
    getResponse(HttpMethod.Delete, url, null, token)

suspend fun ApplicationTestBuilder.getResponse(
    method: HttpMethod,
    url: String,
    body: String? = null,
    token: String? = null,
) = client.request {
    this.method = method
    this.url(url)
    if (method != HttpMethod.Get) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        body?.let { setBody(it) }
    }
    token?.let { header(HttpHeaders.Authorization, it) }
}
