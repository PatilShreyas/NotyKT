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

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.LoginState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : ViewModelBehaviorSpec({

    val repository: NotyUserRepository = mockk()
    val sessionManager: SessionManager = mockk(relaxUnitFun = true)

    val viewModel = LoginViewModel(repository, sessionManager)

    Given("The ViewModel") {
        When("Initialized") {
            val expectedState = LoginState(
                isLoading = false,
                isLoggedIn = false,
                error = null,
                username = "",
                password = "",
                isValidUsername = null,
                isValidPassword = null
            )
            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }
    }

    Given("A username and password") {
        val username = "johndoe"
        val password = "eodnhoj"

        When("Username is set") {
            viewModel.setUsername(username)

            Then("Username should be updated in the current state") {
                viewModel.withState { this.username shouldBe username }
            }
        }

        When("Password is set") {
            viewModel.setPassword(password)

            Then("Password should be updated in the current state") {
                viewModel.withState { this.password shouldBe password }
            }
        }
    }

    Given("A user credentials") {
        And("The user provides incomplete credentials") {
            val username = "john"
            val password = "eod"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            When("User logs in") {
                viewModel.login()

                Then("Credentials should be validated and state should be updated") {
                    viewModel.withState {
                        isValidUsername shouldBe true
                        isValidPassword shouldBe false
                    }
                }

                Then("User should NOT be get retrieved") {
                    coVerify(exactly = 0) {
                        repository.getUserByUsernameAndPassword(username, password)
                    }
                }
            }
        }

        And("User uses valid credentials") {
            val username = "johndoe1234"
            val password = "4321eodnhoj"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            val token = "Bearer TOKEN_ABC"

            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.success(AuthCredential(token)))

            When("User logs in") {
                viewModel.login()

                Then("User should be get retrieved") {
                    coVerify { repository.getUserByUsernameAndPassword(username, password) }
                }

                Then("Authentication token should be get saved") {
                    verify { sessionManager.saveToken(eq(token)) }
                }

                Then("Credentials should be validated") {
                    viewModel.withState {
                        isValidUsername shouldBe true
                        isValidPassword shouldBe true
                    }
                }

                Then("Valid UI states should be updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        isLoggedIn shouldBe true
                        error shouldBe null
                    }
                }
            }
        }

        And("Repository fails to fulfil the request") {
            val username = "johndoe12345"
            val password = "54321eodnhoj"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.error("User not exist"))

            When("User logs in") {
                viewModel.login()

                Then("User should be get retrieved") {
                    coVerify { repository.getUserByUsernameAndPassword(username, password) }
                }

                Then("State should contain error") {
                    viewModel.withState {
                        isLoggedIn shouldBe false
                        error shouldBe "User not exist"
                    }
                }
            }
        }
    }
})
