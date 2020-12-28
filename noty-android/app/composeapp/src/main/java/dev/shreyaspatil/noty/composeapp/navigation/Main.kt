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
import androidx.lifecycle.asLiveData
import androidx.navigation.NavType
import androidx.navigation.compose.*
import dev.shreyaspatil.noty.composeapp.utils.toast
import dev.shreyaspatil.noty.composeapp.view.addnotes.AddNotesScreen
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetailsScreen
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.view.viewmodel.AddNoteViewModel
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun Main(
    toggleTheme: () -> Unit,
    registerViewModel: RegisterViewModel,
    loginViewModel: LoginViewModel,
    notesViewModel: NotesViewModel,
    addNoteViewModel: AddNoteViewModel,
) {
    val navController = rememberNavController()
    val context = AmbientContext.current

    NavHost(navController, startDestination = Screen.Notes.route) {
        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController, registerViewModel,
                onSignUpClicked = {
                    registerViewModel.register(it.username, it.password)
                },
                onAuthSuccess = {
                    registerViewModel.authFlow.asLiveData().value.let { viewState ->
                        when (viewState) {
                            is ViewState.Loading -> context.toast("Loading")

                            is ViewState.Success -> {
                                navController.navigate(Screen.Notes.route)
                            }
                            is ViewState.Failed -> {
                                context.toast("Error ${viewState.message}")
                            }
                        }
                    }
                }
            )
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
            "${Screen.NotesDetail.route}/{id}/{title}/{note}/{created}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("title") { type = NavType.StringType },
                navArgument("note") { type = NavType.StringType },
                navArgument("created") { type = NavType.LongType }

            )
        ) {
            NoteDetailsScreen(
                navController,
                it.arguments?.getInt("id") ?: 0,
                it.arguments!!.getString("title")!!,
                it.arguments!!.getString("note")!!,
                it.arguments!!.getLong("created")
            )
        }
    }
}
