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

package dev.shreyaspatil.noty.composeapp.rule

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class WorkManagerRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val config = Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    .setExecutor(SynchronousExecutor())
                    .build()
                WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
                try {
                    base?.evaluate()
                } finally {
                    Log.d("WorkManagerRule", "Do some teardown")
                }
            }
        }
    }
}
