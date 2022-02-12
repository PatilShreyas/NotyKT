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
import androidx.lifecycle.liveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.utils.ext.getEnum
import dev.shreyaspatil.noty.worker.NotyTaskWorker
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class NotyTaskManagerImplTest : BehaviorSpec() {

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        setupAsyncTaskExecutor()
    }

    init {
        // LiveData's asFlow() method uses Main dispatcher under the hood
        Dispatchers.setMain(TestCoroutineDispatcher())

        /**
         * Useful for testing [NotyTaskManagerImpl.observeTask] method
         */
        val randomWorkId = UUID.randomUUID()

        /**
         * Useful for testing [NotyTaskManagerImpl.getTaskState] method
         */
        val workStates = listOf(
            UUID.randomUUID() to WorkInfo.State.ENQUEUED,
            UUID.randomUUID() to WorkInfo.State.RUNNING,
            UUID.randomUUID() to WorkInfo.State.BLOCKED,
            UUID.randomUUID() to WorkInfo.State.CANCELLED,
            UUID.randomUUID() to WorkInfo.State.SUCCEEDED,
            UUID.randomUUID() to WorkInfo.State.FAILED
        )

        /**
         * Useful for capturing scheduled works
         */
        val workRequests = mutableListOf<OneTimeWorkRequest>()

        val workManager: WorkManager = mockk {
            every { enqueueUniqueWork(any(), any(), capture(workRequests)) } returns mockk()
            every { cancelAllWork() } returns mockk()

            workStates.forEach { (id, state) ->
                every { getWorkInfoById(id) } returns futureWorkInfo(state)
            }

            every { getWorkInfoByIdLiveData(randomWorkId) } returns liveData {
                emit(workInfo(WorkInfo.State.ENQUEUED))
                emit(workInfo(WorkInfo.State.RUNNING))
                emit(workInfo(WorkInfo.State.FAILED))
            }
        }

        val manager = NotyTaskManagerImpl(workManager)

        Given("The tasks") {
            When("Notes are synced") {
                manager.syncNotes()

                Then("Unique work should be get scheduled") {
                    verify(exactly = 1) {
                        workManager.enqueueUniqueWork(
                            NotyTaskManagerImpl.SYNC_TASK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            any<OneTimeWorkRequest>()
                        )
                    }
                }

                Then("Work spec should contain internet connectivity constraint") {
                    val spec = workRequests.last().workSpec
                    spec.constraints.requiredNetworkType shouldBe NetworkType.CONNECTED
                }
            }

            When("The noty task is scheduled") {
                val noteId = "note-1234"
                val notyTask = NotyTask.create(noteId)

                manager.scheduleTask(notyTask)

                Then("Unique work should be get scheduled with work name having note ID") {
                    verify(exactly = 1) {
                        workManager.enqueueUniqueWork(
                            noteId,
                            ExistingWorkPolicy.REPLACE,
                            any<OneTimeWorkRequest>()
                        )
                    }
                }

                val spec = workRequests.last().workSpec

                Then("Work spec should contain internet connectivity constraint") {
                    spec.constraints.requiredNetworkType shouldBe NetworkType.CONNECTED
                }

                Then("Work spec should contain data") {
                    val inputData = spec.input
                    val actualNoteId = inputData.getString(NotyTaskWorker.KEY_NOTE_ID)
                    val actualTask = inputData.getEnum<NotyTaskAction>(NotyTaskWorker.KEY_TASK_TYPE)

                    actualNoteId shouldBe noteId
                    actualTask shouldBe NotyTaskAction.CREATE
                }
            }

            When("The task state is retrieved") {
                val taskStates = workStates.map { (id, _) -> manager.getTaskState(id) }

                Then("Valid mapped task state should be returned") {
                    taskStates shouldBe listOf(
                        TaskState.SCHEDULED,
                        TaskState.SCHEDULED,
                        TaskState.SCHEDULED,
                        TaskState.CANCELLED,
                        TaskState.COMPLETED,
                        TaskState.FAILED
                    )
                }
            }

            When("The task is observed") {
                val taskStates = manager.observeTask(randomWorkId).take(1).toList()

                Then("Valid task states should be emitted") {
                    // Recently emitted value by LiveData will be collected by this Flow
                    taskStates shouldBe listOf(TaskState.FAILED)
                }
            }

            When("All tasks are aborted") {
                manager.abortAllTasks()

                Then("All tasks should get cancelled in WorkManager") {
                    verify(exactly = 1) { workManager.cancelAllWork() }
                }
            }
        }
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        cleanupAsyncTaskExecutor()
    }

    private fun setupAsyncTaskExecutor() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean {
                return true
            }
        })
    }

    private fun cleanupAsyncTaskExecutor() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}

fun futureWorkInfo(
    state: WorkInfo.State
): ListenableFuture<WorkInfo> = object : ListenableFuture<WorkInfo> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true
    override fun isCancelled(): Boolean = true
    override fun isDone(): Boolean = true
    override fun get(): WorkInfo = workInfo(state)
    override fun get(timeout: Long, unit: TimeUnit?): WorkInfo = TODO("Not needed")
    override fun addListener(listener: Runnable?, executor: Executor?) = TODO("Not needed")
}

fun workInfo(state: WorkInfo.State): WorkInfo {
    val fakeData = Data.Builder().build()
    return WorkInfo(UUID.randomUUID(), state, fakeData, emptyList(), fakeData, 1)
}
