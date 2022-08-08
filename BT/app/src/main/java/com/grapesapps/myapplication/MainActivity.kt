package com.grapesapps.myapplication
import NavHostScreen
import android.Manifest
import android.content.*
import android.media.audiofx.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
//import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
//import com.grapesapps.myapplication.view.navigation.Navigation
import com.grapesapps.myapplication.vm.ClientDataViewModel
import com.grapesapps.myapplication.vm.Home
import com.grapesapps.myapplication.vm.Splash
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*


@AndroidEntryPoint
@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var pref: SharedPrefManager
    private val CHANNEL_NAME = "Notification Channel"
    private val CHANNEL_ID = "id=1"
    private val NOTIFICATION_ID = 0
    //  private lateinit var mService: BluetoothSDKService

    // Wear OS
    private val clientDataViewModel by viewModels<ClientDataViewModel>()
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    private val splashVm by viewModels<Splash>()
    private val headsetVm by viewModels<Home>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = SharedPrefManager(this)
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )

        Log.e("EVENTS", "${clientDataViewModel.events}")

        val notifyIntent = Intent(this, BluetoothSDKService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = CHANNEL_START_ACTION
        }
        startService(notifyIntent)

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
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,

                ),
                1
            )
        }
        val intentFilter = IntentFilter().apply {
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.EXTRA_CONTENT_TYPE)
        }
        registerReceiver(btClassicReceiver, intentFilter)

        setContent {
            BudsApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    NavHostScreen(splashVm = splashVm, headsetVm = headsetVm)
                }
            }
        }

    }
    private val btClassicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION) {
                val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                if (sessionID == -1 || sessionID == -3) {
                    Log.e("BroadcastReceiver", "ERROR")
                    return
                }
                val mainEqualizer = Equalizer(0, sessionID)

                val numberOfBands = mainEqualizer.numberOfBands
                val bands = ArrayList<Int>(0)

                val lowestBandLevel = mainEqualizer.bandLevelRange?.get(0)
                val highestBandLevel = mainEqualizer.bandLevelRange?.get(1)

                (0 until numberOfBands)
                    .map { mainEqualizer.getCenterFreq(it.toShort()) }
                    .mapTo(bands) { it.div(1000) }
                    .forEachIndexed { index, it ->
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
                    }
                mainEqualizer.enabled = false
                Log.d("BroadcastReceiver", "Equalizer is STARTED")

            }
            if (intent.action == AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION) {
                Log.d("BroadcastReceiver", "${intent.action}")
                val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                if (sessionID == -1 || sessionID == -3) {
                    Log.e("BroadcastReceiver", "ERROR")
                    return
                }
                val mainEqualizer = Equalizer(0, sessionID)
                mainEqualizer.enabled = false
                Log.d("BroadcastReceiver", "Equalizer is STOPPED")
            }
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
            GlobalScope.launch {
                val nodes = nodeClient.connectedNodes.await()

                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, COUNT_PATH, byteArrayOf())
                            .await()
                    }
                }.awaitAll()
            }
//            val request = PutDataMapRequest.create(COUNT_PATH).apply {
//                dataMap.putInt(COUNT_KEY, count)
//            }
//                .asPutDataRequest()
//                .setUrgent()
//
//            val result = dataClient.putDataItem(request).await()
//
//            Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btClassicReceiver)
        // BluetoothSDKListenerHelper.unregisterBluetoothSDKListener(applicationContext, mBluetoothListener)
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
        private const val CHANNEL_START_ACTION = "START_ACTION"

    }
}

