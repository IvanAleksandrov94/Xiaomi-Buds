package com.grapesapps.myapplication

import NavHostScreen
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grapesapps.myapplication.bluetooth.BluetoothSDKListenerHelper
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.bluetooth.IBluetoothSDKListener
import com.grapesapps.myapplication.ui.theme.BudsApplicationTheme
import com.grapesapps.myapplication.vm.HeadphoneVm
import com.grapesapps.myapplication.vm.Splash
import com.grapesapps.myapplication.vm.SplashStatePermission
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


        override fun onDeviceConnected(device: BluetoothDevice?, message: String) {
            Log.e("IBluetoothSDKListener", "onDeviceConnected $message")
            splashVm.onDeviceConnected(deviceName = message)
        }


        override fun onMessageReceived(device: BluetoothDevice?, message: String?) {
            Log.e("IBluetoothSDKListener", "onMessageReceived: $message")
        }

        override fun onMessageSent(device: BluetoothDevice?) {
            // Log.e("IBluetoothSDKListener", "onMessageSent: ${device?.name}")
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

        override fun onRequestPermission() {
            splashVm.onRequestPermission()
        }

        override fun onRequestPermanentDeniedPermission() {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                requestPermissionLauncher.launch(
//                    arrayOf(
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_ADMIN,
//                        Manifest.permission.BLUETOOTH,
//                        Manifest.permission.BLUETOOTH_SCAN,
//                        Manifest.permission.FOREGROUND_SERVICE,
//                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
//                        Manifest.permission.WAKE_LOCK,
//                    )
//                )
//            }
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
//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) { permissions ->
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//
//                val result = permissions
//                    .filterKeys {
//                        it == Manifest.permission.BLUETOOTH_CONNECT
//                    }
//
//                val deniedList: List<String> = result.filter {
//                    !it.value
//                }.map {
//                    it.key
//                }
//                when {
//                    deniedList.isNotEmpty() -> {
//                        val map = deniedList.groupBy { permission ->
//                            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                    this,
//                                    permission
//                                )
//                            ) "DENIED" else "EXPLAINED"
//                        }
//                        map["DENIED"]?.let {
//                            splashVm.onRequestPermission()
//                            print(it)
//                        }
//                        map["EXPLAINED"]?.let {
//                            splashVm.onRequestPermission()
//                            if (splashVm.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionRequested) {
//                                Toast.makeText(
//                                    this,
//                                    "Предоставьте разрешение к обнаружению устройств поблизости",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                                val intent = Intent(
//                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                                    Uri.fromParts("package", packageName, null)
//                                )
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                ActivityCompat.startActivityForResult(this, intent, 1, Bundle())
//                            }
//                        }
//                    }
//                    else -> {
//                        splashVm.onChangePermission(SplashStatePermission.SplashStatePermissionGranted)
//                        //splashVm.load()
//
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Предоставьте разрешение к обнаружению устройств поблизости", Toast.LENGTH_LONG)
//                    .show()
//            }
//        }

    override fun onResume() {
        super.onResume()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (splashVm.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionRequested) {
//                val isGranted = ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                )
//                if (isGranted == 0) {
//                    splashVm.onChangePermission(SplashStatePermission.SplashStatePermissionGranted)
//                    //splashVm.load()
//                } else {
//                    splashVm.onChangePermission(SplashStatePermission.SplashStatePermissionRequested)
//                }
//            }
//            if (splashVm.viewStateSplashPermission.value is SplashStatePermission.SplashStatePermissionInitial) {
//                splashVm.onChangePermission(SplashStatePermission.SplashStatePermissionRequested)
//                requestPermissionLauncher.launch(
//                    arrayOf(
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_ADMIN,
//                        Manifest.permission.BLUETOOTH,
//                        Manifest.permission.BLUETOOTH_SCAN,
//                        Manifest.permission.FOREGROUND_SERVICE,
//                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
//                        Manifest.permission.WAKE_LOCK,
//                    )
//                )
//            }
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothSDKListenerHelper.registerBluetoothSDKListener(this, mBluetoothListener)

        //pref = SharedPrefManager(this)


//        val notifyIntent = Intent(this, BluetoothSDKService::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            action = "START_ACTION"
//        }
//        startService(notifyIntent)

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

