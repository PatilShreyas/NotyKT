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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * WorkManager's fake implementation
 */
class FakeWorkManager {
    val mockWorkManager: WorkManager = mockk(relaxed = true)

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

    init {
        // Set up the mock to handle enqueueUniqueWork
        every {
            mockWorkManager.enqueueUniqueWork(
                any(),
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>()
            )
        } answers {
            oneTimeWorkRequests.add(arg<OneTimeWorkRequest>(2))
            mockk()
        }

        // Set up the mock to handle getWorkInfoById
        every { mockWorkManager.getWorkInfoById(any()) } answers {
            val id = arg<UUID>(0)
            fakeWorkStates[id]?.let { state -> 
                mockk<ListenableFuture<WorkInfo?>> {
                    every { get() } returns mockk<WorkInfo> {
                        every { this@mockk.state } returns state
                    }
                }
            } ?: mockk(relaxed = true)
        }

        // Set up the mock to handle cancelAllWork
        every { mockWorkManager.cancelAllWork() } answers {
            allWorkCancelled = true
            mockk()
        }

        // Set up the mock to handle getWorkInfoByIdLiveData
        every { mockWorkManager.getWorkInfoByIdLiveData(any()) } answers {
            fakeWorkStatesForObserve.map { state -> 
                mockk<WorkInfo> {
                    every { this@mockk.state } returns state
                }
            }.asLiveData()
        }
    }
}
