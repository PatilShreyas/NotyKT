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

package dev.shreyaspatil.noty.composeapp.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.component.NotyIcon
import dev.shreyaspatil.noty.composeapp.component.button.NotyFullWidthButton
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.component.dialog.LoaderDialog
import dev.shreyaspatil.noty.composeapp.component.text.PasswordTextField
import dev.shreyaspatil.noty.composeapp.component.text.UsernameTextField
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToNotes: () -> Unit,
) {
    val state by viewModel.collectState()

    LoginContent(
        isLoading = state.isLoading,
        username = state.username,
        password = state.password,
        isValidUsername = state.isValidUsername ?: true,
        isValidPassword = state.isValidPassword ?: true,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onLoginClick = viewModel::login,
        onSignupClick = onNavigateToSignup,
        onDialogDismiss = viewModel::clearError,
        error = state.error,
    )

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToNotes()
        }
    }
}

@Composable
fun LoginContent(
    isLoading: Boolean,
    username: String,
    isValidUsername: Boolean,
    password: String,
    isValidPassword: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onDialogDismiss: () -> Unit,
    error: String?,
) {
    if (isLoading) {
        LoaderDialog()
    }

    if (error != null) {
        FailureDialog(error, onDialogDismiss = onDialogDismiss)
    }

    Scaffold {
        Column(
            modifier =
                Modifier
                    .consumeWindowInsets(it)
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.ime)
                    .verticalScroll(rememberScrollState())
        ) {
            TopGreeting()

            LoginForm(
                username = username,
                isValidUsername = isValidUsername,
                onUsernameChange = onUsernameChange,
                password = password,
                isValidPassword = isValidPassword,
                onPasswordChange = onPasswordChange,
                onLoginClick = onLoginClick,
            )

            SignUpLink(Modifier.align(Alignment.CenterHorizontally), onSignupClick = onSignupClick)
        }
    }
}

@Composable
private fun TopGreeting() {
    Column(Modifier.fillMaxWidth()) {
        NotyIcon(
            Modifier
                .padding(top = 60.dp)
                .requiredSize(92.dp)
                .align(Alignment.CenterHorizontally),
        )

        Text(
            text = "Welcome\nback",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp),
        )
    }
}

@Composable
private fun LoginForm(
    username: String,
    isValidUsername: Boolean,
    password: String,
    isValidPassword: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    UsernameTextField(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.background),
        value = username,
        onValueChange = onUsernameChange,
        isError = !isValidUsername,
    )

    PasswordTextField(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.background),
        value = password,
        onValueChange = onPasswordChange,
        isError = !isValidPassword,
    )

    NotyFullWidthButton(
        text = "Login",
        onClick = onLoginClick,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
    )
}

@Composable
private fun SignUpLink(
    modifier: Modifier,
    onSignupClick: () -> Unit,
) {
    Text(
        text =
            buildAnnotatedString {
                append("Don't have an account? Signup")
                addStyle(SpanStyle(color = MaterialTheme.colorScheme.primary), 23, this.length)
                toAnnotatedString()
            },
        style = MaterialTheme.typography.titleMedium,
        modifier =
            modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .clickable(onClick = onSignupClick),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginContentInDark() =
    NotyPreview {
        FakeLoginContent()
    }

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewLoginContentInLight() =
    NotyPreview {
        FakeLoginContent()
    }

@Composable
private fun FakeLoginContent() {
    LoginContent(
        isLoading = false,
        username = "johndoe",
        onUsernameChange = {},
        password = "password",
        onPasswordChange = {},
        onLoginClick = {},
        onSignupClick = {},
        isValidPassword = false,
        isValidUsername = false,
        onDialogDismiss = {},
        error = null,
    )
}
