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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.shreyaspatil.noty.composeapp.ui.Screen
import dev.shreyaspatil.noty.composeapp.ui.screens.AboutScreen
import dev.shreyaspatil.noty.composeapp.ui.screens.AddNoteScreen
import dev.shreyaspatil.noty.composeapp.ui.screens.LoginScreen
import dev.shreyaspatil.noty.composeapp.ui.screens.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.ui.screens.NotesScreen
import dev.shreyaspatil.noty.composeapp.ui.screens.SignUpScreen
import dev.shreyaspatil.noty.composeapp.utils.assistedViewModel
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

const val NOTY_NAV_HOST_ROUTE = "noty-main-route"

@ExperimentalAnimationApi
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun NotyNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Notes.route, route = NOTY_NAV_HOST_ROUTE) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController, hiltViewModel())
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, hiltViewModel())
        }
        composable(Screen.AddNote.route) {
            AddNoteScreen(navController, hiltViewModel())
        }
        composable(Screen.Notes.route) {
            NotesScreen(navController, hiltViewModel())
        }
        composable(
            Screen.NotesDetail.route,
            arguments = listOf(
                navArgument(Screen.NotesDetail.ARG_NOTE_ID) { type = NavType.StringType }
            )
        ) {
            val noteId = requireNotNull(it.arguments?.getString(Screen.NotesDetail.ARG_NOTE_ID))
            NoteDetailsScreen(
                navController,
                assistedViewModel {
                    NoteDetailViewModel.provideFactory(noteDetailViewModelFactory(), noteId)
                }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
    }
}
