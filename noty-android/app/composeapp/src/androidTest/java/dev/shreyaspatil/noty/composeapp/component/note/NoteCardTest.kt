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

package dev.shreyaspatil.noty.composeapp.component.note

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NoteCardTest {

    @JvmField
    @Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNoteCard() {
        var clickCount = 0
        // Start the app
        composeTestRule.setContent {
            NoteCard(title = "Lorem Ipsum", note = "Hello World") {
                clickCount++
            }
        }

        val titleNode = composeTestRule.onNodeWithText("Lorem Ipsum")
        val noteNode = composeTestRule.onNodeWithText("Hello World")

        // Title and note should be displayed
        titleNode.assertIsDisplayed()
        noteNode.assertIsDisplayed()

        // Perform click twice
        titleNode.performClick()
        noteNode.performClick()

        assertEquals(2, clickCount)
    }
}
