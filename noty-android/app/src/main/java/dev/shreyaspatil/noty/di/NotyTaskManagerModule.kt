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

package dev.shreyaspatil.noty.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.task.NotyTaskManagerImpl

@Module
@InstallIn(SingletonComponent::class)
interface NotyTaskManagerModule {

    @Binds
    fun notyTaskManager(notyTaskManager: NotyTaskManagerImpl): NotyTaskManager
}
