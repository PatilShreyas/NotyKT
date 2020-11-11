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

package dev.shreyaspatil.noty.testutil

import dev.shreyaspatil.noty.core.model.Note

object FakeNoteDataGenerator {

    fun getNotesList() =
        listOf(
            Note(
                id = "fake_id_1",
                title = "fake_title_1",
                note = "fake_note_1",
                created = 1000
            ),
            Note(
                id = "fake_id_12",
                title = "fake_title_2",
                note = "fake_note_2",
                created = 2000
            )
        )


    fun getNote() = Note(
        id = "fake_id_1",
        title = "fake_title_1",
        note = "fake_note_1",
        created = 1000
    )
}
