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

package dev.shreyaspatil.noty.composeapp.component

import android.graphics.Bitmap
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap

/**
 * Adds ability to capture the [Composable] component in the form of [Bitmap]
 *
 * @param captureRequestKey Unique key for capture request
 * @param onBitmapCaptured Callback providing captured [Bitmap] of a [content]
 * @param content Composable content to be captured
 */
@Composable
fun Capturable(
    captureRequestKey: Any? = null,
    onBitmapCaptured: (Bitmap) -> Unit,
    content: @Composable () -> Unit
) {
    val latestCapturedCallback by rememberUpdatedState(onBitmapCaptured)

    val context = LocalContext.current
    val view = remember { ComposeView(context) }

    AndroidView(
        factory = {
            view.apply {
                setContent {
                    Surface(color = MaterialTheme.colors.background) {
                        content()
                    }
                }
            }
        }
    )

    // If key is changed it means it's requested to capture a Bitmap
    LaunchedEffect(captureRequestKey) {
        if (captureRequestKey != null) {
            view.post {
                latestCapturedCallback(view.drawToBitmap())
            }
        }
    }
}
