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

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dev.shreyaspatil.noty.composeapp.navigation.Screen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun NotesScreen(
    navController: NavHostController,
    notesViewModel: NotesViewModel,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Noty KT",
                    textAlign = TextAlign.Start,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            backgroundColor = MaterialTheme.colors.onBackground,
            contentColor = MaterialTheme.colors.secondary,
            elevation = 0.dp
        )
    }, bodyContent = {


        val context = AmbientContext.current

        if (!notesViewModel.isUserLoggedIn()) {
            navController.navigate(Screen.Login.route)
            Toast.makeText(context, "Hello ${notesViewModel.isUserLoggedIn()}", Toast.LENGTH_SHORT)
                .show()
        } else {
            notesViewModel.notes.value.let { notesState ->
                when (notesState) {
                    is ViewState.Success -> Toast.makeText(context,
                        "Notes are ${notesState.data.first().title}",
                        Toast.LENGTH_SHORT).show()

                    is ViewState.Loading -> Toast.makeText(context, "Loading", Toast.LENGTH_SHORT)
                        .show()
                    is ViewState.Failed -> Toast.makeText(context, "Failed", Toast.LENGTH_SHORT)
                        .show()
                    null -> Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
                }
            }

        }

    })
}
