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

package dev.shreyaspatil.noty.utils.validator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain

class NoteValidatorTest : BehaviorSpec({
    Given("Note title and note content") {
        And("They are valid") {
            val titleAndNote = listOf(
                "title" to "content",
                "    Hey there    " to "Hey there, this is body of a note",
                "1234" to "Hi"
            )

            When("Title and note is validated") {
                val areValid = titleAndNote.map { (title, note) ->
                    NoteValidator.isValidNote(title, note)
                }

                Then("Note should be valid") {
                    areValid shouldContain true
                    areValid shouldNotContain false
                }
            }
        }

        And("They are invalid") {
            val titleAndNote = listOf(
                "hi" to "content",
                "    Hey   " to "Hey there, this is body of a note",
                "1234" to ""
            )

            When("Title and note is validated") {
                val areValid = titleAndNote.map { (title, note) ->
                    NoteValidator.isValidNote(title, note)
                }

                Then("Note should be valid") {
                    areValid shouldContain false
                    areValid shouldNotContain true
                }
            }
        }
    }
})
