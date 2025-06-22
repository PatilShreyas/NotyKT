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

package dev.shreyaspatil.noty.api.plugin

import dagger.Lazy
import dev.shreyaspatil.noty.api.auth.JWTController
import dev.shreyaspatil.noty.api.auth.NotyJWTController
import dev.shreyaspatil.noty.api.auth.principal.UserPrincipal
import dev.shreyaspatil.noty.data.dao.UserDao
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import java.util.UUID

fun Application.configureAuthentication(
    jwtController: Lazy<JWTController> = appComponent.controllerComponent().jwtController(),
    userDao: Lazy<UserDao> = appComponent.daoComponent().userDao(),
) {
    install(Authentication) {
        jwt {
            verifier(jwtController.get().verifier)
            validate {
                // Extract userId from token
                val userId = it.payload.getClaim(NotyJWTController.CLAIM).asString()

                // Return Principle if user exists otherwise null
                val user = userDao.get().findByUUID(UUID.fromString(userId))
                if (user != null) UserPrincipal(user.id) else null
            }
        }
    }
}
