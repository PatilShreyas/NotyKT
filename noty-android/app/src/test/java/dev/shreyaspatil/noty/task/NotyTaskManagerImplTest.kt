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

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.fakes.FakeWorkManager
import dev.shreyaspatil.noty.utils.ext.getEnum
import dev.shreyaspatil.noty.worker.NotyTaskWorker
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalStdlibApi::class)
class NotyTaskManagerImplTest {
    private lateinit var fakeWorkManager: FakeWorkManager
    private lateinit var workManager: androidx.work.WorkManager
    private lateinit var workRequests: MutableList<OneTimeWorkRequest>
    private lateinit var manager: NotyTaskManagerImpl
    private lateinit var workStates: List<Pair<UUID, WorkInfo.State>>

    @BeforeEach
    fun setup() {
        setupAsyncTaskExecutor()

        fakeWorkManager = FakeWorkManager()
        workManager = fakeWorkManager.mockWorkManager
        workRequests = fakeWorkManager.oneTimeWorkRequests
        manager = NotyTaskManagerImpl(workManager)

        // Setup test data
        Dispatchers.setMain(UnconfinedTestDispatcher())
        workStates =
            listOf(
                UUID.randomUUID() to WorkInfo.State.ENQUEUED,
                UUID.randomUUID() to WorkInfo.State.RUNNING,
                UUID.randomUUID() to WorkInfo.State.BLOCKED,
                UUID.randomUUID() to WorkInfo.State.CANCELLED,
                UUID.randomUUID() to WorkInfo.State.SUCCEEDED,
                UUID.randomUUID() to WorkInfo.State.FAILED
            )

        workStates.forEach { (id, state) -> fakeWorkManager.fakeWorkStates[id] = state }

        // To test observing work status flow
        // Here delay is added because LiveData conflates the emitted result if emissions
        // are too fast and that's the reason we have used Unconfined dispatcher here so that
        // it can respect the delay.
        fakeWorkManager.fakeWorkStatesForObserve =
            flow {
                emit(WorkInfo.State.ENQUEUED)
                delay(100)
                emit(WorkInfo.State.RUNNING)
                delay(100)
                emit(WorkInfo.State.FAILED)
                delay(100)
            }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        cleanupAsyncTaskExecutor()
    }

    @Test
    fun `syncNotes should schedule unique work with internet connectivity constraint`() {
        // When
        manager.syncNotes()

        // Then
        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                NotyTaskManagerImpl.SYNC_TASK_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }

        val spec = workRequests.last().workSpec
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
    }

    @Test
    fun `scheduleTask should schedule unique work with proper constraints and data`() {
        // Given
        val noteId = "note-1234"
        val notyTask = NotyTask.create(noteId)

        // When
        manager.scheduleTask(notyTask)

        // Then
        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                noteId,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }

        val spec = workRequests.last().workSpec
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)

        val inputData = spec.input
        val actualNoteId = inputData.getString(NotyTaskWorker.KEY_NOTE_ID)
        val actualTask = inputData.getEnum<NotyTaskAction>(NotyTaskWorker.KEY_TASK_TYPE)

        assertEquals(noteId, actualNoteId)
        assertEquals(NotyTaskAction.CREATE, actualTask)
    }

    @Test
    fun `getTaskState should return correct mapped task state`() {
        // When
        val taskStates = workStates.map { (id, _) -> manager.getTaskState(id) }

        // Then
        assertEquals(
            listOf(
                TaskState.SCHEDULED,
                TaskState.SCHEDULED,
                TaskState.SCHEDULED,
                TaskState.CANCELLED,
                TaskState.COMPLETED,
                TaskState.FAILED
            ),
            taskStates
        )
    }

    @Test
    fun `observeTask should emit correct task states`() = runTest {
        // When
        val taskStates = manager.observeTask(UUID.randomUUID()).toList()

        // Then
        assertEquals(listOf(TaskState.SCHEDULED, TaskState.FAILED), taskStates)
    }

    @Test
    fun `abortAllTasks should cancel all work in WorkManager`() {
        // When
        manager.abortAllTasks()

        // Then
        verify(exactly = 1) { workManager.cancelAllWork() }
    }

    private fun setupAsyncTaskExecutor() {
        ArchTaskExecutor.getInstance().setDelegate(
            object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) {
                    runnable.run()
                }

                override fun postToMainThread(runnable: Runnable) {
                    runnable.run()
                }

                override fun isMainThread(): Boolean {
                    return true
                }
            }
        )
    }

    private fun cleanupAsyncTaskExecutor() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
