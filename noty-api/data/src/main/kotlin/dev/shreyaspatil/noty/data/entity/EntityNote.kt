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

package dev.shreyaspatil.noty.data.entity

import dev.shreyaspatil.noty.data.database.table.Notes
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class EntityNote(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<EntityNote>(Notes)

    var user by EntityUser referencedOn Notes.user
    var title by Notes.title
    var note by Notes.note
    var created by Notes.created
    var isPinned by Notes.isPinned
    var updated by Notes.updated
}
