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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.shreyaspatil.noty.composeapp.component.ConnectivityStatus
import dev.shreyaspatil.noty.composeapp.component.action.AboutAction
import dev.shreyaspatil.noty.composeapp.component.action.LogoutAction
import dev.shreyaspatil.noty.composeapp.component.action.ThemeSwitchAction
import dev.shreyaspatil.noty.composeapp.component.dialog.ConfirmationDialog
import dev.shreyaspatil.noty.composeapp.component.note.NotesList
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyScaffold
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyTopAppBar
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.collectState()

    val isInDarkMode = isSystemInDarkTheme()

    var showLogoutConfirmation by remember { mutableStateOf(false) }

    NotesContent(
        isLoading = state.isLoading,
        notes = state.notes,
        isConnectivityAvailable = state.isConnectivityAvailable,
        onRefresh = viewModel::syncNotes,
        onToggleTheme = { viewModel.setDarkMode(!isInDarkMode) },
        onAboutClick = onNavigateToAbout,
        onAddNoteClick = onNavigateToAddNote,
        onLogoutClick = { showLogoutConfirmation = true },
        onNavigateToNoteDetail = onNavigateToNoteDetail
    )

    LogoutConfirmation(
        show = showLogoutConfirmation,
        onConfirm = viewModel::logout,
        onDismiss = { showLogoutConfirmation = false }
    )

    val isUserLoggedIn = state.isUserLoggedIn
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == false) {
            onNavigateToLogin()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotesContent(
    isLoading: Boolean,
    notes: List<Note>,
    isConnectivityAvailable: Boolean?,
    error: String? = null,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onAboutClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit
) {
    NotyScaffold(
        error = error,
        notyTopAppBar = {
            NotyTopAppBar(
                actions = {
                    ThemeSwitchAction(onToggleTheme)
                    AboutAction(onAboutClick)
                    LogoutAction(onLogout = onLogoutClick)
                }
            )
        },
        content = {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isLoading,
                onRefresh = onRefresh
            )
            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .fillMaxSize()
            ) {
                Column {
                    if (isConnectivityAvailable != null) {
                        ConnectivityStatus(isConnectivityAvailable)
                    }
                    NotesList(notes) { note -> onNavigateToNoteDetail(note.id) }
                }

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Add",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun LogoutConfirmation(show: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (show) {
        ConfirmationDialog(
            title = "Logout?",
            message = "Sure want to logout?",
            onConfirmedYes = onConfirm,
            onConfirmedNo = onDismiss,
            onDismissed = onDismiss
        )
    }
}
