package com.grapesapps.myapplication

import NavHostScreen
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.grapesapps.myapplication.bluetooth.BluetoothSDKListenerHelper
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.bluetooth.IBluetoothSDKListener
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.vm.HeadphoneVm
import com.grapesapps.myapplication.vm.Splash
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashVm by viewModels<Splash>()
    private val headphoneVm by viewModels<HeadphoneVm>()


    private val mBluetoothListener: IBluetoothSDKListener = object : IBluetoothSDKListener {

        override fun onDiscoveryStarted() {
            Log.e("IBluetoothSDKListener", "onDiscoveryStarted")
        }

        override fun onDiscoveryStopped() {
            splashVm.onEndSearchReceiver()
            Log.e("IBluetoothSDKListener", "onDiscoveryStopped")
        }

        override fun onDeviceDiscovered(device: BluetoothDevice?) {
            Log.e("IBluetoothSDKListener", "onDeviceDiscovered")

        }

//        override fun onDeviceFoundConnected(device: BluetoothDevice?, message: String) {
//            splashVm.onDeviceConnected(deviceName = message)
//        }

        override fun onDeviceConnected(device: BluetoothDevice?, message: String) {
            Log.e("IBluetoothSDKListener", "onDeviceConnected $message")
            splashVm.onDeviceConnected(deviceName = message)
        }


        override fun onMessageReceived(device: BluetoothDevice?, message: String?) {
            Log.e("IBluetoothSDKListener", "onMessageReceived: $message")
        }

        @SuppressLint("MissingPermission")
        override fun onMessageSent(device: BluetoothDevice?) {
            Log.e("IBluetoothSDKListener", "onMessageSent: ${device?.name}")
        }

        override fun onError(message: String?) {
            Log.e("IBluetoothSDKListener", "onError: $message")
        }

        override fun onDeviceDisconnected() {
            Log.e("IBluetoothSDKListener", "onDeviceDisconnected")

        }

        override fun onDeviceNotFound() {
            splashVm.onDeviceNotFound()
            Log.e("IBluetoothSDKListener", "onDeviceNotFound")
        }

        override fun onBluetoothDisabled() {
            splashVm.onBluetoothDisabled()
            Log.e("IBluetoothSDKListener", "onBluetoothDisabled")
        }

        override fun onBluetoothInitial() {
            splashVm.load()
        }

        override fun onBluetoothEnabled() {
            splashVm.onBluetoothEnabled()
            Log.e("IBluetoothSDKListener", "onBluetoothEnabled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothSDKListenerHelper.registerBluetoothSDKListener(this, mBluetoothListener)
        //pref = SharedPrefManager(this)


        val notifyIntent = Intent(this, BluetoothSDKService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "START_ACTION"
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
                    NavHostScreen(
                        splashVm = splashVm,
                        headphoneVm = headphoneVm
                    )
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
                try {
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
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((lowestBandLevel?.div(5)) ?: 0).toShort()
                                )
                            } else if (it in 600..2499) {
                                mainEqualizer.setBandLevel(
                                    index.toShort(),
                                    ((highestBandLevel?.div(5)) ?: 0).toShort()
                                )
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

                } catch (e: Exception) {
                    Log.e("BroadcastReceiver", "Equalizer Start Exception $e")
                }
            }
            if (intent.action == AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION) {
                Log.d("BroadcastReceiver", "${intent.action}")
                val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                if (sessionID == -1 || sessionID == -3) {
                    Log.e("BroadcastReceiver", "ERROR")
                    return
                }
                try {
                    val mainEqualizer = Equalizer(0, sessionID)
                    mainEqualizer.enabled = false
                    Log.d("BroadcastReceiver", "Equalizer is STOPPED")
                } catch (e: Exception) {
                    Log.e("BroadcastReceiver", "Equalizer Stop Exception $e")
                }
            }
        }
    }

//    private fun startWearableActivity() {
//        lifecycleScope.launch {
//            try {
//                val nodes = nodeClient.connectedNodes.await()
//                // Send a message to all nodes in parallel
//                nodes.map { node ->
//                    async {
//                        messageClient.sendMessage(node.id, START_ACTIVITY_PATH, byteArrayOf())
//                            .await()
//                    }
//                }.awaitAll()
//
//                Log.d("TAG", "Starting activity requests sent successfully")
//            } catch (cancellationException: CancellationException) {
//                throw cancellationException
//            } catch (exception: Exception) {
//                Log.d("TAG", "Starting activity failed: $exception")
//            }
//        }
//    }

//    private suspend fun sendCount(count: Int) {
//        try {
//            GlobalScope.launch {
//                val nodes = nodeClient.connectedNodes.await()
//
//                // Send a message to all nodes in parallel
//                nodes.map { node ->
//                    async {
//                        messageClient.sendMessage(node.id, COUNT_PATH, byteArrayOf())
//                            .await()
//                    }
//                }.awaitAll()
//            }
////            val request = PutDataMapRequest.create(COUNT_PATH).apply {
////                dataMap.putInt(COUNT_KEY, count)
////            }
////                .asPutDataRequest()
////                .setUrgent()
////
////            val result = dataClient.putDataItem(request).await()
////
////            Log.d(TAG, "DataItem saved: $result")
//        } catch (cancellationException: CancellationException) {
//            throw cancellationException
//        } catch (exception: Exception) {
//            Log.d(TAG, "Saving DataItem failed: $exception")
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btClassicReceiver)
        BluetoothSDKListenerHelper.unregisterBluetoothSDKListener(applicationContext, mBluetoothListener)
    }
}

