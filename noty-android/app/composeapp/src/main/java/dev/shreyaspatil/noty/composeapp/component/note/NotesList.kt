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

package dev.shreyaspatil.noty.composeapp.component.note

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.utils.collection.ComposeImmutableList
import dev.shreyaspatil.noty.core.model.Note

@Composable
fun NotesList(
    notes: ComposeImmutableList<Note>,
    onClick: (Note) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.testTag("notesList"),
    ) {
        items(
            items = notes,
            itemContent = { note ->
                NoteCard(
                    title = note.title,
                    note = note.note,
                    isPinned = note.isPinned,
                    onNoteClick = { onClick(note) },
                )
            },
            key = { Triple(it.id, it.title, it.note) },
        )
    }
}
