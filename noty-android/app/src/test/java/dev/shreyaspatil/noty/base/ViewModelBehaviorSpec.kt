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

import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Base spec for testing ViewModel.
 *
 * Since we are using `viewModelScope` in the ViewModel which uses Main dispatcher, this spec
 * sets Test dispatcher as a Main dispatcher so that it becomes easy to test the ViewModel.
 */
abstract class ViewModelBehaviorSpec(body: BehaviorSpec.() -> Unit = {}) : BehaviorSpec({
    val dispatcher = TestCoroutineDispatcher()

    testCoroutineDispatcher = true
    Dispatchers.setMain(dispatcher)

    apply(body)

    afterSpec { Dispatchers.resetMain() }
})
