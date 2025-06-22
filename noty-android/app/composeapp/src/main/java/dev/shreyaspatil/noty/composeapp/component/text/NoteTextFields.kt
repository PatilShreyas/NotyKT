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

package dev.shreyaspatil.noty.composeapp.component.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NoteTitleField(
    modifier: Modifier = Modifier,
    value: String = "",
    onTextChange: (String) -> Unit,
) {
    BasicNotyTextField(
        modifier,
        value = value,
        label = "Title",
        onTextChange = onTextChange,
        textStyle = MaterialTheme.typography.headlineLarge,
        maxLines = 2,
    )
}

@Composable
fun NoteField(
    modifier: Modifier = Modifier,
    value: String = "",
    onTextChange: (String) -> Unit,
) {
    BasicNotyTextField(
        modifier,
        value = value,
        label = "Write note here",
        onTextChange = onTextChange,
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}
