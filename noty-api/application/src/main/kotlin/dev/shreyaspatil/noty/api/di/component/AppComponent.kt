/*
 * Copyright 2021 Shreyas Patil
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

package dev.shreyaspatil.noty.api.di.component

import dagger.BindsInstance
import dagger.Component
import io.ktor.server.application.Application

@Component
interface AppComponent {
    fun application(): Application

    fun controllerComponent(): ControllerComponent
    fun configComponent(): ConfigComponent
    fun daoComponent(): DaoComponent

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withApplication(application: Application): Builder
        fun build(): AppComponent
    }
}
