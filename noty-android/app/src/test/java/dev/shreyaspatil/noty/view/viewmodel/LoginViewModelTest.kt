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

package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.ui.UIDataState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : BehaviorSpec({
    testCoroutineDispatcher = true
    Dispatchers.setMain(TestCoroutineDispatcher())

    val repository: NotyUserRepository = mockk()
    val sessionManager: SessionManager = mockk(relaxUnitFun = true)

    val viewModel = LoginViewModel(repository, sessionManager)

    Given("A user credentials") {
        val username = "johndoe"
        val password = "eodnhoj"

        And("Credentials are valid") {
            val token = "Bearer TOKEN_ABC"

            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.success(AuthCredential(token)))

            val states = mutableListOf<UIDataState<String>>()
            val collectStatesJob = launch { viewModel.authFlow.toList(states) }

            When("User logs in") {
                viewModel.login(username, password)

                Then("User should be get retrieved") {
                    coVerify { repository.getUserByUsernameAndPassword(username, password) }
                }

                Then("Authentication token should be get saved") {
                    verify { sessionManager.saveToken(eq(token)) }
                }

                Then("Valid UI states should be emitted") {
                    collectStatesJob.cancel()

                    states[0].isLoading shouldBe true
                    states[1].isSuccess shouldBe true
                }
            }
        }

        And("Credentials are Invalid") {
            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.error("Invalid credentials"))

            val states = mutableListOf<UIDataState<String>>()
            val collectStatesJob = launch { viewModel.authFlow.drop(1).toList(states) }

            When("User logs in") {
                viewModel.login(username, password)

                Then("User should be get retrieved") {
                    coVerify { repository.getUserByUsernameAndPassword(username, password) }
                }

                Then("Valid UI states should be emitted") {
                    collectStatesJob.cancel()

                    states[0].isLoading shouldBe true
                    states[1].isFailed shouldBe true
                }
            }
        }
    }
})
