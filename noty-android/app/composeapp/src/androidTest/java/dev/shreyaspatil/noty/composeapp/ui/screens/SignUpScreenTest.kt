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

package dev.shreyaspatil.noty.composeapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidTest
import dev.shreyaspatil.noty.composeapp.NotyScreenTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTestApi::class)
@HiltAndroidTest
class SignUpScreenTest : NotyScreenTest() {
    @Test
    fun navigateUp_onClickingLoginText() =
        runTest {
            var navigatingUp = false
            setNotyContent {
                SignUpScreen(onNavigateUp = { navigatingUp = true })
            }

            onNodeWithText("Already have an account?", substring = true).performClick()
            waitForIdle()

            assertTrue(navigatingUp)
        }

    @Test
    fun showDoNothing_whenEnteredInvalidCredentials() =
        runTest {
            var navigatedToNotes = false
            val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
            setNotyContent {
                SignUpScreen(
                    closeKeyboard = closeKeyboard,
                    onNavigateToNotes = { navigatedToNotes = true },
                )
            }

            onNodeWithTag("Username").performTextInput("john")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("Password").performTextInput("doe")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("Confirm Password").performTextInput("doe1234")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("createAccountButton").performClick()
            waitForIdle()

            assertFalse(navigatedToNotes)
        }

    @Test
    fun showDoNothing_whenEnteredWrongCredentials() =
        runTest {
            var navigatedToNotes = false
            val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 2)
            setNotyContent {
                SignUpScreen(
                    closeKeyboard = closeKeyboard,
                    onNavigateToNotes = { navigatedToNotes = true },
                )
            }

            waitForIdle()
            onNodeWithTag("Username").performTextInput("johndoe")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("Password").performTextInput("johndoe1234")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            waitUntilExactlyOneExists(hasTestTag("Confirm Password"))

            onNodeWithTag("Confirm Password").performTextInput("johndoe1234")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("createAccountButton").performClick()
            waitForIdle()

            assertFalse(navigatedToNotes)

            onNodeWithText("User already exist").assertExists()
        }

    @Test
    fun navigateToNotes_onSuccessfulSignup() =
        runTest {
            var navigatedToNotes = false
            val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 2)
            setNotyContent {
                SignUpScreen(
                    closeKeyboard = closeKeyboard,
                    onNavigateToNotes = { navigatedToNotes = true },
                )
            }

            waitForIdle()
            onNodeWithTag("Username").performTextInput("shreyaspatil")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("Password").performTextInput("johndoe1234")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            waitUntilExactlyOneExists(hasTestTag("Confirm Password"))

            onNodeWithTag("Confirm Password").performTextInput("johndoe1234")
            waitForIdle()
            closeKeyboard.tryEmit(Unit)
            waitForIdle()

            onNodeWithTag("createAccountButton").performClick()
            waitForIdle()

            assertTrue(navigatedToNotes)
        }

    @Composable
    private fun SignUpScreen(
        closeKeyboard: Flow<Unit> = emptyFlow(),
        onNavigateUp: () -> Unit = {},
        onNavigateToNotes: () -> Unit = {},
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            closeKeyboard.collect { keyboardController?.hide() }
        }

        SignUpScreen(
            viewModel = viewModel(),
            onNavigateUp = onNavigateUp,
            onNavigateToNotes = onNavigateToNotes,
        )
    }
}
