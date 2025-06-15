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

class NoteValidatorTest {
    @Test
    fun `isValidNote should return true for valid title and content`() {
        // Given
        val validTitleAndNotes =
            listOf(
                "title" to "content",
                "    Hey there    " to "Hey there, this is body of a note",
                "1234" to "Hi",
            )

        // When & Then
        validTitleAndNotes.forEach { (title, note) ->
            assertTrue(NoteValidator.isValidNote(title, note))
        }
    }

    @Test
    fun `isValidNote should return false for invalid title and content`() {
        // Given
        val invalidTitleAndNotes =
            listOf(
                "hi" to "content",
                "    Hey   " to "Hey there, this is body of a note",
                "1234" to "",
            )

        // When & Then
        invalidTitleAndNotes.forEach { (title, note) ->
            assertFalse(NoteValidator.isValidNote(title, note))
        }
    }
}
