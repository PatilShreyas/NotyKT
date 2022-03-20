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

package dev.shreyaspatil.noty.composeapp.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dev.shreyaspatil.noty.composeapp.NotyComposableTest
import org.junit.Test

class ConnectivityStatusTest : NotyComposableTest() {
    @Test
    fun testNoConnectivity() = runTest {
        setContent {
            ConnectivityStatus(isConnected = false)
        }

        onNodeWithText("No Internet Connection!").assertIsDisplayed()
    }

    @Test
    fun testConnectivityBackAfterNoConnectivity() = runTest {
        var isConnected by mutableStateOf(false)
        setContent {
            ConnectivityStatus(isConnected = isConnected)
        }

        // First, no connectivity should be displayed
        onNodeWithText("No Internet Connection!").assertIsDisplayed()

        // Bring connectivity back
        isConnected = true
        onNodeWithText("No Internet Connection!").assertDoesNotExist()
        onNodeWithText("Back Online!").assertIsDisplayed()

        // Wait for 2 seconds
        mainClock.advanceTimeBy(2000)

        // Status should be vanished
        onNodeWithText("Back Online!").assertDoesNotExist()
    }
}
