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

package dev.shreyaspatil.noty.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dev.shreyaspatil.noty.core.connectivity.ConnectionState
import dev.shreyaspatil.noty.core.connectivity.ConnectivityObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityObserverImpl(
    private val connectivityManager: ConnectivityManager
) : ConnectivityObserver {

    override val connectionState: Flow<ConnectionState>
        get() = observeConnectivityAsFlow()

    override val currentConnectionState: ConnectionState
        get() = getCurrentConnectivityState()


    @ExperimentalCoroutinesApi
    fun observeConnectivityAsFlow() = callbackFlow {
        // Set current state
        val currentState = getCurrentConnectivityState()
        trySend(currentState)

        val callback = NetworkCallback { connectionState -> trySend(connectionState) }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun getCurrentConnectivityState(): ConnectionState {
        // Retrieve current status of connectivity
        val connected = connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        }

        return if (connected) ConnectionState.Available else ConnectionState.Unavailable
    }


    @Suppress("FunctionName")
    private fun NetworkCallback(
        callback: (ConnectionState) -> Unit
    ): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "NetworkCallback: onAvailable")
                callback(ConnectionState.Available)
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "NetworkCallback: onLost")
                callback(ConnectionState.Unavailable)
            }

            override fun onUnavailable() {
                Log.d(TAG, "NetworkCallback: onUnavailable")
                callback(ConnectionState.Unavailable)
            }
        }
    }

    companion object {
        private const val TAG = "ConnectivityObserver"
    }
}
