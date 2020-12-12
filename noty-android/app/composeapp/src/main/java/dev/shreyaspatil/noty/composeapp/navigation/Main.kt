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

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetails
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun Main() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Notes.route) {
        composable(Screen.SignUp.route) {
            val registerViewModel: RegisterViewModel = viewModel()
            SignUpScreen(navController, registerViewModel)
        }
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(navController, loginViewModel)
        }
        composable(Screen.AddNotes.route) {
            val noteDetailViewModel: NoteDetailViewModel = viewModel()
            NoteDetails(navController, noteDetailViewModel)
        }
        composable(Screen.Notes.route) {
            val notesViewModel: NotesViewModel = viewModel()
            NotesScreen(navController, notesViewModel)
        }
        composable(Screen.NotesDetail.route) {
            val noteDetailViewModel: NoteDetailViewModel = viewModel()
            NoteDetails(navController, noteDetailViewModel)
        }
    }
}
