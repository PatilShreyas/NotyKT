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

package dev.shreyaspatil.noty.composeapp

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dev.shreyaspatil.noty.composeapp.rule.WorkManagerRule
import dev.shreyaspatil.noty.composeapp.ui.MainActivity
import dev.shreyaspatil.noty.composeapp.ui.theme.NotyTheme
import dev.shreyaspatil.noty.view.state.State
import dev.shreyaspatil.noty.view.viewmodel.BaseViewModel
import org.junit.Rule

/**
 * Base spec for testing Jetpack Compose screens
 *
 * This takes care of instantiating Hilt, WorkManager.
 */
@Suppress("LeakingThis")
abstract class NotyScreenTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val workManagerRule = WorkManagerRule()

    @JvmField
    @Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    fun inject() = hiltRule.inject()

    inline fun <reified T : BaseViewModel<out State>> viewModel() =
        composeTestRule.activity.viewModels<T>().value

    fun runTest(
        body: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.() -> Unit
    ) = composeTestRule.run(body)

    fun setNotyContent(content: @Composable () -> Unit) = composeTestRule.activity.setContent {
        NotyTheme {
            content()
        }
    }
}
