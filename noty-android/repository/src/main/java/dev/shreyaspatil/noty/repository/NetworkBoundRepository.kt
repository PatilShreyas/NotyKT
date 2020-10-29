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

package dev.shreyaspatil.noty.repository

import androidx.annotation.WorkerThread
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.data.remote.model.response.BaseResponse
import dev.shreyaspatil.noty.data.remote.model.response.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * A repository which provides resource from local database as well as remote end point.
 *
 * [RESULT] represents the type for database.
 * [REQUEST] represents the type for network.
 */
@ExperimentalCoroutinesApi
abstract class NetworkBoundRepository<RESULT, REQUEST : BaseResponse> {

    suspend fun asFlow() = flow<ResponseResult<RESULT>> {

        emit(ResponseResult.success(fetchFromLocal().first()))

        val response = fetchFromRemote()

        if (response.status == State.SUCCESS) {
            persistData(response)
        } else {
            emit(ResponseResult.error(response.message))
        }

        emitAll(fetchFromLocal().map { ResponseResult.success<RESULT>(it) })
    }.catch { e ->
        emit(ResponseResult.error("Something went wrong!"))
        e.printStackTrace()
    }

    /**
     * Saves the data to the persistence storage.
     */
    @WorkerThread
    protected abstract suspend fun persistData(response: REQUEST)

    /**
     * Returns the data from persistence storage.
     */
    @WorkerThread
    protected abstract fun fetchFromLocal(): Flow<RESULT>

    /**
     * Received data from network.
     */
    @WorkerThread
    protected abstract suspend fun fetchFromRemote(): REQUEST
}
