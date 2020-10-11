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

import dev.shreyaspatil.noty.core.repository.NotyAuthRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.data.remote.api.NotyAuthService
import dev.shreyaspatil.noty.data.remote.model.request.AuthRequest
import dev.shreyaspatil.noty.data.remote.model.response.State
import dev.shreyaspatil.noty.data.remote.util.getResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

@Singleton
class DefaultNotyAuthRepository @Inject internal constructor(
    private val authService: NotyAuthService
) : NotyAuthRepository {

    override suspend fun register(
        username: String,
        password: String
    ): Flow<ResponseResult<String>> = flow {
        val authResponse = authService.register(AuthRequest(username, password)).getResponse()

        val state = when (authResponse.status) {
            State.SUCCESS -> ResponseResult.success(authResponse.token!!)
            else -> ResponseResult.error(authResponse.message)
        }
        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }

    override suspend fun login(
        username: String,
        password: String
    ): Flow<ResponseResult<String>> = flow {
        val authResponse = authService.login(AuthRequest(username, password)).getResponse()

        val state = when (authResponse.status) {
            State.SUCCESS -> ResponseResult.success(authResponse.token!!)
            else -> ResponseResult.error(authResponse.message)
        }
        emit(state)
    }.catch {
        emit(ResponseResult.error("Something went wrong!"))
    }
}
