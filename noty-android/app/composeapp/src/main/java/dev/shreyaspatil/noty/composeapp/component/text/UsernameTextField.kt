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

package dev.shreyaspatil.noty.composeapp.component.text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.shreyaspatil.noty.composeapp.R

@Composable
fun UsernameTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    isError: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    NotyTextField(
        value = value,
        label = stringResource(R.string.hint_username),
        onValueChange = onValueChange,
        modifier = modifier,
        leadingIcon = { Icon(Icons.Outlined.Person, stringResource(R.string.hint_username)) },
        isError = isError,
        helperText = stringResource(R.string.message_field_username_invalid),
    )
}
