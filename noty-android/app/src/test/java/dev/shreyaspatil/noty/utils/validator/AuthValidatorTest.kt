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

package dev.shreyaspatil.noty.utils.validator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

class AuthValidatorTest : BehaviorSpec({
    Given("Usernames") {
        And("Usernames are valid") {
            val usernames = listOf("johndoe", "johndoe1234", "njvearjgnuiw5895h89oh")

            When("Usernames are validated") {
                val areUsernamesValid = usernames.map { AuthValidator.isValidUsername(it) }

                Then("Usernames should be valid") {
                    areUsernamesValid shouldContain true
                    areUsernamesValid shouldNotContain false
                }
            }
        }

        And("Usernames are invalid") {
            val usernames = listOf("123", "joh", "11njvearjgnuiw5895h89oh456tre54y", "    hey    ")

            When("Usernames are validated") {
                val areUsernamesValid = usernames.map { AuthValidator.isValidUsername(it) }

                Then("Usernames should be invalid") {
                    areUsernamesValid shouldContain false
                    areUsernamesValid shouldNotContain true
                }
            }
        }
    }

    Given("Passwords") {
        And("Passwords are valid") {
            val passwords = listOf("heythere", "johndoe1234", "njvearjgnuiw5895h89oh")

            When("Passwords are validated") {
                val arePasswordsValid = passwords.map { AuthValidator.isValidPassword(it) }

                Then("Passwords should be valid") {
                    arePasswordsValid shouldContain true
                    arePasswordsValid shouldNotContain false
                }
            }
        }

        And("Passwords are invalid") {
            val passwords = listOf(
                "12345", // Short
                "johndoe", // Short
                "   hey       ", // Including whitespace
                "123456789012345678901234567890123456789012345678901234567890" // More than 50 chars
            )

            When("Passwords are validated") {
                val arePasswordsValid = passwords.map { AuthValidator.isValidPassword(it) }

                Then("Passwords should be invalid") {
                    arePasswordsValid shouldContain false
                    arePasswordsValid shouldNotContain true
                }
            }
        }
    }

    Given("Password and confirm password") {
        And("Both are same") {
            val password = "password1234"
            val confirmPassword = "password1234"

            When("They are checked whether they are same") {
                val areSame = AuthValidator.isPasswordAndConfirmPasswordSame(
                    password = password,
                    confirmedPassword = confirmPassword
                )

                Then("They should be the same") {
                    areSame shouldBe true
                }
            }
        }

        And("Both are NOT same") {
            val password = "password"
            val confirmPassword = "confirmPassword"

            When("They are checked whether they are same") {
                val areSame = AuthValidator.isPasswordAndConfirmPasswordSame(
                    password = password,
                    confirmedPassword = confirmPassword
                )

                Then("They should NOT be the same") {
                    areSame shouldBe false
                }
            }
        }
    }
})
