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

package dev.shreyaspatil.noty.composeapp

import android.app.Application
import android.util.Log
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dev.shreyaspatil.noty.appfunctions.NotyAppFunctions
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class NotyApp :
    Application(),
    Configuration.Provider,
    AppFunctionConfiguration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var appFunctions: Provider<NotyAppFunctions>

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() =
            AppFunctionConfiguration
                .Builder()
                .addEnclosingClassFactory(NotyAppFunctions::class.java) { appFunctions.get() }
                .build()
}
