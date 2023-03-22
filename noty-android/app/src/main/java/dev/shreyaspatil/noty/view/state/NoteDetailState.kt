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

package dev.shreyaspatil.noty.view.state

import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

@GenerateMutableModel
interface NoteDetailState : State {
    val isLoading: Boolean
    val title: String?
    val note: String?
    val isPinned: Boolean
    val showSave: Boolean
    val finished: Boolean
    val error: String?

    companion object {
        val initialState = NoteDetailState(
            isLoading = false,
            title = null,
            note = null,
            isPinned = false,
            showSave = false,
            finished = false,
            error = null
        )
    }
}
