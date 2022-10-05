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

package dev.shreyaspatil.noty.repository.local

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.Either.Error
import dev.shreyaspatil.noty.core.repository.Either.Success
import dev.shreyaspatil.noty.data.local.dao.NotesDao
import dev.shreyaspatil.noty.data.local.entity.NoteEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.util.*

class NotyLocalNoteRepositoryTest : BehaviorSpec({
    val notesDao: NotesDao = mockk(relaxUnitFun = true)
    val repository = NotyLocalNoteRepository(notesDao)

    Given("Notes for addition") {
        val note = Note(
            id = "UNIQUE_ID",
            title = "Lorem Ipsum",
            note = "This is body of a note!",
            created = Date().time,
            isPinned = false
        )

        val expectedEntity = NoteEntity(note.id, note.title, note.note, note.created, note.isPinned)

        When("Note is added") {
            And("DAO can add note") {
                val response = repository.addNote(note.title, note.note)
                val noteId = (response as Success).data

                Then("Temporary note ID should be returned") {
                    noteId shouldStartWith "TMP"
                }

                Then("Note should be get added in DAO") {
                    val actualNoteEntity = slot<NoteEntity>()

                    coVerify { notesDao.addNote(capture(actualNoteEntity)) }

                    with(actualNoteEntity.captured) {
                        this.title shouldBe expectedEntity.title
                        this.note shouldBe expectedEntity.note
                    }
                }
            }

            And("DAO cannot add note") {
                coEvery { notesDao.addNote(any()) } throws Exception("")

                val response = repository.addNote(note.title, note.note)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to create a new note"
                }
            }
        }

        When("Notes are added in bulk") {
            repository.addNotes(listOf(note))

            Then("Notes should be get added in DAO") {
                coVerify { notesDao.addNotes(listOf(expectedEntity)) }
            }
        }
    }

    Given("A note") {
        val noteEntity = NoteEntity(
            noteId = "UNIQUE_ID",
            title = "Lorem Ipsum",
            note = "This is body of a note!",
            created = Date().time,
            isPinned = false
        )

        When("Note is observed") {
            coEvery { notesDao.getNoteById(noteEntity.noteId) } returns flowOf(noteEntity)

            val actualNote = repository.getNoteById(noteEntity.noteId)

            Then("Note should be returned") {
                with(actualNote.first()) {
                    this.id shouldBe noteEntity.noteId
                    this.title shouldBe noteEntity.title
                    this.note shouldBe noteEntity.note
                    this.created shouldBe noteEntity.created
                }
            }
        }

        When("Note is updated") {
            val newTitle = "New title"
            val newNote = "New note body"

            And("DAO can update note") {
                coEvery { notesDao.updateNoteById(any(), any(), any()) } just Runs

                repository.updateNote(noteEntity.noteId, newTitle, newNote)

                Then("Note should be get updated in DAO") {
                    coVerify { notesDao.updateNoteById(noteEntity.noteId, newTitle, newNote) }
                }
            }

            And("DAO can NOT update note") {
                coEvery { notesDao.updateNoteById(any(), any(), any()) } throws Exception()

                val response = repository.updateNote(noteEntity.noteId, newTitle, newNote)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to update a note"
                }
            }
        }

        When("Note ID is updated") {
            val newNoteId = "NEW_NOTE_ID"
            repository.updateNoteId(oldNoteId = noteEntity.noteId, newNoteId = newNoteId)

            Then("Note ID should be get updated in DAO") {
                coVerify { notesDao.updateNoteId(noteEntity.noteId, newNoteId) }
            }
        }

        When("Note is deleted") {
            And("DAO can delete note") {
                coEvery { notesDao.deleteNoteById(any()) } just Runs

                repository.deleteNote(noteEntity.noteId)

                Then("Note should be get deleted in DAO") {
                    coVerify { notesDao.deleteNoteById(noteEntity.noteId) }
                }
            }

            And("DAO can NOT delete note") {
                coEvery { notesDao.deleteNoteById(any()) } throws Exception()

                val response = repository.deleteNote(noteEntity.noteId)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to delete a note"
                }
            }
        }

        When("Note is pinned") {
            And("DAO can pin note") {
                coEvery { notesDao.updateNotePin(any(), any()) } just Runs

                repository.pinNote(noteEntity.noteId, true)

                Then("Note should be get pinned in DAO") {
                    coVerify { notesDao.updateNotePin(noteEntity.noteId, true) }
                }
            }

            And("DAO will be unable to pin note") {
                coEvery { notesDao.updateNotePin(any(), any()) } throws Exception()

                val response = repository.pinNote(noteEntity.noteId, false)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to pin the note"
                }
            }
        }
    }

    Given("All notes") {
        val note = NoteEntity("ID", "Title", "Note", 0, false)
        val noteEntities = listOf(note.copy(noteId = "1"), note.copy(noteId = "2"))

        When("Notes are observed") {
            coEvery { notesDao.getAllNotes() } returns flowOf(noteEntities)

            val notes = repository.getAllNotes().first()

            Then("All notes should be retrieved") {
                (notes as Success).data shouldBe noteEntities.map {
                    Note(it.noteId, it.title, it.note, it.created, it.isPinned)
                }
            }
        }

        When("Notes are deleted in bulk") {
            repository.deleteAllNotes()

            Then("All notes should be get deleted in DAO") {
                coVerify { notesDao.deleteAllNotes() }
            }
        }
    }
})
