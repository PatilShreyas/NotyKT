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

package dev.shreyaspatil.noty.composeapp.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.shreyaspatil.noty.composeapp.NotyScreenTest
import dev.shreyaspatil.noty.composeapp.ui.MainActivity
import dev.shreyaspatil.noty.composeapp.ui.Screen
import dev.shreyaspatil.noty.composeapp.ui.theme.LocalUiInDarkMode
import dev.shreyaspatil.noty.core.session.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NavigationTest : NotyScreenTest() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        inject()
        // Mock fake authentication
        sessionManager.saveToken("Bearer ABCD")
    }

    @Test
    fun startDestination_isLoginScreen_whenLoggedOut() = runTest {
        val isLoggedIn = false
        val expectedRoute = Screen.Login.route

        testStartDestination(isLoggedIn, expectedRoute)
    }

    @Test
    fun startDestination_isNotesScreen_whenLoggedIn() = runTest {
        val isLoggedIn = true
        val expectedRoute = Screen.Notes.route

        testStartDestination(isLoggedIn, expectedRoute)
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.testStartDestination(
        isLoggedIn: Boolean,
        expectedRoute: String
    ) {
        lateinit var navController: NavHostController

        setNotyContent {
            CompositionLocalProvider(LocalUiInDarkMode provides true) {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())

                NotyNavigation(
                    isLoggedIn = isLoggedIn,
                    navController = navController
                )
            }
        }

        waitForIdle()
        assertEquals(expectedRoute, navController.currentDestination?.route)
    }
}
