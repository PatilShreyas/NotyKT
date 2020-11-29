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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.shreyaspatil.noty.composeapp.view.details.NoteDetails
import dev.shreyaspatil.noty.composeapp.view.login.LoginScreen
import dev.shreyaspatil.noty.composeapp.view.notes.NotesScreen
import dev.shreyaspatil.noty.composeapp.view.signup.SignUpScreen

@Composable
fun NavigatorFlow() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Notes.route) {
            NotesScreen(navController)
        }
        composable(Screen.NotesDetail.route) {
            NoteDetails(navController)
        }
    }
}
