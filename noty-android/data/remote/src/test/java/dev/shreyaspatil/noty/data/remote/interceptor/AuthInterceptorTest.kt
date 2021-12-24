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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request

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
    val chain: Interceptor.Chain = mockk {
        every { proceed(any()) } returns mockk()
        every { request() } returns mockk {
            every { newBuilder() } returns mockk {
                every { newBuilder() } returns requestBuilder
            }
        }
    }

    Given("An auth token") {
        every { sessionManager.getToken() } returns "ABCD1234"

        When("The request goes through interceptor") {
            interceptor.intercept(chain)

            Then("Auth Bearer Token should get added in the header") {
                verify { requestBuilder.header("Authorization", "Bearer ABCD1234") }
            }

            Then("The chain should proceed with the new request") {
                verify { chain.proceed(expectedRequest) }
            }
        }
    }
})
