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

package dev.shreyaspatil.noty.composeapp.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import dev.shreyaspatil.noty.composeapp.component.action.DeleteAction
import dev.shreyaspatil.noty.composeapp.component.action.PinAction
import dev.shreyaspatil.noty.composeapp.component.action.ShareAction
import dev.shreyaspatil.noty.composeapp.component.action.ShareActionItem
import dev.shreyaspatil.noty.composeapp.component.action.ShareDropdown
import dev.shreyaspatil.noty.composeapp.component.dialog.ConfirmationDialog
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyScaffold
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyTopAppBar
import dev.shreyaspatil.noty.composeapp.component.text.NoteField
import dev.shreyaspatil.noty.composeapp.component.text.NoteTitleField
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.composeapp.utils.collection.composeImmutableListOf
import dev.shreyaspatil.noty.utils.saveBitmap
import dev.shreyaspatil.noty.utils.share.shareImage
import dev.shreyaspatil.noty.utils.share.shareNoteText
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun NoteDetailsScreen(
    viewModel: NoteDetailViewModel,
    onNavigateUp: () -> Unit,
) {
    val state by viewModel.collectState()
    val context = LocalContext.current

    var showDeleteNoteConfirmation by remember { mutableStateOf(false) }

    NoteDetailContent(
        title = state.title ?: "",
        note = state.note ?: "",
        error = state.error,
        isPinned = state.isPinned,
        showSaveButton = state.showSave,
        onTitleChange = viewModel::setTitle,
        onNoteChange = viewModel::setNote,
        onPinClick = viewModel::togglePin,
        onSaveClick = viewModel::save,
        onDeleteClick = { showDeleteNoteConfirmation = true },
        onNavigateUp = onNavigateUp,
        onShareNoteAsText = { context.shareNoteText(state.title ?: "", state.note ?: "") },
        onShareNoteAsImage = { bitmap ->
            val uri = saveBitmap(context, bitmap.asAndroidBitmap())
            if (uri != null) {
                context.shareImage(uri)
            }
        },
    )

    DeleteNoteConfirmation(
        show = showDeleteNoteConfirmation,
        onConfirm = viewModel::delete,
        onDismiss = { showDeleteNoteConfirmation = false },
    )

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onNavigateUp()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteDetailContent(
    title: String,
    note: String,
    error: String?,
    isPinned: Boolean,
    showSaveButton: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPinClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareNoteAsText: () -> Unit,
    onShareNoteAsImage: (ImageBitmap) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val captureController = rememberCaptureController()

    NotyScaffold(
        error = error,
        modifier =
            Modifier
                .focusRequester(focusRequester)
                .focusable(true),
        notyTopAppBar = {
            NotyTopAppBar(
                onNavigateUp = onNavigateUp,
                actions = {
                    NoteDetailActions(
                        isPinned = isPinned,
                        onPinClick = onPinClick,
                        onDeleteClick = onDeleteClick,
                        onShareNoteAsTextClick = onShareNoteAsText,
                        onShareNoteAsImageClick = {
                            focusRequester.requestFocus()
                            coroutineScope.launch {
                                onShareNoteAsImage(captureController.captureAsync().await())
                            }
                        },
                    )
                },
            )
        },
        content = {
            NoteDetailBody(
                modifier =
                    Modifier
                        .padding(it)
                        .capturable(captureController)
                        .background(MaterialTheme.colorScheme.background),
                title = title,
                onTitleChange = onTitleChange,
                note = note,
                onNoteChange = onNoteChange,
            )
        },
        floatingActionButton = {
            if (showSaveButton) {
                ExtendedFloatingActionButton(
                    text = { Text("Save") },
                    icon = { Icon(Icons.Filled.Done, "Save") },
                    onClick = onSaveClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.imePadding(),
                )
            }
        },
    )
}

@Composable
private fun NoteDetailActions(
    isPinned: Boolean,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareNoteAsTextClick: () -> Unit,
    onShareNoteAsImageClick: () -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val shareActions =
        remember {
            composeImmutableListOf(
                ShareActionItem(
                    label = "Text",
                    onActionClick = onShareNoteAsTextClick,
                ),
                ShareActionItem(
                    label = "Image",
                    onActionClick = onShareNoteAsImageClick,
                ),
            )
        }

    PinAction(isPinned, onClick = onPinClick)
    DeleteAction(onClick = onDeleteClick)
    ShareAction(onClick = { dropdownExpanded = true })
    ShareDropdown(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        shareActions = shareActions,
    )
}

@Composable
private fun NoteDetailBody(
    modifier: Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        NoteTitleField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
            value = title,
            onTextChange = onTitleChange,
        )

        NoteField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 32.dp),
            value = note,
            onTextChange = onNoteChange,
        )
    }
}

@Composable
fun DeleteNoteConfirmation(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (show) {
        ConfirmationDialog(
            title = "Delete?",
            message = "Sure want to delete this note?",
            onConfirmedYes = onConfirm,
            onConfirmedNo = onDismiss,
            onDismissed = onDismiss,
        )
    }
}

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewNoteDetailScreen() {
    NotyPreview {
        NoteDetailContent(
            title = "Existing note title",
            note = "Existing note body",
            error = null,
            isPinned = false,
            showSaveButton = true,
            onTitleChange = {},
            onNoteChange = {},
            onPinClick = {},
            onSaveClick = {},
            onNavigateUp = {},
            onDeleteClick = {},
            onShareNoteAsImage = {},
            onShareNoteAsText = {},
        )
    }
}
