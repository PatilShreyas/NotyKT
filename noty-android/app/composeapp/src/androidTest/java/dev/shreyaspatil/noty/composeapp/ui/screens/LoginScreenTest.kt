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
import androidx.compose.ui.test.assertContentDescriptionEquals
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

@OptIn(ExperimentalComposeUiApi::class)
@HiltAndroidTest
class LoginScreenTest : NotyScreenTest() {

    @Test
    fun navigateToSignup_onClickingSignupText() = runTest {
        var navigatingToSignup = false
        setNotyContent {
            LoginScreen(onNavigateToSignup = { navigatingToSignup = true })
        }

        onNodeWithText("Don't have an account? Signup").performClick()
        waitForIdle()

        assertTrue(navigatingToSignup)
    }

    @Test
    fun showDoNothing_whenEnteredInvalidCredentials() = runTest {
        var navigatedToNotes = false
        val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
        setNotyContent {
            LoginScreen(
                closeKeyboard = closeKeyboard,
                onNavigateToNotes = { navigatedToNotes = true }
            )
        }

        onNodeWithTag("Username").performTextInput("john")
        waitForIdle()
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        onNodeWithTag("Password").performTextInput("doe")
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        onNodeWithText("Login").performClick()
        waitForIdle()

        assertFalse(navigatedToNotes)
    }

    @Test
    fun showDoNotExistError_whenEnteredWrongCredentials() = runTest {
        var navigatedToNotes = false
        val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
        setNotyContent {
            LoginScreen(
                closeKeyboard = closeKeyboard,
                onNavigateToNotes = { navigatedToNotes = true }
            )
        }

        onNodeWithTag("Username").performTextInput("johndoe")
        waitForIdle()
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        Thread.sleep(500)

        onNodeWithTag("Password").performTextInput("wrongpassword")
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        Thread.sleep(500)

        onNodeWithText("Login").performClick()
        waitForIdle()

        assertFalse(navigatedToNotes)

        // Should show error in dialog
        onNodeWithText("User not exist").assertExists()
    }

    @Test
    fun navigateToNotes_onSuccessfulLogin() = runTest {
        var navigatedToNotes = false
        val closeKeyboard = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
        setNotyContent {
            LoginScreen(
                closeKeyboard = closeKeyboard,
                onNavigateToNotes = { navigatedToNotes = true }
            )
        }

        onNodeWithTag("Username").performTextInput("johndoe")
        waitForIdle()
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        Thread.sleep(500)

        onNodeWithTag("Password").performTextInput("johndoe1234")
        closeKeyboard.tryEmit(Unit)
        waitForIdle()

        Thread.sleep(500)

        onNodeWithText("Login").performClick()
        waitForIdle()

        assertTrue(navigatedToNotes)
    }

    @Test
    fun testIfPasswordIsVisible_onEyeButtonClicked() = runTest {
        setNotyContent {
            LoginScreen()
        }
        onNodeWithTag(
            testTag = "TogglePasswordVisibility",
            useUnmergedTree = true
        ).performClick()

        onNodeWithTag(
            testTag = "TogglePasswordVisibility",
            useUnmergedTree = true
        ).assertContentDescriptionEquals("Hide password")
    }

    @Composable
    private fun LoginScreen(
        closeKeyboard: Flow<Unit> = emptyFlow(),
        onNavigateToSignup: () -> Unit = {},
        onNavigateToNotes: () -> Unit = {}
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current

        LoginScreen(
            viewModel = viewModel(),
            onNavigateToSignup = onNavigateToSignup,
            onNavigateToNotes = onNavigateToNotes
        )

        LaunchedEffect(Unit) {
            closeKeyboard.collect { keyboardController?.hide() }
        }
    }
}
