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

package dev.shreyaspatil.noty.composeapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun CaptureBitmap(
    content: @Composable () -> Unit,
): () -> Bitmap {

    val context = LocalContext.current
    val composeView = remember { ComposeView(context) }
    var rendered by remember { mutableStateOf(false) }

    fun captureBitmap(): Bitmap {
        return composeView.drawToBitmap()
    }

    LaunchedEffect(key1 = rendered) {
        if (rendered) {
            captureBitmap()
        }
    }

    AndroidView(
        factory = {
            composeView.apply {
                setContent {
                    content.invoke()
                    rendered = true
                }
            }
        }
    )

    return ::captureBitmap
}

@Preview
@Composable
fun ComposableBitmapPreview() {
    val context = LocalContext.current
    val colorCount = remember { mutableStateOf(1) }
    val (viewBitmap, setViewBitmap) = remember { mutableStateOf<Bitmap?>(null) }

    Column {
        val captureBitmap = CaptureBitmap(
            content = {
                Button(
                    onClick = {
                        colorCount.value = colorCount.value + 1
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (colorCount.value % 2 == 0) {
                            Color.Green
                        } else {
                            Color.Red
                        }
                    )
                ) {
                    Text(text = "Pokemon")
                }
            }
        )

        Button(
            onClick = {
                MainScope().launch {
                    val bitmap = captureBitmap.invoke()
                    val uri = saveImage(bitmap, context)
                    if (uri != null) {
                        shareImageUri(context, uri)
                    } else {
                        Toast.makeText(
                            context,
                            "uri is null",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(55.dp),
        ) {
            Text(text = "Share")
        }
    }
}

suspend fun saveImage(image: Bitmap, context: Context): Uri? =
    withContext(Dispatchers.IO) {
        val imagesFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(context, "com.ch8n.fileprovider", file)
        } catch (e: IOException) {
            Log.d("Error", "IOException while trying to write file for sharing: " + e.message)
        }
        uri
    }

fun shareImageUri(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.type = "image/png"
    context.startActivity(intent)
}
