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

package dev.shreyaspatil.noty.composeapp.view.screen

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dagger.hilt.android.EntryPointAccessors
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.component.action.DeleteAction
import dev.shreyaspatil.noty.composeapp.component.action.ShareAction
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.component.dialog.LoaderDialog
import dev.shreyaspatil.noty.composeapp.utils.ShowToast
import dev.shreyaspatil.noty.composeapp.view.MainActivity
import dev.shreyaspatil.noty.composeapp.view.Screen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.utils.NoteValidator
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    viewModel: NoteDetailViewModel
) {
    val activity = LocalContext.current as Activity

    val updateState = viewModel.updateNoteState.collectAsState(initial = null)
    val deleteState = viewModel.deleteNoteState.collectAsState(initial = null)

    val note = viewModel.note.collectAsState(initial = null).value

    if (note == null) {
        LoaderDialog()
    } else {
        val titleText = remember { mutableStateOf(note.title) }
        val noteText = remember { mutableStateOf(note.note) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Noty",
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
                            Icon(
                                painterResource(R.drawable.ic_back),
                                "Back",
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.onPrimary,
                    elevation = 0.dp,
                    actions = {
                        DeleteAction(onClick = { viewModel.deleteNote() })
                        ShareAction(
                            onClick = {
                                shareNote(
                                    activity,
                                    titleText.value,
                                    noteText.value
                                )
                            }
                        )
                    }
                )
            },
            content = {
                LazyColumn {
                    item {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 0.dp, 16.dp, 0.dp)
                                .background(MaterialTheme.colors.background),
                            label = { Text(text = "Title") },
                            textStyle = TextStyle(
                                color = MaterialTheme.colors.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            value = titleText.value,
                            onValueChange = { titleText.value = it }
                        )
                    }
                    item {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(16.dp, 0.dp, 16.dp, 0.dp)
                                .background(MaterialTheme.colors.background),
                            label = { Text(text = "Write something...") },
                            textStyle = TextStyle(
                                color = MaterialTheme.colors.onPrimary,
                                fontSize = 16.sp
                            ),
                            value = noteText.value,
                            onValueChange = { noteText.value = it }
                        )
                    }
                }
            },
            floatingActionButton = {
                val noteTitle = titleText.value
                val noteContent = noteText.value

                if (NoteValidator.isValidNote(noteTitle, noteContent)) {
                    ExtendedFloatingActionButton(
                        text = { Text("Save", color = Color.White) },
                        icon = {
                            Icon(
                                Icons.Filled.Done,
                                "Save",
                                tint = Color.White
                            )
                        },
                        onClick = { viewModel.updateNote(noteTitle, noteContent) },
                        backgroundColor = MaterialTheme.colors.primary
                    )
                } else {
                    ShowToast("Note title or note text are not valid!")
                }
            }
        )

        when (val state = updateState.value) {
            is ViewState.Loading -> LoaderDialog()
            is ViewState.Success -> navController.navigateUp()
            is ViewState.Failed -> FailureDialog(state.message)
        }

        when (val state = deleteState.value) {
            is ViewState.Loading -> LoaderDialog()
            is ViewState.Success -> navController.navigateUp()
            is ViewState.Failed -> FailureDialog(state.message)
        }
    }
}

fun shareNote(activity: Activity, title: String, note: String) {
    val shareMsg = activity.getString(
        R.string.text_message_share,
        title,
        note
    )

    val intent = ShareCompat.IntentBuilder(activity)
        .setType("text/plain")
        .setText(shareMsg)
        .intent

    activity.startActivity(Intent.createChooser(intent, null))
}

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Composable
fun noteDetailViewModel(noteId: String): NoteDetailViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        MainActivity.ViewModelFactoryProvider::class.java
    ).noteDetailViewModelFactory()

    return viewModel(factory = NoteDetailViewModel.provideFactory(factory, noteId))
}
