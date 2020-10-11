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

package dev.shreyaspatil.noty.data.dao

import dev.shreyaspatil.noty.data.database.table.Users
import dev.shreyaspatil.noty.data.entity.EntityUser
import dev.shreyaspatil.noty.data.model.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import javax.inject.Inject

class UserDao @Inject constructor() {

    fun addUser(username: String, password: String): User = transaction {
        EntityUser.new {
            this.username = username
            this.password = password
        }
    }.let { User.fromEntity(it) }

    fun getUserByUuid(uuid: UUID): User? = transaction {
        EntityUser.findById(uuid)
    }?.let { User.fromEntity(it) }

    fun getByUsernameAndPassword(username: String, password: String): User? = transaction {
        EntityUser.find {
            (Users.username eq username) and (Users.password eq password)
        }.firstOrNull()
    }?.let { User.fromEntity(it) }

    fun isUsernameAvailable(username: String): Boolean {
        return transaction {
            EntityUser.find { Users.username eq username }.firstOrNull()
        } == null
    }

    fun isUserExists(id: String): Boolean {
        return transaction {
            EntityUser.find { Users.id eq UUID.fromString(id) }.firstOrNull()
        } != null
    }
}