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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.shreyaspatil.noty.composeapp.component.ConnectivityStatus
import dev.shreyaspatil.noty.composeapp.component.action.AboutAction
import dev.shreyaspatil.noty.composeapp.component.action.LogoutAction
import dev.shreyaspatil.noty.composeapp.component.action.ThemeSwitchAction
import dev.shreyaspatil.noty.composeapp.component.dialog.ConfirmationDialog
import dev.shreyaspatil.noty.composeapp.component.note.NotesList
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyScaffold
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyTopAppBar
import dev.shreyaspatil.noty.composeapp.ui.theme.LocalUiInDarkMode
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.composeapp.utils.collection.ComposeImmutableList
import dev.shreyaspatil.noty.composeapp.utils.collection.composeImmutableListOf
import dev.shreyaspatil.noty.composeapp.utils.collection.rememberComposeImmutableList
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlin.random.Random

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.collectState()

    val isInDarkMode = LocalUiInDarkMode.current

    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val notes by rememberComposeImmutableList { state.notes }

    NotesContent(
        isLoading = state.isLoading,
        notes = notes,
        isConnectivityAvailable = state.isConnectivityAvailable,
        onRefresh = viewModel::syncNotes,
        onToggleTheme = { viewModel.setDarkMode(!isInDarkMode) },
        onAboutClick = onNavigateToAbout,
        onAddNoteClick = onNavigateToAddNote,
        onLogoutClick = { showLogoutConfirmation = true },
        onNavigateToNoteDetail = onNavigateToNoteDetail,
    )

    LogoutConfirmation(
        show = showLogoutConfirmation,
        onConfirm = viewModel::logout,
        onDismiss = { showLogoutConfirmation = false },
    )

    val isUserLoggedIn = state.isUserLoggedIn
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == false) {
            onNavigateToLogin()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    isLoading: Boolean,
    notes: ComposeImmutableList<Note>,
    isConnectivityAvailable: Boolean?,
    error: String? = null,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onAboutClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
) {
    NotyScaffold(
        error = error,
        notyTopAppBar = {
            NotyTopAppBar(
                actions = {
                    ThemeSwitchAction(onToggleTheme)
                    AboutAction(onAboutClick)
                    LogoutAction(onLogout = onLogoutClick)
                },
            )
        },
        content = {
            PullToRefreshBox(
                modifier =
                    Modifier
                        .padding(it)
                        .fillMaxSize(),
                isRefreshing = isLoading,
                onRefresh = onRefresh,
            ) {
                Column {
                    if (isConnectivityAvailable != null) {
                        ConnectivityStatus(isConnectivityAvailable)
                    }
                    NotesList(notes) { note -> onNavigateToNoteDetail(note.id) }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick, shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Filled.Add, "Add")
            }
        },
    )
}

@Composable
fun LogoutConfirmation(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (show) {
        ConfirmationDialog(
            title = "Logout?",
            message = "Sure want to logout?",
            onConfirmedYes = onConfirm,
            onConfirmedNo = onDismiss,
            onDismissed = onDismiss,
        )
    }
}

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotesScreen() {
    NotyPreview {
        NotesContent(
            isLoading = false,
            notes =
                composeImmutableListOf(
                    noteFixture(true),
                    noteFixture(),
                    noteFixture(),
                ),
            isConnectivityAvailable = false,
            onRefresh = { },
            onToggleTheme = { },
            onAboutClick = { },
            onAddNoteClick = { },
            onLogoutClick = { },
            onNavigateToNoteDetail = { },
        )
    }
}

private fun noteFixture(isPinned: Boolean = false): Note {
    return Note(Random.nextInt().toString(), "Lorem Ipsum", "Hey this is a note body", 1, isPinned)
}
