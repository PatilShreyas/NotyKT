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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

const val NOTY_NAV_HOST_ROUTE = "noty-main-route"

@Composable
fun NotyNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Notes.route, route = NOTY_NAV_HOST_ROUTE) {
        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = hiltViewModel(),
                onNavigateUp = { navController.navigateUp() },
                onNavigateToNotes = { navController.popAllAndNavigateToNotes() }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToSignup = { navController.navigateToSignup() },
                onNavigateToNotes = { navController.popAllAndNavigateToNotes() }
            )
        }
        composable(Screen.AddNote.route) {
            AddNoteScreen(
                viewModel = hiltViewModel(),
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(Screen.Notes.route) {
            NotesScreen(
                viewModel = hiltViewModel(),
                onNavigateToAbout = { navController.navigateToAbout() },
                onNavigateToAddNote = { navController.navigateToAddNote() },
                onNavigateToNoteDetail = { navController.navigateToNoteDetail(it) },
                onNavigateToLogin = { navController.popAllAndNavigateToLogin() }
            )
        }
        composable(
            Screen.NotesDetail.route,
            arguments = listOf(
                navArgument(Screen.NotesDetail.ARG_NOTE_ID) { type = NavType.StringType }
            )
        ) {
            val noteId = requireNotNull(it.arguments?.getString(Screen.NotesDetail.ARG_NOTE_ID))
            NoteDetailsScreen(
                viewModel = assistedViewModel {
                    NoteDetailViewModel.provideFactory(noteDetailViewModelFactory(), noteId)
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}

/**
 * Launches Signup screen
 */
fun NavController.navigateToSignup() = navigate(Screen.SignUp.route)

/**
 * Launches About screen
 */
fun NavController.navigateToAbout() = navigate(Screen.About.route)

/**
 * Launches Add note screen
 */
fun NavController.navigateToAddNote() = navigate(Screen.AddNote.route)

/**
 * Launches note detail screen for specified [noteId]
 */
fun NavController.navigateToNoteDetail(noteId: String) = navigate(Screen.NotesDetail.route(noteId))

/**
 * Clears backstack including current screen and navigates to Login Screen
 */
fun NavController.popAllAndNavigateToLogin() = navigate(Screen.Login.route) {
    popUpTo(NOTY_NAV_HOST_ROUTE)
    launchSingleTop = true
}

/**
 * Clears backstack including current screen and navigates to Notes Screen
 */
fun NavController.popAllAndNavigateToNotes() = navigate(Screen.Notes.route) {
    launchSingleTop = true
    popUpTo(NOTY_NAV_HOST_ROUTE)
}
