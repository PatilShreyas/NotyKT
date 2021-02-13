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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.component.dialog.LoaderDialog
import dev.shreyaspatil.noty.composeapp.ui.typography
import dev.shreyaspatil.noty.composeapp.view.Screen
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel
) {

    val viewState = viewModel.authFlow.collectAsState(initial = null).value

    when (viewState) {
        is ViewState.Loading -> LoaderDialog()
        is ViewState.Success -> {
            navController.navigate(
                route = Screen.Notes.route,
                builder = {
                    launchSingleTop = true
                    popUpTo(Screen.SignUp.route) {
                        inclusive = true
                    }
                }
            )
        }
        is ViewState.Failed -> FailureDialog(viewState.message)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            ConstraintLayout {

                val (
                    titleRef,
                    usernameRef,
                    passwordRef,
                    confirmPasswordRef,
                    buttonSignupRef,
                    textLoginRef,
                ) = createRefs()

                Text(
                    text = "Create\naccount",
                    style = typography.h4,
                    color = Color.Black,
                    modifier = Modifier.constrainAs(titleRef) {
                        top.linkTo(parent.top, margin = 60.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                )

                val username = remember { mutableStateOf(TextFieldValue()) }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(usernameRef) {
                            top.linkTo(titleRef.bottom, margin = 50.dp)
                        },
                    label = { Text(text = "Username") },
                    leadingIcon = { Icon(Icons.Outlined.Person, "Person") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = 16.sp
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
                        .constrainAs(passwordRef) {
                            top.linkTo(usernameRef.bottom, margin = 16.dp)
                        },
                    label = { Text(text = "Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Lock") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = 16.sp
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    backgroundColor = MaterialTheme.colors.background,
                    value = password.value,
                    onValueChange = { password.value = it }
                )

                val confirmPassword = remember { mutableStateOf(TextFieldValue()) }

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(confirmPasswordRef) {
                            top.linkTo(passwordRef.bottom, margin = 16.dp)
                        },
                    label = { Text(text = "Confirm password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Lock") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colors.onPrimary,
                        fontSize = 16.sp
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    backgroundColor = MaterialTheme.colors.background,
                    value = confirmPassword.value,
                    onValueChange = { confirmPassword.value = it }
                )

                Button(
                    onClick = {
                        viewModel.register(username.value.text, password.value.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                        .constrainAs(buttonSignupRef) {
                            top.linkTo(confirmPasswordRef.bottom, margin = 40.dp)
                        },
                ) {
                    Text(style = typography.subtitle1, color = Color.White, text = "Create account")
                }
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(color = Color.Black))
                        append("Already have an account? Login")
                        addStyle(SpanStyle(color = MaterialTheme.colors.primary), 24, this.length)
                    },
                    style = typography.subtitle1,
                    modifier = Modifier
                        .constrainAs(textLoginRef) {
                            top.linkTo(buttonSignupRef.bottom, margin = 24.dp)
                            start.linkTo(parent.start, margin = 16.dp)
                            end.linkTo(parent.end, margin = 16.dp)
                        }
                        .clickable(
                            onClick = {
                                navController.navigateUp()
                            }
                        )
                )
            }
        }
    }
}
