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
import android.net.Uri

object IntentUtils {
    fun launchBrowser(
        context: Context,
        url: String,
    ) = Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
        context.startActivity(it)
    }
}
