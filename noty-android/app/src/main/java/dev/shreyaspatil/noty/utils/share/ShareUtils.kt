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

package dev.shreyaspatil.noty.utils.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import dev.shreyaspatil.noty.R

fun Context.shareNoteText(title: String, note: String) {
    val shareMsg = getString(R.string.text_message_share, title, note)

    val intent = ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setText(shareMsg)
        .intent

    startActivity(Intent.createChooser(intent, null))
}

fun Context.shareImage(imageUri: Uri) {
    val intent = ShareCompat.IntentBuilder(this)
        .setType("image/jpeg")
        .setStream(imageUri)
        .intent

    startActivity(Intent.createChooser(intent, null))
}
