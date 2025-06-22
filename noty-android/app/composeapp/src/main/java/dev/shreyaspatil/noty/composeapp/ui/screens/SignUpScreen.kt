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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.component.dialog.FailureDialog
import dev.shreyaspatil.noty.composeapp.component.dialog.LoaderDialog
import dev.shreyaspatil.noty.composeapp.component.text.PasswordTextField
import dev.shreyaspatil.noty.composeapp.component.text.UsernameTextField
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview
import dev.shreyaspatil.noty.composeapp.utils.collectState
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel

@Composable
fun SignUpScreen(
    viewModel: RegisterViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToNotes: () -> Unit,
) {
    val state by viewModel.collectState()

    SignUpContent(
        isLoading = state.isLoading,
        username = state.username,
        password = state.password,
        confirmPassword = state.confirmPassword,
        isValidUsername = state.isValidUsername ?: true,
        isValidPassword = state.isValidPassword ?: true,
        isValidConfirmPassword = state.isValidConfirmPassword ?: true,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onConfirmPasswordChanged = viewModel::setConfirmPassword,
        onSignUpClick = viewModel::register,
        onDialogDismiss = viewModel::clearError,
        onNavigateUp = onNavigateUp,
        error = state.error,
    )

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToNotes()
        }
    }
}

@Composable
fun SignUpContent(
    isLoading: Boolean,
    username: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    password: String,
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    isValidConfirmPassword: Boolean,
    onNavigateUp: () -> Unit,
    onSignUpClick: () -> Unit,
    onDialogDismiss: () -> Unit,
    isValidUsername: Boolean,
    isValidPassword: Boolean,
    error: String?,
) {
    if (isLoading) {
        LoaderDialog()
    }

    if (error != null) {
        FailureDialog(error, onDialogDismiss = onDialogDismiss)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.text_registration),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 60.dp, bottom = 16.dp),
        )

        SignUpForm(
            username = username,
            onUsernameChange = onUsernameChange,
            isValidUsername = isValidUsername,
            password = password,
            onPasswordChange = onPasswordChange,
            isValidPassword = isValidPassword,
            confirmPassword = confirmPassword,
            onConfirmPasswordChanged = onConfirmPasswordChanged,
            isValidConfirmPassword = isValidConfirmPassword,
            onSignUpClick = onSignUpClick,
        )

        LoginLink(Modifier.align(Alignment.CenterHorizontally), onLoginClick = onNavigateUp)
    }
}

@Composable
private fun SignUpForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    isValidUsername: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    isValidPassword: Boolean,
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    isValidConfirmPassword: Boolean,
    onSignUpClick: () -> Unit,
) {
    Column(
        Modifier.padding(
            start = 16.dp,
            top = 32.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
    ) {
        UsernameTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.background),
            value = username,
            onValueChange = onUsernameChange,
            isError = !isValidUsername,
        )

        PasswordTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.background),
            value = password,
            onValueChange = onPasswordChange,
            isError = !isValidPassword,
        )

        PasswordTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.background),
            value = confirmPassword,
            label = stringResource(R.string.hint_confirm_password),
            onValueChange = onConfirmPasswordChanged,
            isError = !isValidConfirmPassword,
        )

        Button(
            onClick = onSignUpClick,
            modifier =
                Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .height(60.dp),
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                text = stringResource(R.string.text_registration),
            )
        }
    }
}

@Composable
private fun LoginLink(
    modifier: Modifier,
    onLoginClick: () -> Unit,
) {
    Text(
        text =
            buildAnnotatedString {
                val loginAccountText = stringResource(R.string.text_login_account)
                append(loginAccountText)
                addStyle(SpanStyle(color = MaterialTheme.colorScheme.primary), 24, this.length)
            },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .clickable(onClick = onLoginClick),
    )
}

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewSignupContent() =
    NotyPreview {
        SignUpContent(
            isLoading = false,
            username = "johndoe",
            onUsernameChange = {},
            onPasswordChange = {},
            password = "password",
            confirmPassword = "password",
            onConfirmPasswordChanged = {},
            isValidConfirmPassword = false,
            onNavigateUp = {},
            onSignUpClick = {},
            onDialogDismiss = {},
            isValidUsername = false,
            isValidPassword = false,
            error = null,
        )
    }
