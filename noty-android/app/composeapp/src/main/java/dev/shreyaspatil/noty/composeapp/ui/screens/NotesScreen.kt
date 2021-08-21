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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.shreyaspatil.noty.composeapp.component.ConnectivityStatus
import dev.shreyaspatil.noty.composeapp.component.NotesList
import dev.shreyaspatil.noty.composeapp.component.action.AboutAction
import dev.shreyaspatil.noty.composeapp.component.action.LogoutAction
import dev.shreyaspatil.noty.composeapp.component.action.ThemeSwitchAction
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.navigation.NOTY_NAV_HOST_ROUTE
import dev.shreyaspatil.noty.composeapp.ui.Screen
import dev.shreyaspatil.noty.core.ui.UIDataState
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun NotesScreen(navController: NavHostController, viewModel: NotesViewModel) {
    if (!viewModel.isUserLoggedIn()) {
        navigateToLogin(navController)
        return
    }

    val scope = rememberCoroutineScope()

    val isInDarkMode = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Noty",
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 0.dp,
                actions = {
                    ThemeSwitchAction { viewModel.setDarkMode(!isInDarkMode) }
                    AboutAction {
                        navController.navigate(
                            Screen.About.route
                        )
                    }
                    LogoutAction(
                        onLogout = {
                            scope.launch {
                                viewModel.clearUserSession()
                                navigateToLogin(navController)
                            }
                        }
                    )
                }
            )
        },
        content = {
            var isSynced by rememberSaveable(key = "notesSyncedInitially") { mutableStateOf(false) }

            val notes = viewModel.notes.collectAsState(UIDataState.loading()).value
            val syncState = viewModel.syncState.collectAsState(UIDataState.loading()).value

            // Check whether it's already synced in the past composition
            // Or also check whether current state is also successful or not
            isSynced = isSynced || syncState.isSuccess

            val isRefreshing = notes.isLoading or syncState.isLoading

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    isSynced = false
                    viewModel.syncNotes()
                }
            ) {
                Column {
                    ConnectivityStatus()
                    when (notes) {
                        is UIDataState.Success -> NotesList(notes.data) { note ->
                            navController.navigate(Screen.NotesDetail.route(note.id))
                        }
                        is UIDataState.Failed -> FailureDialog(notes.message)
                    }
                }
            }

            LaunchedEffect(true) {
                if (!isSynced) {
                    viewModel.syncNotes()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddNote.route) },
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

private fun navigateToLogin(navController: NavHostController) {
    navController.navigate(
        Screen.Login.route,
        builder = {
            popUpTo(NOTY_NAV_HOST_ROUTE)
            launchSingleTop = true
        }
    )
}
