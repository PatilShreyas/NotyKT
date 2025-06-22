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

package dev.shreyaspatil.noty.composeapp.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyScaffold
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyTopAppBar
import dev.shreyaspatil.noty.composeapp.component.text.NoteField
import dev.shreyaspatil.noty.composeapp.component.text.NoteTitleField
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.view.viewmodel.AddNoteViewModel

@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel,
    onNavigateUp: () -> Unit,
) {
    val state by viewModel.collectState()

    AddNotesContent(
        isLoading = state.isAdding,
        title = state.title,
        note = state.note,
        showSaveFab = state.showSave,
        onTitleChange = viewModel::setTitle,
        onNoteChange = viewModel::setNote,
        onClickAddNote = viewModel::add,
        error = state.errorMessage,
        onNavigateUp = onNavigateUp,
    )

    LaunchedEffect(state.added) {
        if (state.added) {
            onNavigateUp()
        }
    }
}

@Composable
fun AddNotesContent(
    isLoading: Boolean,
    title: String,
    note: String,
    showSaveFab: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onClickAddNote: () -> Unit,
    error: String?,
    onNavigateUp: () -> Unit,
) {
    NotyScaffold(
        isLoading = isLoading,
        error = error,
        notyTopAppBar = { NotyTopAppBar(title = "Add Note", onNavigateUp = onNavigateUp) },
        content = {
            Column(
                Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                NoteTitleField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onTextChange = onTitleChange,
                )

                NoteField(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = 32.dp),
                    value = note,
                    onTextChange = onNoteChange,
                )
            }
        },
        floatingActionButton = {
            if (showSaveFab) {
                ExtendedFloatingActionButton(
                    text = { Text("Save") },
                    icon = {
                        Icon(
                            Icons.Filled.Done,
                            "Save",
                        )
                    },
                    onClick = onClickAddNote,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.imePadding(),
                )
            }
        },
    )
}

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAddNoteScreen() {
    NotyPreview {
        AddNotesContent(
            isLoading = false,
            title = "Existing note title",
            note = "Existing note body",
            error = null,
            showSaveFab = true,
            onTitleChange = {},
            onNoteChange = {},
            onNavigateUp = {},
            onClickAddNote = {},
        )
    }
}
