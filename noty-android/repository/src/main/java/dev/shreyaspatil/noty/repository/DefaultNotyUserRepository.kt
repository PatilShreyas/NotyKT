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

package dev.shreyaspatil.noty.repository

import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.data.remote.api.NotyAuthService
import dev.shreyaspatil.noty.data.remote.model.request.AuthRequest
import dev.shreyaspatil.noty.data.remote.model.response.State
import dev.shreyaspatil.noty.data.remote.util.getResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of data of User of the app.
 */
@Singleton
class DefaultNotyUserRepository @Inject internal constructor(
    private val authService: NotyAuthService
) : NotyUserRepository {

    override suspend fun addUser(
        username: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val authResponse = authService.register(AuthRequest(username, password)).getResponse()

            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(authResponse.token!!))
                else -> Either.error(authResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    override suspend fun getUserByUsernameAndPassword(
        username: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val authResponse = authService.login(AuthRequest(username, password)).getResponse()

            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(authResponse.token!!))
                else -> Either.error(authResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }
}
