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

package dev.shreyaspatil.noty.composeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primary = Color(0xFF7885FF)

val backgroundDay = Color(0xfff3f7f9)
val backgroundNight = Color(0xff1A191E)

val surfaceDay = Color(0xffffffff)
val surfaceNight = Color(0xFF38353F)

val black = Color(0xff000000)
val white = Color(0xffffffff)

val green = Color(0xff6FCF97)
val red = Color(0xffEB5757)

@Composable
fun getTextFieldHintColor(): Color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray
