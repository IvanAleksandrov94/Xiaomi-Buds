package com.grapesapps.myapplication


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    private val clientDataViewModel by viewModels<ClientDataViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isScreenRound = resources.configuration.isScreenRound


        setContent {
            MainApp(
                isScreenRound = isScreenRound,
                events = clientDataViewModel.events,
                onQueryNoise = ::onQueryNoise,
                onQueryTransparent = ::onQueryTransparent,
                onQueryOff = ::onQueryOff,
            )
        }
    }

    private fun onQueryNoise() {
        lifecycleScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                Log.d(TAG, "NODE SIZE ${nodes.size}")
                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, QUERY_NOISE_MODE, byteArrayOf())
                            .await()
                        Log.d(TAG, "Starting activity requests sent successfully")

                    }
                }.awaitAll()

            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }

    private fun onQueryTransparent() {
        lifecycleScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                Log.d(TAG, "NODE SIZE ${nodes.size}")
                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, QUERY_TRANSPARENT_MODE, byteArrayOf())
                            .await()
                        Log.d(TAG, "Starting activity requests sent successfully")

                    }
                }.awaitAll()

            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }


    private fun onQueryOff() {
        lifecycleScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                Log.d(TAG, "NODE SIZE ${nodes.size}")
                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, QUERY_OFF_MODE, byteArrayOf())
                            .await()
                        Log.d(TAG, "Starting activity requests sent successfully")

                    }
                }.awaitAll()

            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }


    /**
     * Collects the capabilities for all nodes that are reachable using the [CapabilityClient].
     *
     * [CapabilityClient.getAllCapabilities] returns this information as a [Map] from capabilities
     * to nodes, while this function inverts the map so we have a map of [Node]s to capabilities.
     *
     * This form is easier to work with when trying to operate upon all [Node]s.
     */
    private suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> =
        capabilityClient.getAllCapabilities(CapabilityClient.FILTER_ALL)
            .await()
            // Pair the list of all reachable nodes with their capabilities
            .flatMap { (capability, capabilityInfo) ->
                capabilityInfo.nodes.map { it to capability }
            }
            // Group the pairs by the nodes
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            // Transform the capability list for each node into a set
            .mapValues { it.value.toSet() }

    private fun displayNodes(nodes: Set<Node>) {
        val message = if (nodes.isEmpty()) {
            getString(R.string.no_device)
        } else {
            getString(R.string.connected_nodes, nodes.joinToString(", ") { it.displayName })
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val QUERY_NOISE_MODE = "/query-noise"
        private const val QUERY_TRANSPARENT_MODE = "/query-transparent"
        private const val QUERY_OFF_MODE = "/query-off"
    }
}
