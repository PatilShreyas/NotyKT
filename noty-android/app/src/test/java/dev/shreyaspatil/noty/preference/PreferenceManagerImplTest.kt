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

package dev.shreyaspatil.noty.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import java.io.IOException

class PreferenceManagerImplTest : BehaviorSpec() {

    // This is unused but it's necessary to mock the extension function on DataStore
    val editMock = mockkStatic(DataStore<Preferences>::edit)

    private val preferences: MutablePreferences = mockk(relaxUnitFun = true)

    private val dataStore: DataStore<Preferences> = mockk {
        every { data } returns flow {
            emit(preference(null))
            emit(preference(true))
            emit(preference(false))
            throw IOException("Fake error")
        }

        coEvery { edit(captureLambda()) } coAnswers {
            lambda<suspend (MutablePreferences) -> Unit>().coInvoke(preferences)
            preference(true)
        }
    }

    private val manager = PreferenceManagerImpl(dataStore)

    init {
        Given("The UI mode") {
            When("UI Preferences are retrieved") {
                val modes = manager.uiModeFlow.toList()

                Then("Valid UI preferences should be emitted") {
                    modes shouldBe listOf(false, true, false, false)
                }
            }

            When("The UI preference is updated") {
                manager.setDarkMode(true)

                Then("UI preference should be updated") {
                    verify {
                        preferences.set(PreferenceManagerImpl.IS_DARK_MODE, true)
                    }
                }
            }
        }
    }

    private fun preference(value: Boolean?) = mockk<Preferences> {
        every { get(PreferenceManagerImpl.IS_DARK_MODE) } returns value
    }
}
