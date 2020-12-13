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

package dev.shreyaspatil.noty.composeapp.view.notes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dev.shreyaspatil.noty.composeapp.component.DarkThemeSwitch
import dev.shreyaspatil.noty.composeapp.component.NotesList
import dev.shreyaspatil.noty.composeapp.data.FakeData
import dev.shreyaspatil.noty.composeapp.navigation.Screen
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun NotesScreen(
    toggleTheme: () -> Unit,
    navController: NavHostController,
    notesViewModel: NotesViewModel,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Noty KT",
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                backgroundColor = MaterialTheme.colors.onBackground,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 0.dp,
                actions = {
                    DarkThemeSwitch(toggleTheme)
                }
            )
        },
        bodyContent = {
            val context = AmbientContext.current
            NotesList(
                noteList = FakeData.noteList,
                onClick = {
                    navController.navigate(Screen.NotesDetail.route)
                }
            )
        }
    )
}
