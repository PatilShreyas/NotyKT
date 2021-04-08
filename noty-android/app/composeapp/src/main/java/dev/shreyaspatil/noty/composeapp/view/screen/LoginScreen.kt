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

package dev.shreyaspatil.noty.composeapp.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import dev.shreyaspatil.noty.composeapp.R.drawable.noty_app_logo
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.component.dialog.LoaderDialog
import dev.shreyaspatil.noty.composeapp.ui.typography
import dev.shreyaspatil.noty.composeapp.view.Screen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.utils.AuthValidator
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun LoginScreen(navController: NavHostController, loginViewModel: LoginViewModel) {

    val viewState = loginViewModel.authFlow.collectAsState(initial = null).value

    when (viewState) {
        is ViewState.Loading -> LoaderDialog()
        is ViewState.Failed -> FailureDialog(viewState.message)
        is ViewState.Success -> {
            navController.navigateUp()
        }
    }

    LazyColumn {
        item {
            ConstraintLayout(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.surface)
            ) {
                val (
                    logoRef,
                    titleRef,
                    usernameRef,
                    passwordRef,
                    buttonSignupRef,
                    textLoginRef
                ) = createRefs()

                Image(
                    contentDescription = "App Logo",
                    painter = painterResource(id = noty_app_logo),
                    modifier = Modifier
                        .sizeIn(100.dp, 100.dp)
                        .constrainAs(logoRef) {
                            top.linkTo(parent.top, margin = 60.dp)
                            start.linkTo(parent.start, 16.dp)
                            end.linkTo(parent.end, 16.dp)
                        },
                    contentScale = ContentScale.Inside
                )

                Text(
                    text = "Welcome\nback",
                    style = typography.h4,
                    modifier = Modifier.constrainAs(titleRef) {
                        top.linkTo(logoRef.bottom, margin = 30.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                )
                var username by remember { mutableStateOf(TextFieldValue()) }
                val isValidUsername = AuthValidator.isValidUsername(username.text)
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(usernameRef) {
                            top.linkTo(titleRef.bottom, margin = 30.dp)
                        }.background(MaterialTheme.colors.background),
                    label = { Text(text = "Username") },
                    leadingIcon = { Icon(Icons.Outlined.Person, "User") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = 16.sp
                    ),
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    isError = !isValidUsername
                )

                var password by remember { mutableStateOf(TextFieldValue()) }
                val isValidPassword = AuthValidator.isValidPassword(password.text)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(passwordRef) {
                            top.linkTo(usernameRef.bottom, margin = 16.dp)
                        }.background(MaterialTheme.colors.background),
                    label = { Text(text = "Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Password") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = 16.sp
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    isError = !isValidPassword
                )

                Button(
                    onClick = {
                        if (isValidUsername && isValidPassword) {
                            loginViewModel.login(username.text, password.text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(buttonSignupRef) {
                            top.linkTo(passwordRef.bottom, margin = 40.dp)
                        },
                ) {
                    Text(style = typography.subtitle1, color = Color.White, text = "Login")
                }

                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account? Signup")
                        addStyle(SpanStyle(color = MaterialTheme.colors.primary), 23, this.length)
                        toAnnotatedString()
                    },
                    style = typography.subtitle1,
                    modifier = Modifier
                        .constrainAs(textLoginRef) {
                            top.linkTo(buttonSignupRef.bottom, margin = 24.dp)
                            start.linkTo(parent.start, margin = 16.dp)
                            end.linkTo(parent.end, margin = 16.dp)
                        }.clickable(
                            onClick = {
                                navController.navigate(Screen.SignUp.route)
                            }
                        )
                )
            }
        }
    }
}
