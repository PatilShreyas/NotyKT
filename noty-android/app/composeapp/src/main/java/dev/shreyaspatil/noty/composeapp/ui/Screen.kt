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

package dev.shreyaspatil.noty.composeapp.ui

sealed class Screen(val route: String, val name: String) {
    object SignUp : Screen("signup", "Sign Up")
    object Login : Screen("login", "Login")
    object Notes : Screen("notes", "Notes")
    object NotesDetail : Screen("note/{noteId}", "Note details") {
        fun route(noteId: String) = "note/$noteId"

        const val ARG_NOTE_ID: String = "noteId"
    }

    object AddNote : Screen("note/new", "New note")
    object About : Screen("about", "About")
}
