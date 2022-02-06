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

package dev.shreyaspatil.noty.session

import android.content.SharedPreferences
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence

class SessionManagerImplTest : BehaviorSpec({
    val preferenceEditor: SharedPreferences.Editor = mockk {
        every { putString(any(), any()) } returns this
        every { clear() } returns this
        every { commit() } returns true
    }

    val preference: SharedPreferences = mockk(relaxUnitFun = true) {
        every { edit() } returns preferenceEditor
    }

    val manager = SessionManagerImpl(preference)

    Given("A authentication token") {
        val expectedToken = "Bearer ABCD"
        every { preference.getString("auth_token", any()) } returns expectedToken

        When("The token is saved in the preference storage") {
            manager.saveToken(expectedToken)

            Then("Token should get saved in the preference storage") {
                verifySequence {
                    preference.edit()
                    preferenceEditor.putString("auth_token", expectedToken)
                    preferenceEditor.commit()
                }
            }
        }

        When("The token is retrieved from storage") {
            val actualToken = manager.getToken()

            Then("Manager should request token from preference storage") {
                verify(exactly = 1) { preference.getString("auth_token", null) }
            }

            Then("Retrieved token should be valid") {
                actualToken shouldBe expectedToken
            }
        }
    }
})
