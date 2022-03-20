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

package dev.shreyaspatil.noty.composeapp.testUtil

import dev.shreyaspatil.noty.core.utils.json
import okhttp3.ResponseBody
import retrofit2.Response

inline fun <reified T> errorResponse(
    code: Int,
    response: T
): Response<T> = Response.error(code, ResponseBody.create(null, response.json))

inline fun <reified T> successResponse(response: T): Response<T> = Response.success(response)
