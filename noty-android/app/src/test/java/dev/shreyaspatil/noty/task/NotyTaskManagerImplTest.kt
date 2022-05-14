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
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.util.*

class NotyTaskManagerImplTest : BehaviorSpec() {

    override suspend fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        setupAsyncTaskExecutor()
    }

    init {
        // LiveData's asFlow() method uses Main dispatcher under the hood
        Dispatchers.setMain(TestCoroutineDispatcher())

        val workManager = spyk(FakeWorkManager())
        val workRequests = workManager.oneTimeWorkRequests

        val manager = NotyTaskManagerImpl(workManager)

        Given("The tasks") {
            val workStates = listOf(
                UUID.randomUUID() to WorkInfo.State.ENQUEUED,
                UUID.randomUUID() to WorkInfo.State.RUNNING,
                UUID.randomUUID() to WorkInfo.State.BLOCKED,
                UUID.randomUUID() to WorkInfo.State.CANCELLED,
                UUID.randomUUID() to WorkInfo.State.SUCCEEDED,
                UUID.randomUUID() to WorkInfo.State.FAILED
            )

            workStates.forEach { (id, state) -> workManager.fakeWorkStates[id] = state }

            // To test observing work status flow
            // Here delay is added because LiveData conflates the emitted result if emissions
            // are too fast and that's the reason we have used Unconfined dispatcher here so that
            // it can respect the delay.
            workManager.fakeWorkStatesForObserve = flow {
                emit(WorkInfo.State.ENQUEUED)
                delay(100)
                emit(WorkInfo.State.RUNNING)
                delay(100)
                emit(WorkInfo.State.FAILED)
                delay(100)
            }.flowOn(Dispatchers.Unconfined)

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
                val taskStates = manager.observeTask(UUID.randomUUID()).toList()

                Then("Valid task states should be emitted") {
                    taskStates shouldBe listOf(TaskState.SCHEDULED, TaskState.FAILED)
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

    override suspend fun afterSpec(spec: Spec) {
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
