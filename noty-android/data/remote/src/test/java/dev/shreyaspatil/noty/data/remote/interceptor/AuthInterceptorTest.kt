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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class AuthInterceptorTest : BehaviorSpec({

    val sessionManager: SessionManager = mockk()
    val interceptor = AuthInterceptor(sessionManager)

    // Init mocks
    val expectedRequest: Request = mockk()
    val requestBuilder: Request.Builder = mockk {
        every { header(any(), any()) } returns this
        every { build() } returns expectedRequest
    }
    val chain = FakeChain(requestBuilder)

    Given("An auth token") {
        every { sessionManager.getToken() } returns "ABCD1234"

        When("The request goes through interceptor") {
            interceptor.intercept(chain)

            Then("Auth Bearer Token should get added in the header") {
                verify { requestBuilder.header("Authorization", "Bearer ABCD1234") }
            }

            Then("The chain should proceed with the new request") {
                chain.proceededRequest shouldBe expectedRequest
            }
        }
    }

    Given("No auth token available") {
        clearAllMocks(answers = false)
        every { sessionManager.getToken() } returns null

        When("The request goes through interceptor") {
            interceptor.intercept(chain)

            Then("Auth Bearer Token should NOT get added in the header") {
                verify(exactly = 0) { requestBuilder.header("Authorization", any()) }
            }

            Then("The chain should proceed with the new request") {
                chain.proceededRequest shouldBe expectedRequest
            }
        }
    }
})

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
            every { newBuilder() } returns mockk {
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
