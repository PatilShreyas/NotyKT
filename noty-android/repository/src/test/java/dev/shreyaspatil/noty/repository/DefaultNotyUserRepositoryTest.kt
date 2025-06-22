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

import com.squareup.moshi.adapter
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.utils.moshi
import dev.shreyaspatil.noty.data.remote.api.NotyAuthService
import dev.shreyaspatil.noty.data.remote.model.request.AuthRequest
import dev.shreyaspatil.noty.data.remote.model.response.AuthResponse
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class DefaultNotyUserRepositoryTest {
    private lateinit var authService: FakeAuthService
    private lateinit var repository: DefaultNotyUserRepository

    @BeforeEach
    fun setup() {
        authService = spyk(FakeAuthService())
        repository = DefaultNotyUserRepository(authService)
    }

    @Test
    fun `addUser should return success with token when credentials are valid`() =
        runTest {
            // When
            val response = repository.addUser(username = "admin", password = "admin")

            // Then
            coVerify { authService.register(AuthRequest("admin", "admin")) }
            val credentials = (response as Either.Success).data
            assertEquals("Bearer ABCD", credentials.token)
        }

    @Test
    fun `addUser should return error when credentials are invalid`() =
        runTest {
            // When
            val response = repository.addUser(username = "john", password = "doe")

            // Then
            coVerify { authService.register(AuthRequest("john", "doe")) }
            val message = (response as Either.Error).message
            assertEquals("Invalid credentials", message)
        }

    @Test
    fun `getUserByUsernameAndPassword should return success with token when credentials are valid`() =
        runTest {
            // When
            val response =
                repository.getUserByUsernameAndPassword(
                    username = "admin",
                    password = "admin",
                )

            // Then
            coVerify { authService.login(AuthRequest("admin", "admin")) }
            val credentials = (response as Either.Success).data
            assertEquals("Bearer ABCD", credentials.token)
        }

    @Test
    fun `getUserByUsernameAndPassword should return error when credentials are invalid`() =
        runTest {
            // When
            val response =
                repository.getUserByUsernameAndPassword(
                    username = "john",
                    password = "doe",
                )

            // Then
            coVerify { authService.login(AuthRequest("john", "doe")) }
            val message = (response as Either.Error).message
            assertEquals("Invalid credentials", message)
        }
}

class FakeAuthService : NotyAuthService {
    override suspend fun register(authRequest: AuthRequest): Response<AuthResponse> {
        return fakeAuthResponse(authRequest)
    }

    override suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
        return fakeAuthResponse(authRequest)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeAuthResponse(authRequest: AuthRequest): Response<AuthResponse> {
        return if (authRequest.username == "admin" && authRequest.password == "admin") {
            Response.success(AuthResponse(message = "Success", token = "Bearer ABCD"))
        } else {
            val response = AuthResponse(message = "Invalid credentials", token = null)
            val body =
                ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    moshi.adapter<AuthResponse>().toJson(response),
                )
            Response.error(401, body)
        }
    }
}
