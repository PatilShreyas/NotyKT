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

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import dev.shreyaspatil.noty.composeapp.view.addnotes.AddNotesScreen
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen
import dev.shreyaspatil.noty.view.viewmodel.AddNoteViewModel
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun Main(
    addNoteViewModel: AddNoteViewModel = viewModel(),
    notesViewModel: NotesViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel(),
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    (AmbientContext.current as AppCompatActivity).viewModels<NotesViewModel>().let {

    }
    NavHost(navController, startDestination = Screen.Notes.route) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController, registerViewModel)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, loginViewModel)
        }
        composable(Screen.AddNotes.route) {
            AddNotesScreen(navController, addNoteViewModel)
        }
        composable(Screen.Notes.route) {
            NotesScreen(toggleTheme, navController, notesViewModel)
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
