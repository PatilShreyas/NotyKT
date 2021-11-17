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

package dev.shreyaspatil.noty.utils.ext

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class DialogComponents(
    val title: String? = null,
    val message: String? = null,
    val positiveActionText: String? = null,
    val positiveAction: (d: DialogInterface, i: Int) -> Unit = { _, _ -> },
    val negativeActionText: String? = null,
    val negativeAction: (d: DialogInterface, i: Int) -> Unit = { _, _ -> },
    val isDismissAble: Boolean = true
)

fun Fragment.showDialog(
    dialogComponents: DialogComponents
) {
    val dialog: AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogComponents.title)
            .setMessage(dialogComponents.message)
            .setPositiveButton(dialogComponents.positiveActionText) { d, i ->
                dialogComponents.positiveAction(d, i)
            }
            .setNegativeButton(dialogComponents.negativeActionText) { d, i ->
                dialogComponents.negativeAction(d, i)
            }
            .setCancelable(dialogComponents.isDismissAble)
            .create()
    dialog.show()
}
