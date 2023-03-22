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

package dev.shreyaspatil.noty.store

import dev.shreyaspatil.mutekt.core.MutektMutableState
import kotlinx.coroutines.flow.StateFlow

/**
 * A single source of truth for storing UI state
 *
 * [STATE] represents immutable state model.
 * [MUTABLE_STATE] represents mutable state model.
 *
 * @param initialState The initial state which is mutable
 */
class StateStore<STATE, MUTABLE_STATE : MutektMutableState<STATE, out STATE>>(initialState: MUTABLE_STATE) {
    /**
     * Mutable state model
     */
    private val mutableState = initialState

    /**
     * Reactive flow of Read only state model
     */
    val state: StateFlow<STATE> = mutableState.asStateFlow()

    /**
     * Updates state atomically.
     *
     * @param update The lambda block to perform state updates
     */
    @Suppress("UNCHECKED_CAST")
    fun setState(update: MUTABLE_STATE.() -> Unit) {
        mutableState.update(update as STATE.() -> Unit)
    }
}
