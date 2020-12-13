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

package dev.shreyaspatil.noty.composeapp.view.signup

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dev.shreyaspatil.noty.composeapp.navigation.Screen
import dev.shreyaspatil.noty.composeapp.ui.typography
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel,
) {

    ScrollableColumn {

        ConstraintLayout(Modifier.fillMaxSize().background(Color.White)) {

            val (title, et_username, et_password, et_confirmPassword, btn_signup, txt_login) = createRefs()

            Text(
                text = "Create\naccount",
                style = typography.h4,
                color = Color.Black,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top, margin = 60.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
            )

            val username = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(et_username) {
                        top.linkTo(title.bottom, margin = 50.dp)
                    },
                label = { Text(text = "Username") },
                leadingIcon = { Icon(Icons.Outlined.Person) },
                textStyle = typography.subtitle1,
                backgroundColor = MaterialTheme.colors.background,
                value = username.value,
                onValueChange = { username.value = it }
            )

            val password = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(et_password) {
                        top.linkTo(et_username.bottom, margin = 16.dp)
                    },
                label = { Text(text = "Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock) },
                textStyle = typography.subtitle1,
                backgroundColor = MaterialTheme.colors.background,
                value = password.value,
                onValueChange = { password.value = it }
            )

            val confirmPassword = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(et_confirmPassword) {
                        top.linkTo(et_password.bottom, margin = 16.dp)
                    },
                label = { Text(text = "Confirm password") },
                leadingIcon = { Icon(Icons.Outlined.Lock) },
                textStyle = typography.subtitle1,
                backgroundColor = MaterialTheme.colors.background,
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it }
            )

            Button(
                onClick = {
                    onRegisterClicked(
                        viewModel,
                        username.value,
                        password.value,
                        confirmPassword.value
                    )
//                    navController.navigate(Screen.Notes.route)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(btn_signup) {
                        top.linkTo(et_confirmPassword.bottom, margin = 40.dp)
                    },
            ) {
                Text(style = typography.subtitle1, color = Color.White, text = "Create account")
            }

            Text(
                text = annotatedString {
                    // push black so entire text will be in black
                    pushStyle(SpanStyle(color = Color.Black))
                    // append new text, this text will be rendered as black
                    append("Already have an account? Login")
                    // then style the last added word as red, exclamation mark will be red
                    addStyle(SpanStyle(color = MaterialTheme.colors.primary), 24, this.length)
                    toAnnotatedString()
                },
                style = typography.subtitle1,
                modifier = Modifier.constrainAs(txt_login) {
                    top.linkTo(btn_signup.bottom, margin = 24.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }.clickable(
                    onClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            )
        }
    }
}

@ExperimentalCoroutinesApi
fun onRegisterClicked(
    viewModel: RegisterViewModel,
    usernameValue: TextFieldValue,
    passwordValue: TextFieldValue,
    confirmPasswordValue: TextFieldValue,
) {
    val username = usernameValue.text
    val password = passwordValue.text
    val confirmPassword = confirmPasswordValue.text
    viewModel.register(username = username, password = password)
}
