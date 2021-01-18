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

package dev.shreyaspatil.noty.composeapp.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.NavType
import androidx.navigation.compose.*
import dagger.hilt.android.EntryPointAccessors
import dev.shreyaspatil.noty.composeapp.utils.toast
import dev.shreyaspatil.noty.composeapp.view.MainActivity
import dev.shreyaspatil.noty.composeapp.view.addnotes.AddNotesScreen
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.view.viewmodel.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun Main(
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Notes.route) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.AddNotes.route) {
            AddNotesScreen(navController)
        }
        composable(Screen.Notes.route) {
            NotesScreen(toggleTheme, navController)
        }
        composable(
            Screen.NotesDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            val noteId = it.arguments?.getString("noteId")
                ?: throw IllegalStateException("'noteId' shouldn't be null")
            NoteDetailsScreen(navController, noteId)
        }
    }
}
