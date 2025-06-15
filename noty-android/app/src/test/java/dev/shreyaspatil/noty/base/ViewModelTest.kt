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

package dev.shreyaspatil.noty.base

import dev.shreyaspatil.noty.view.state.State
import dev.shreyaspatil.noty.view.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

/**
 * Base class for ViewModel tests that sets up the Dispatchers.Main for coroutines
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTest {
    @BeforeEach
    fun setupCoroutineDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun cleanupCoroutineDispatcher() {
        Dispatchers.resetMain()
    }

    /**
     * JUnit 5 matcher for verifying and comparing the ViewModel's current state
     */
    infix fun <S : State, VM : BaseViewModel<S>> VM.currentStateShouldBe(expected: S) {
        assertEquals(expected, currentState)
    }

    /**
     * Utility on ViewModel for getting current state in the [block] lambda.
     * Useful in verifying each contents of the state
     */
    infix fun <S : State, VM : BaseViewModel<S>> VM.withState(block: S.() -> Unit) = currentState.run(block)
}
