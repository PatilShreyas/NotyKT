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

package dev.shreyaspatil.noty.composeapp.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.component.anim.LottieAnimation
import dev.shreyaspatil.noty.composeapp.ui.theme.typography

@Composable
fun LoaderDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(modifier = Modifier.size(128.dp)) {
            LottieAnimation(
                resId = R.raw.loading,
                modifier = Modifier
                    .padding(16.dp)
                    .size(100.dp)
            )
        }
    }
}

@Composable
fun FailureDialog(failureMessage: String, onDialogDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = onDialogDismiss) {
        Surface {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    resId = R.raw.failure,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(84.dp)
                )
                Text(
                    text = failureMessage,
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = onDialogDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(16.dp)

                ) {
                    Text(style = typography.subtitle1, color = Color.White, text = "OK")
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirmedYes: () -> Unit,
    onConfirmedNo: () -> Unit,
    onDismissed: () -> Unit
) {
    var isDismissed by remember { mutableStateOf(false) }

    if (!isDismissed) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth(),
            onDismissRequest = onDismissed,
            title = {
                Text(text = title)
            },
            text = {
                Text(
                    text = message,
                    fontSize = 15.sp
                )
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onConfirmedYes()
                            isDismissed = true
                        },
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Yes",
                            style = MaterialTheme.typography.button.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    TextButton(
                        onClick = {
                            onConfirmedNo()
                            isDismissed = true
                        },
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "No",
                            style = MaterialTheme.typography.button.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        )
    }
}
