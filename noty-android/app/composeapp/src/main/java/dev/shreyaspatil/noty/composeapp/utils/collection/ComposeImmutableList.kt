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

package dev.shreyaspatil.noty.composeapp.utils.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import java.util.Collections

/**
 * Creates a immutable [List] which can be used in [@Composable] functions safely by inferring
 * the stability of a list
 */
@Immutable
data class ComposeImmutableList<E> private constructor(
    private val baseList: List<E>,
) : List<E> by baseList {
    companion object {
        /**
         * Creates [ComposeImmutableList] from [baseList].
         */
        fun <E> from(baseList: List<E>): ComposeImmutableList<E> {
            return ComposeImmutableList(Collections.unmodifiableList(ArrayList(baseList)))
        }
    }
}

/**
 * Creates a new [ComposeImmutableList] from this [Iterable]
 */
fun <E> Iterable<E>.toComposeImmutableList(): ComposeImmutableList<E> {
    val list = if (this is List<E>) this else toList()
    return ComposeImmutableList.from(list)
}

/**
 * Creates a new [ComposeImmutableList] from [items]
 */
fun <E> composeImmutableListOf(vararg items: E): ComposeImmutableList<E> =
    items.toList().toComposeImmutableList()

/**
 * Derives a state of [ComposeImmutableList] from [baseList] computation.
 */
@Composable
inline fun <E> rememberComposeImmutableList(
    crossinline baseList: @DisallowComposableCalls () -> Iterable<E>,
): State<ComposeImmutableList<E>> {
    return remember { derivedStateOf { baseList().toComposeImmutableList() } }
}
