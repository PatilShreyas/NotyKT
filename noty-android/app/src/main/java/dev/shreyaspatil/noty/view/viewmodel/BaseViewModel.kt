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

package dev.shreyaspatil.noty.view.viewmodel

import androidx.lifecycle.ViewModel
import dev.shreyaspatil.noty.view.state.State
import kotlinx.coroutines.flow.StateFlow

/**
 * Base for all the ViewModels
 */
abstract class BaseViewModel<STATE : State> : ViewModel() {
    /**
     * State to be exposed to the UI layer
     */
    abstract val state: StateFlow<STATE>

    /**
     * Retrieves the current UI state
     */
    val currentState: STATE get() = state.value
}



