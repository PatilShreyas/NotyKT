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
import androidx.compose.ui.platform.AmbientContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import dev.shreyaspatil.noty.composeapp.view.addnotes.AddNotesScreen
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.view.details.noteDetailViewModel
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun Main(toggleTheme: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Notes.route) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController, it.hiltNavGraphViewModel())
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, it.hiltNavGraphViewModel())
        }
        composable(Screen.AddNote.route) {
            AddNotesScreen(navController, it.hiltNavGraphViewModel())
        }
        composable(Screen.Notes.route) {
            NotesScreen(toggleTheme, navController, it.hiltNavGraphViewModel())
        }
        composable(
            Screen.NotesDetail.route,
            arguments = listOf(navArgument(Screen.NotesDetail.ARG_NOTE_ID) {
                type = NavType.StringType
            })
        ) {
            val noteId = it.arguments?.getString(Screen.NotesDetail.ARG_NOTE_ID)
                ?: throw IllegalStateException("'noteId' shouldn't be null")
            NoteDetailsScreen(navController, noteDetailViewModel(noteId))
        }
    }
}

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.hiltNavGraphViewModel(): VM {
    val viewModelFactory = HiltViewModelFactory(AmbientContext.current, this)
    return ViewModelProvider(this, viewModelFactory).get(VM::class.java)
}
