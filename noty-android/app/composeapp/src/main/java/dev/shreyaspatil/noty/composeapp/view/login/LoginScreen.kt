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

package dev.shreyaspatil.noty.composeapp.view.login

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dev.shreyaspatil.noty.composeapp.R.drawable.noty_app_logo
import dev.shreyaspatil.noty.composeapp.navigation.Screen
import dev.shreyaspatil.noty.composeapp.ui.typography
import dev.shreyaspatil.noty.composeapp.utils.toast
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
@Composable
fun LoginScreen(navController: NavHostController, loginViewModel: LoginViewModel = viewModel()) {

    val context = AmbientContext.current
    AmbientLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        loginViewModel.authFlow.collect {
            when (it) {
                is ViewState.Failed -> context.toast(it.message)
                is ViewState.Loading -> context.toast("Signing in")
                is ViewState.Success -> {
                    context.toast("Success")
                    navController.navigate(Screen.Notes.route)
                }
            }
        }
    }

    ScrollableColumn {
        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val (logo, title, et_username, et_password, btn_signup, txt_login) = createRefs()

            Image(
                contentDescription = "App Logo",
                bitmap = imageResource(id = noty_app_logo),
                modifier = Modifier
                    .preferredHeightIn(100.dp, 100.dp)
                    .constrainAs(logo) {
                        top.linkTo(parent.top, margin = 60.dp)
                        start.linkTo(parent.start, 16.dp)
                        end.linkTo(parent.end, 16.dp)
                    },
                contentScale = ContentScale.Inside
            )

            Text(
                text = "Welcome\nback",
                style = typography.h4,
                color = Color.Black,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(logo.bottom, margin = 30.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
            )

            val username = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(et_username) {
                        top.linkTo(title.bottom, margin = 30.dp)
                    },
                label = { Text(text = "Username") },
                leadingIcon = { Icon(Icons.Outlined.Person, "User") },
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = TextUnit.Sp(16)
                ),
                backgroundColor = MaterialTheme.colors.background,
                value = username.value,
                onValueChange = { username.value = it }
            )

            val password = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(et_password) {
                        top.linkTo(et_username.bottom, margin = 16.dp)
                    },
                label = { Text(text = "Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, "Password") },
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = TextUnit.Companion.Sp(16)
                ),
                backgroundColor = MaterialTheme.colors.background,
                value = password.value,
                onValueChange = { password.value = it }
            )

            Button(
                onClick = {
                    onLoginClicked(
                        loginViewModel = loginViewModel,
                        usernameValue = username.value,
                        passwordValue = password.value
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    .constrainAs(btn_signup) {
                        top.linkTo(et_password.bottom, margin = 40.dp)
                    },
            ) {
                Text(style = typography.subtitle1, color = Color.White, text = "Login")
            }

            Text(
                text = buildAnnotatedString {
                    // push black so entire text will be in black
                    pushStyle(SpanStyle(color = Color.Black))
                    // append new text, this text will be rendered as black
                    append("Don't have an account? Signup")
                    // then style the last added word as red, exclamation mark will be red
                    addStyle(SpanStyle(color = MaterialTheme.colors.primary), 23, this.length)
                    toAnnotatedString()
                },
                style = typography.subtitle1,
                modifier = Modifier
                    .constrainAs(txt_login) {
                        top.linkTo(btn_signup.bottom, margin = 24.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                        end.linkTo(parent.end, margin = 16.dp)
                    }
                    .clickable(
                        onClick = {
                            navController.navigate(Screen.SignUp.route)
                        }
                    )
            )
        }
    }
}

@ExperimentalCoroutinesApi
fun onLoginClicked(
    loginViewModel: LoginViewModel,
    usernameValue: TextFieldValue,
    passwordValue: TextFieldValue,
) {
    val username = usernameValue.text
    val password = passwordValue.text
    loginViewModel.login(username = username, password = password)
}
