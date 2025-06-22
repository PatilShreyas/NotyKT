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

package dev.shreyaspatil.noty.data.remote.util

import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.utils.fromJson
import dev.shreyaspatil.noty.data.remote.model.response.BaseResponse
import retrofit2.Response

inline fun <reified T : BaseResponse, R> Response<T>.either(
    errorMessage: (T) -> String = { it.message },
    map: (T) -> R,
): Either<R> {
    return try {
        val responseBody = body()
        return if (this.isSuccessful && responseBody != null) {
            Either.success(map(responseBody))
        } else {
            Either.error(errorMessage(fromJson<T>(errorBody()!!.string())!!))
        }
    } catch (error: Throwable) {
        Either.error("Error occurred while performing this operation. Try again.")
    }
}
