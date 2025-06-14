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

import dagger.Lazy
import dagger.Subcomponent
import dev.shreyaspatil.noty.api.di.module.DaoModule
import dev.shreyaspatil.noty.data.dao.NoteDao
import dev.shreyaspatil.noty.data.dao.UserDao
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [DaoModule::class])
interface DaoComponent {
    fun userDao(): Lazy<UserDao>
    fun noteDao(): Lazy<NoteDao>
}
