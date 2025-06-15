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
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class PreferenceManagerImplTest {
    // This is unused but it's necessary to mock the extension function on DataStore
    private val editMock = mockkStatic(DataStore<Preferences>::edit)
    private lateinit var preferences: MutablePreferences
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var manager: PreferenceManagerImpl

    @BeforeEach
    fun setup() {
        preferences = mockk(relaxUnitFun = true)

        dataStore =
            mockk {
                every { data } returns
                    flow {
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

        manager = PreferenceManagerImpl(dataStore)
    }

    @Test
    fun `uiModeFlow should emit valid UI preferences`() =
        runTest {
            // When
            val modes = manager.uiModeFlow.toList()

            // Then
            assertEquals(listOf(false, true, false, false), modes)
        }

    @Test
    fun `setDarkMode should update UI preference`() =
        runTest {
            // When
            manager.setDarkMode(true)

            // Then
            verify {
                preferences.set(PreferenceManagerImpl.IS_DARK_MODE, true)
            }
        }

    private fun preference(value: Boolean?) =
        mockk<Preferences> {
            every { get(PreferenceManagerImpl.IS_DARK_MODE) } returns value
        }
}
