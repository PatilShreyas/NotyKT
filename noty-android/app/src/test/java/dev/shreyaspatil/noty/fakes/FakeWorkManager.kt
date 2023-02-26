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

import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * WorkManager's fake implementation
 */
class FakeWorkManager : WorkManager() {
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

    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): Operation {
        oneTimeWorkRequests.addAll(work)
        return mockk()
    }

    override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
        return futureWorkInfo(fakeWorkStates[id]!!)
    }

    override fun cancelAllWork(): Operation {
        allWorkCancelled = true
        return mockk()
    }

    override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo> {
        return fakeWorkStatesForObserve.map { workInfo(it) }.asLiveData()
    }

    override fun getConfiguration(): Configuration {
        TODO("Not yet implemented")
    }

    override fun enqueue(requests: MutableList<out WorkRequest>): Operation {
        TODO("Not yet implemented")
    }

    override fun beginWith(work: MutableList<OneTimeWorkRequest>): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun beginUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        periodicWork: PeriodicWorkRequest
    ): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelWorkById(id: UUID): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelAllWorkByTag(tag: String): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelUniqueWork(uniqueWorkName: String): Operation {
        TODO("Not yet implemented")
    }

    override fun createCancelPendingIntent(id: UUID): PendingIntent {
        TODO("Not yet implemented")
    }

    override fun pruneWork(): Operation {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTagLiveData(tag: String): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTag(tag: String): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWorkLiveData(
        uniqueWorkName: String
    ): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWork(
        uniqueWorkName: String
    ): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosLiveData(workQuery: WorkQuery): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfos(workQuery: WorkQuery): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun updateWork(request: WorkRequest): ListenableFuture<UpdateResult> {
        TODO("Not yet implemented")
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
    override fun addListener(listener: Runnable, executor: Executor) = TODO("Not needed")
}

fun workInfo(state: WorkInfo.State): WorkInfo {
    val fakeData = Data.Builder().build()
    return WorkInfo(UUID.randomUUID(), state, fakeData, emptyList(), fakeData, 1, 0)
}
