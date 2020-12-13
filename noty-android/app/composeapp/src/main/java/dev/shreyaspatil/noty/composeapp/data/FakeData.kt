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

package dev.shreyaspatil.noty.composeapp.data

import dev.shreyaspatil.noty.core.model.Note

object FakeData {

    val noteList = listOf(
        Note(
            "1",
            "Android Developer",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 1
        ),
        Note(
            "2",
            "Kotlin Ext funcs",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry",
            created = 2
        ),
        Note(
            "3",
            "Android ViewBinding",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 3
        ),
        Note(
            "4",
            "WorkManager",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 4
        ),
        Note(
            "5",
            "Navigation component",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 5
        ),
        Note(
            "6",
            "Jetpack compose",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 6
        ),
        Note(
            "7",
            "DataStore",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            created = 7
        ),
    )
}
