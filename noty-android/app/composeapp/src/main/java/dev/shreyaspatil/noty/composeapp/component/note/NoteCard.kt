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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview

@Composable
fun NoteCard(
    title: String,
    note: String,
    isPinned: Boolean,
    onNoteClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.elevatedCardColors(),
        modifier =
            Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onNoteClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isPinned) {
                    Icon(
                        painterResource(id = R.drawable.ic_pinned),
                        contentDescription = stringResource(R.string.content_description_pinned_note),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
fun PreviewNoteCard() =
    NotyPreview {
        NoteCard(
            title = "Lorem Ipsum",
            note = "Here is note body...",
            isPinned = true,
            onNoteClick = {},
        )
    }

@Preview
@Composable
fun PreviewNoteCardLongContent() =
    NotyPreview {
        NoteCard(
            title = "Lorem Ipsum, and here is the long title... too much long, yeah!! Can it fit good?",
            note = "Here is note body\nSo what do you think about this?\nI think this is looking good\nright?",
            isPinned = false,
            onNoteClick = {},
        )
    }
