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

package dev.shreyaspatil.noty.composeapp.component.scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.R

/**
 * Common usable Top app bar for the project
 */
@Composable
fun NotyTopAppBar(
    title: String = "NotyKT",
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = {
            Row {
                val image = painterResource(id = R.drawable.ic_noty_logo)
                Image(painter = image, contentDescription = "Noty Icon")
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = onNavigateUp?.let {
            val navigationIcon: @Composable () -> Unit = {
                IconButton(
                    modifier = Modifier.padding(start = 4.dp),
                    onClick = onNavigateUp
                ) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        "Back",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
            navigationIcon
        },
        actions = actions,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 0.dp
    )
}
