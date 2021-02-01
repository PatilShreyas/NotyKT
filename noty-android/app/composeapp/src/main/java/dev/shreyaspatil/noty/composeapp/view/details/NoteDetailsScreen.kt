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

package dev.shreyaspatil.noty.composeapp.view.details

import android.app.Activity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dagger.hilt.android.EntryPointAccessors
import dev.shreyaspatil.noty.composeapp.navigation.Screen
import dev.shreyaspatil.noty.composeapp.view.MainActivity
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteDetailViewModel: NoteDetailViewModel
) {
    val note = noteDetailViewModel.note.collectAsState(initial = null)

    val titleText = mutableStateOf(note.value?.title)
    val noteText = mutableStateOf(note.value?.note)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NotyKT",
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp),
                        onClick = {
                            navController.navigate(Screen.Notes.route)
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colors.onPrimary)
                    }
                },
                backgroundColor = MaterialTheme.colors.onBackground,
                contentColor = MaterialTheme.colors.secondary,
                elevation = 0.dp
            )
        },
        bodyContent = {
            ScrollableColumn {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp),
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit.Sp(24)
                    ),
                    value = titleText.value ?: "",
                    onValueChange = { titleText.value = it }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp),
                    label = { Text(text = "Title") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit.Sp(24)
                    ),
                    backgroundColor = MaterialTheme.colors.background,
                    value = titleText.value ?: "",
                    onValueChange = { titleText.value = it }
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp),
                    label = { Text(text = "Write something...") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = TextUnit.Sp(16)
                    ),
                    backgroundColor = MaterialTheme.colors.background,
                    value = noteText.value ?: "",
                    onValueChange = { noteText.value = it }
                )
            }
        }
    )
}

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun noteDetailViewModel(noteId: String): NoteDetailViewModel {
    val factory = EntryPointAccessors.fromActivity(
        AmbientContext.current as Activity,
        MainActivity.ViewModelFactoryProvider::class.java
    ).noteDetailViewModelFactory()

    return viewModel(
        factory = NoteDetailViewModel.provideFactory(factory, noteId)
    )
}
