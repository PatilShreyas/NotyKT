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

package dev.shreyaspatil.noty.core.task

import dev.shreyaspatil.noty.core.model.NotyTask
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Singleton

@Singleton
interface NotyTaskManager {
    fun syncNotes(): UUID
    fun scheduleTask(notyTask: NotyTask): UUID
    fun getTaskState(taskId: UUID): TaskState?
    fun observeTask(taskId: UUID): Flow<TaskState>
    fun abortAllTasks()
    fun getTaskIdFromNoteId(noteId: String) = noteId
}
