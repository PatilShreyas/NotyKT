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

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.store.StateStore
import dev.shreyaspatil.noty.view.state.HomeState
import dev.shreyaspatil.noty.view.state.mutable
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        sessionManager: SessionManager,
    ) : BaseViewModel<HomeState>() {
        private val stateStore = StateStore(initialState = HomeState.initialState.mutable())

        override val state: StateFlow<HomeState> = stateStore.state

        init {
            stateStore.setState {
                isLoggedIn = sessionManager.getToken() != null
            }
        }
    }
