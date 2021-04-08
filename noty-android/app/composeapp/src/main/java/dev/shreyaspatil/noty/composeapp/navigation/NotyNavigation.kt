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
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import dev.shreyaspatil.noty.composeapp.view.Screen
import dev.shreyaspatil.noty.composeapp.view.screen.AboutScreen
import dev.shreyaspatil.noty.composeapp.view.screen.AddNoteScreen
import dev.shreyaspatil.noty.composeapp.view.screen.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.screen.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.view.screen.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.screen.SignUpScreen
import dev.shreyaspatil.noty.composeapp.view.screen.noteDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

const val NOTY_NAV_HOST_ROUTE = "noty-main-route"

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun NotyNavigation(toggleTheme: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Notes.route, route = NOTY_NAV_HOST_ROUTE) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController, hiltNavGraphViewModel())
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, hiltNavGraphViewModel())
        }
        composable(Screen.AddNote.route) {
            AddNoteScreen(navController, hiltNavGraphViewModel())
        }
        composable(Screen.Notes.route) {
            NotesScreen(toggleTheme, navController, hiltNavGraphViewModel())
        }
        composable(
            Screen.NotesDetail.route,
            arguments = listOf(
                navArgument(Screen.NotesDetail.ARG_NOTE_ID) { type = NavType.StringType }
            )
        ) {
            val noteId = it.arguments?.getString(Screen.NotesDetail.ARG_NOTE_ID)
                ?: throw IllegalStateException("'noteId' shouldn't be null")
            NoteDetailsScreen(navController, noteDetailViewModel(noteId))
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
    }
}
