@file:OptIn(ExperimentalComposeUiApi::class)

package com.grapesapps.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.view.navigation.Navigation
import com.grapesapps.myapplication.vm.ClientDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.Instant
import java.util.*


@AndroidEntryPoint
@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var pref: SharedPrefManager
    private val clientDataViewModel by viewModels<ClientDataViewModel>()
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }


    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var count = 0

        GlobalScope.launch {
           // lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // Set the initial trigger such that the first count will happen in one second.
                var lastTriggerTime = Instant.now() - (countInterval - Duration.ofSeconds(1))
                while (isActive) {
                    // Figure out how much time we still have to wait until our next desired trigger
                    // point. This could be less than the count interval if sending the count took
                    // some time.
                    delay(
                        Duration.between(Instant.now(), lastTriggerTime + countInterval).toMillis()
                    )
                    // Update when we are triggering sending the count
                    lastTriggerTime = Instant.now()
                    sendCount(count)

                    // Increment the count to send next time
                    count++
                }
          //  }
        }


        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )

        Log.e("EVENTS", "${clientDataViewModel.events}")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.WAKE_LOCK,
                ),
                1
            )
        }

        val btClassicReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val sessionStates = arrayOf(
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION,
                    AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION,
                    AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL,
                )

                if (sessionStates.contains(intent.action)) {
                    Log.d("BroadcastReceiver", "${intent.action}")
                    val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                    if (sessionID == -1 || sessionID == -3) {
                        Log.e("BroadcastReceiver", "ERROR")
                        return
                    }
                    val mainEqualizer = Equalizer(100, sessionID)

                    val numberOfBands = mainEqualizer.numberOfBands
                    val bands = ArrayList<Int>(0)

                    val lowestBandLevel = mainEqualizer.bandLevelRange?.get(0)
                    val highestBandLevel = mainEqualizer.bandLevelRange?.get(1)

                    (0 until numberOfBands)
                        .map { mainEqualizer.getCenterFreq(it.toShort()) }
                        .mapTo(bands) { it.div(1000) }
                        .forEachIndexed { index, it ->
                            Log.d("TAG", "Center frequency: $it Hz")
                            if (it < 100) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(1.3)) ?: 0).toShort()
                                )
                            } else if (it in 100..599) {
                                mainEqualizer.setBandLevel(index.toShort(), ((lowestBandLevel?.div(5)) ?: 0).toShort())
                            } else if (it in 600..2499) {
                                mainEqualizer.setBandLevel(index.toShort(), ((highestBandLevel?.div(5)) ?: 0).toShort())
                            } else if (it in 2500..6499) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(2.8)) ?: 0).toShort()
                                )
                            } else if (it > 6499) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(2.1)) ?: 0).toShort()
                                )
                            } else {
                                mainEqualizer.setBandLevel(index.toShort(), 0)
                            }
                            val level = mainEqualizer.getBandLevel(index.toShort())
                            Log.e("LEVEL", "CURRENT LEVEL: $level")
                        }
                    mainEqualizer.enabled = true
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.EXTRA_CONTENT_TYPE)
        }
        registerReceiver(btClassicReceiver, intentFilter)

        setContent {
            pref = SharedPrefManager(LocalContext.current)
            navController = rememberAnimatedNavController()

            Navigation(
                navController = navController,
            )
        }
    }
    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()

                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, START_ACTIVITY_PATH, byteArrayOf())
                            .await()
                    }
                }.awaitAll()

                Log.d("TAG", "Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("TAG", "Starting activity failed: $exception")
            }
        }
    }
    private suspend fun sendCount(count: Int) {
        try {
            val request = PutDataMapRequest.create(COUNT_PATH).apply {
                dataMap.putInt(COUNT_KEY, count)

            }
                .asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(request).await()

           // Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }
    companion object {
        private const val TAG = "MainActivity"

        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val COUNT_PATH = "/count"
        private const val IMAGE_PATH = "/image"
        private const val IMAGE_KEY = "photo"
        private const val TIME_KEY = "time"
        private const val COUNT_KEY = "count"
        private const val CAMERA_CAPABILITY = "camera"

        @RequiresApi(Build.VERSION_CODES.O)
        private val countInterval = Duration.ofSeconds(5)
    }
}

