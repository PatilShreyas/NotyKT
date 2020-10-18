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

package dev.shreyaspatil.noty.utils

import android.view.View
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.shreyaspatil.noty.R

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

/**
 * Set Swipe refresh layout colors
 */
fun SwipeRefreshLayout.setSchemeColors() {
    setColorSchemeColors(
        ContextCompat.getColor(context, R.color.secondaryColor),
        ContextCompat.getColor(context, R.color.onSecondary)
    )
}
