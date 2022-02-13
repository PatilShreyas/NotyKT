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

package dev.shreyaspatil.noty.task

import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.utils.ext.putEnum
import dev.shreyaspatil.noty.worker.NotySyncWorker
import dev.shreyaspatil.noty.worker.NotyTaskWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotyTaskManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : NotyTaskManager {

    override fun syncNotes(): UUID {
        val notySyncWorker = OneTimeWorkRequestBuilder<NotySyncWorker>()
            .setConstraints(getRequiredConstraints())
            .build()

        workManager.enqueueUniqueWork(
            SYNC_TASK_NAME,
            ExistingWorkPolicy.REPLACE,
            notySyncWorker
        )

        return notySyncWorker.id
    }

    override fun scheduleTask(notyTask: NotyTask): UUID {
        val notyTaskWorker = OneTimeWorkRequestBuilder<NotyTaskWorker>()
            .setConstraints(getRequiredConstraints())
            .setInputData(generateData(notyTask))
            .build()

        workManager.enqueueUniqueWork(
            getTaskIdFromNoteId(notyTask.noteId),
            ExistingWorkPolicy.REPLACE,
            notyTaskWorker
        )

        return notyTaskWorker.id
    }

    override fun getTaskState(taskId: UUID): TaskState? = runCatching {
        workManager.getWorkInfoById(taskId)
            .get()
            .let { mapWorkInfoStateToTaskState(it.state) }
    }.getOrNull()

    override fun observeTask(taskId: UUID): Flow<TaskState> {
        return workManager.getWorkInfoByIdLiveData(taskId)
            .asFlow()
            .map { mapWorkInfoStateToTaskState(it.state) }
            .transformWhile { taskState ->
                emit(taskState)

                // This is to terminate this flow when terminal state is arrived
                !taskState.isTerminalState
            }.distinctUntilChanged()
    }

    override fun abortAllTasks() {
        workManager.cancelAllWork()
    }

    private fun mapWorkInfoStateToTaskState(state: State): TaskState = when (state) {
        State.ENQUEUED, State.RUNNING, State.BLOCKED -> TaskState.SCHEDULED
        State.CANCELLED -> TaskState.CANCELLED
        State.FAILED -> TaskState.FAILED
        State.SUCCEEDED -> TaskState.COMPLETED
    }

    private fun generateData(notyTask: NotyTask) = Data.Builder()
        .putString(NotyTaskWorker.KEY_NOTE_ID, notyTask.noteId)
        .putEnum(NotyTaskWorker.KEY_TASK_TYPE, notyTask.action)
        .build()

    private fun getRequiredConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        const val SYNC_TASK_NAME = "Task-Noty-Sync"
    }
}
