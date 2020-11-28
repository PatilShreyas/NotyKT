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

package dev.shreyaspatil.noty.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.di.RemoteRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Singleton

@Singleton
class NotySyncWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @RemoteRepository private val remoteNoteRepository: NotyNoteRepository,
    @LocalRepository private val localNoteRepository: NotyNoteRepository,
    private val notyTaskManager: NotyTaskManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return syncNotes()
    }

    private suspend fun syncNotes(): Result {
        return try {
            // Fetches all notes from remote.
            // If task of any note is still pending, skip it.
            val notes = fetchRemoteNotes().filter { note -> shouldReplaceNote(note.id) }

            // Add/Replace notes locally.
            localNoteRepository.addNotes(notes)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun fetchRemoteNotes(): List<Note> {
        return when (val response = remoteNoteRepository.getAllNotes().first()) {
            is ResponseResult.Success -> response.data
            is ResponseResult.Error -> throw Exception(response.message)
        }
    }

    private fun shouldReplaceNote(noteId: String): Boolean {
        val taskId = notyTaskManager.getTaskIdFromNoteId(noteId).toUUID()
        val state = notyTaskManager.getTaskState(taskId)

        return (state == null || state != TaskState.SCHEDULED)
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}
