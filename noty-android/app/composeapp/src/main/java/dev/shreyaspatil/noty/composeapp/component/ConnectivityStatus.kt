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

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.ui.theme.NotyTheme
import dev.shreyaspatil.noty.composeapp.ui.theme.green
import kotlinx.coroutines.delay

@Composable
fun ConnectivityStatus(isConnected: Boolean) {
    var visibility by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visibility,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        ConnectivityStatusBox(isConnected = isConnected)
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            visibility = true
        } else {
            delay(2000)
            visibility = false
        }
    }
}

@Composable
fun ConnectivityStatusBox(isConnected: Boolean) {
    val backgroundColor by animateColorAsState(if (isConnected) green else MaterialTheme.colorScheme.errorContainer)
    val contentColor = if (isConnected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.error
    val message = if (isConnected) "Back Online!" else "No Internet Connection!"
    val iconResource =
        if (isConnected) {
            R.drawable.ic_connectivity_available
        } else {
            R.drawable.ic_connectivity_unavailable
        }

    Box(
        modifier =
            Modifier
                .background(backgroundColor)
                .fillMaxWidth()
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(id = iconResource), "Connectivity Icon", tint = contentColor)
            Spacer(modifier = Modifier.size(8.dp))
            Text(message, color = contentColor, fontSize = 15.sp)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewStatusConnectedInDark() {
    NotyTheme {
        ConnectivityStatusBox(true)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewStatusNotConnectedInDark() {
    NotyTheme {
        ConnectivityStatusBox(false)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewStatusConnectedInLight() {
    NotyTheme {
        ConnectivityStatusBox(true)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewStatusNotConnectedInLight() {
    NotyTheme {
        ConnectivityStatusBox(false)
    }
}
