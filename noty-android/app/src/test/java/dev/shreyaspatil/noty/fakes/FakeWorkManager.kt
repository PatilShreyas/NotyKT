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

package dev.shreyaspatil.noty.fakes

import androidx.lifecycle.asLiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * WorkManager's fake implementation using Mockk
 */
class FakeWorkManager {
    /**
     * Useful for capturing scheduled work requests
     */
    val oneTimeWorkRequests = mutableListOf<OneTimeWorkRequest>()

    /**
     * To track whether work is cancelled or not
     */
    var allWorkCancelled = false

    /**
     * Fake states to be maintained for scheduled fake work
     */
    val fakeWorkStates = mutableMapOf<UUID, WorkInfo.State>()

    /**
     * For observing work info LiveData
     */
    lateinit var fakeWorkStatesForObserve: Flow<WorkInfo.State>

    // Create a mocked WorkManager instance
    val mockWorkManager = mockk<WorkManager>()

    init {
        // Setup the mock to handle the methods used in tests
        io.mockk.every {
            mockWorkManager.enqueueUniqueWork(
                any(),
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>(),
            )
        } answers {
            val request = thirdArg<OneTimeWorkRequest>()
            oneTimeWorkRequests.add(request)
            mockk()
        }

        io.mockk.every {
            mockWorkManager.getWorkInfoById(any())
        } answers {
            val id = firstArg<UUID>()
            futureWorkInfo(fakeWorkStates[id] ?: WorkInfo.State.FAILED)
        }

        io.mockk.every {
            mockWorkManager.cancelAllWork()
        } answers {
            allWorkCancelled = true
            mockk()
        }

        io.mockk.every {
            mockWorkManager.getWorkInfoByIdLiveData(any())
        } answers {
            fakeWorkStatesForObserve.map { workInfo(it) }.asLiveData()
        }
    }
}

fun futureWorkInfo(state: WorkInfo.State): ListenableFuture<WorkInfo?> =
    object : ListenableFuture<WorkInfo?> {
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true

        override fun isCancelled(): Boolean = true

        override fun isDone(): Boolean = true

        override fun get(): WorkInfo? = workInfo(state)

        override fun get(
            timeout: Long,
            unit: TimeUnit?,
        ): WorkInfo? = TODO("Not needed")

        override fun addListener(
            listener: Runnable,
            executor: Executor,
        ) = TODO("Not needed")
    }

fun workInfo(state: WorkInfo.State): WorkInfo {
    val id = UUID.randomUUID()
    val fakeData = Data.Builder().build()
    return WorkInfo(id, state, emptySet<String>(), fakeData, fakeData)
}
