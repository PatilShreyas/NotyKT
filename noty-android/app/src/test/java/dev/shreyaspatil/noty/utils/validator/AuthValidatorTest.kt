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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthValidatorTest {
    @Test
    fun `isValidUsername should return true for valid usernames`() {
        // Given
        val validUsernames = listOf("johndoe", "johndoe1234", "njvearjgnuiw5895h89oh")

        // When & Then
        validUsernames.forEach { username ->
            assertTrue(AuthValidator.isValidUsername(username))
        }
    }

    @Test
    fun `isValidUsername should return false for invalid usernames`() {
        // Given
        val invalidUsernames =
            listOf("123", "joh", "11njvearjgnuiw5895h89oh456tre54y", "    hey    ")

        // When & Then
        invalidUsernames.forEach { username ->
            assertFalse(AuthValidator.isValidUsername(username))
        }
    }

    @Test
    fun `isValidPassword should return true for valid passwords`() {
        // Given
        val validPasswords = listOf("heythere", "johndoe1234", "njvearjgnuiw5895h89oh")

        // When & Then
        validPasswords.forEach { password ->
            assertTrue(AuthValidator.isValidPassword(password))
        }
    }

    @Test
    fun `isValidPassword should return false for invalid passwords`() {
        // Given
        val invalidPasswords =
            listOf(
                "12345",
                "johndoe",
                "   hey       ",
                "123456789012345678901234567890123456789012345678901234567890",
            )

        // When & Then
        invalidPasswords.forEach { password ->
            assertFalse(AuthValidator.isValidPassword(password))
        }
    }

    @Test
    fun `isPasswordAndConfirmPasswordSame should return true when passwords match`() {
        // Given
        val password = "password1234"
        val confirmPassword = "password1234"

        // When
        val areSame =
            AuthValidator.isPasswordAndConfirmPasswordSame(
                password = password,
                confirmedPassword = confirmPassword,
            )

        // Then
        assertTrue(areSame)
    }

    @Test
    fun `isPasswordAndConfirmPasswordSame should return false when passwords don't match`() {
        // Given
        val password = "password"
        val confirmPassword = "confirmPassword"

        // When
        val areSame =
            AuthValidator.isPasswordAndConfirmPasswordSame(
                password = password,
                confirmedPassword = confirmPassword,
            )

        // Then
        assertFalse(areSame)
    }
}
