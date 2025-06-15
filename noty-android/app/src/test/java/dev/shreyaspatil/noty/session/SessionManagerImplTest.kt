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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SessionManagerImplTest {
    private lateinit var preferenceEditor: SharedPreferences.Editor
    private lateinit var preference: SharedPreferences
    private lateinit var manager: SessionManagerImpl

    @BeforeEach
    fun setup() {
        preferenceEditor =
            mockk {
                every { putString(any(), any()) } returns this
                every { clear() } returns this
                every { commit() } returns true
            }

        preference =
            mockk(relaxUnitFun = true) {
                every { edit() } returns preferenceEditor
            }

        manager = SessionManagerImpl(preference)
    }

    @Test
    fun `saveToken should save token in preference storage`() {
        // Given
        val expectedToken = "Bearer ABCD"

        // When
        manager.saveToken(expectedToken)

        // Then
        verifySequence {
            preference.edit()
            preferenceEditor.putString("auth_token", expectedToken)
            preferenceEditor.commit()
        }
    }

    @Test
    fun `getToken should retrieve token from preference storage`() {
        // Given
        val expectedToken = "Bearer ABCD"
        every { preference.getString("auth_token", any()) } returns expectedToken

        // When
        val actualToken = manager.getToken()

        // Then
        verify(exactly = 1) { preference.getString("auth_token", null) }
        assertEquals(expectedToken, actualToken)
    }
}
