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

package dev.shreyaspatil.noty.data.remote.interceptor

import dev.shreyaspatil.noty.core.session.SessionManager
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthInterceptorTest {

    private lateinit var sessionManager: SessionManager
    private lateinit var interceptor: AuthInterceptor
    private lateinit var expectedRequest: Request
    private lateinit var requestBuilder: Request.Builder
    private lateinit var chain: FakeChain

    @BeforeEach
    fun setup() {
        sessionManager = mockk()
        interceptor = AuthInterceptor(sessionManager)

        // Init mocks
        expectedRequest = mockk()
        requestBuilder = mockk {
            every { header(any(), any()) } returns this
            every { build() } returns expectedRequest
        }
        chain = FakeChain(requestBuilder)
    }

    @Test
    fun `intercept should add auth token to header when token is available`() {
        // Given
        every { sessionManager.getToken() } returns "ABCD1234"

        // When
        interceptor.intercept(chain)

        // Then
        verify { requestBuilder.header("Authorization", "Bearer ABCD1234") }
        assertEquals(expectedRequest, chain.proceededRequest)
    }

    @Test
    fun `intercept should not add auth token to header when token is not available`() {
        // Given
        clearAllMocks(answers = false)
        every { sessionManager.getToken() } returns null

        // When
        interceptor.intercept(chain)

        // Then
        verify(exactly = 0) { requestBuilder.header("Authorization", any()) }
        assertEquals(expectedRequest, chain.proceededRequest)
    }
}

/**
 * Fake implementation of [Interceptor.Chain]
 */
class FakeChain(private val requestBuilder: Request.Builder) : Interceptor.Chain {
    var proceededRequest: Request? = null
        private set

    override fun call(): Call {
        TODO("Not yet implemented")
    }

    override fun connectTimeoutMillis(): Int {
        TODO("Not yet implemented")
    }

    override fun connection(): Connection? {
        TODO("Not yet implemented")
    }

    override fun proceed(request: Request): Response {
        proceededRequest = request
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .message("")
            .code(200)
            .build()
    }

    override fun readTimeoutMillis(): Int {
        TODO("Not yet implemented")
    }

    override fun request(): Request {
        return mockk {
            every { newBuilder() } returns
                mockk {
                    every { newBuilder() } returns requestBuilder
                }
        }
    }

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        TODO("Not yet implemented")
    }

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        TODO("Not yet implemented")
    }

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        TODO("Not yet implemented")
    }

    override fun writeTimeoutMillis(): Int {
        TODO("Not yet implemented")
    }
}
