@file:OptIn(ExperimentalComposeUiApi::class)

package com.grapesapps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFormat
import android.media.audiofx.AudioEffect
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Virtualizer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.view.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.*


@AndroidEntryPoint
@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var pref: SharedPrefManager

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



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
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,

                    ),
                1
            )
        }
        val intentFilter = IntentFilter().apply {
//            for (i in 1..1000) {
//                addAction(
//                    BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY +
//                            '.' + i
//                )
//            }
            // addAction(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY + "." + 0x038F.toString())
            addAction("android.bluetooth.headset.intent.category.companyid.911")
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            addAction(BluetoothHeadset.VENDOR_RESULT_CODE_COMMAND_ANDROID)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        }


        val btClassicReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.e("BroadcastReceiver", "${intent.action}")

                val sessionStates = arrayOf(
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION,
                    AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
                )
                if (sessionStates.contains(intent.action)) {
                    val sessionID = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
                    Log.e("BroadcastReceiver", "${sessionID}")
                    val mainV = Virtualizer(0, sessionID)
                    var rev = EnvironmentalReverb(0, sessionID)



                    rev.enabled = true


                    val canVirtualize =
                        mainV.canVirtualize(AudioFormat.CHANNEL_OUT_DEFAULT, Virtualizer.VIRTUALIZATION_MODE_BINAURAL)
                    Log.e("BroadcastReceiver", "Virtualizer canVirtualize $canVirtualize")
                    val result = mainV.getSpeakerAngles(
                        AudioFormat.CHANNEL_OUT_MONO,
                        Virtualizer.VIRTUALIZATION_MODE_BINAURAL,
                        intArrayOf(
                            -90,
                            -90,
                            -90,
                        )
                    )
                    mainV.setStrength(0)

                    val a = mainV.setEnabled(true)
                    mainV.forceVirtualizationMode(Virtualizer.VIRTUALIZATION_MODE_BINAURAL)


                    Log.e("BroadcastReceiver", "Virtualizer comlete $a is Supported $result")
                }

                when (intent.action) {
                    BluetoothHeadset.EXTRA_STATE -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        Log.e("BroadcastReceiver", "${intent.action}")
                    }
                }


//                when (intent.action) {
//                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
//                        Log.d("BroadcastReceiver", "Device discovery started")
//                    }
//                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//                        Log.d("BroadcastReceiver", "Device discovery finished")
//                        //  MainActivity1.myDevice?.fetchUuidsWithSdp()
//
//                    }
//                    BluetoothDevice.ACTION_UUID -> {
//                        val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
//                        Log.d("BroadcastReceiver", "${uuidExtra?.toSet()}")
//                    }
//
//                    BluetoothDevice.ACTION_FOUND -> {
//                        Log.d("BroadcastReceiver", "ACTION_FOUND")
//                        val device: BluetoothDevice? =
//                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                        Log.d("BroadcastReceiver", "${device?.name}")
//                        Log.d("BroadcastReceiver", "${device?.address}")
//                        if (device?.name == "Xiaomi Buds 3T Pro") {
//                            //  MainActivity1.myDevice = device
//                        }
//                    }
//                }
            }
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
}


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity1 : ComponentActivity() {
    companion object {
        private const val TAG = "BT_CONNECT"

        // 13 byte headset info and version
        private val headsetInfo = listOf(
            0xFE, 0xDC, 0xBA, 0xC1, 0x02, 0x00, 0x05,
            0x02, 0xFF, 0xFF, 0xFF, 0xFF, 0xEF
        )

        // 14 byte Disable mode
        private val arr0 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
            0x02, 0x04, 0x00, 0xef
        )

        // 12 byte ШУМ
        private val arr1 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
            0x02, 0x04, 0x01, 0xef
        )

        // 12 byte Прозрачность
        private val arr2 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x06,
            0x02, 0x04, 0x02, 0xef
        )

        private val checkHeadsetMode = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
            0x02, 0x04, 0x05, 0xef
        )

        private lateinit var bluetoothManager: BluetoothManager
        private lateinit var headphones: BluetoothDevice
        private val uuid: UUID = UUID.fromString("0000fd2d-0000-1000-8000-00805f9b34fb")
        private var myDevice: BluetoothDevice? = null
        private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    }

    private val btClassicReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "${intent.action}")

            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Device discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Device discovery finished")
                    myDevice?.fetchUuidsWithSdp()

                }
                BluetoothDevice.ACTION_UUID -> {
                    val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    Log.d(TAG, "${uuidExtra?.toSet()}")
                }

                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "ACTION_FOUND")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "${device?.name}")
                    Log.d(TAG, "${device?.address}")
                    if (device?.name == "Xiaomi Buds 3T Pro") {
                        myDevice = device
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch {
            val isConnected = BluetoothService().isConnected()
            val isConnection = BluetoothService().statusConnection
            if (isConnected && !isConnection) {
                BluetoothService().sendData(byteArrayOfInts(headsetInfo))
                BluetoothService().sendData(byteArrayOfInts(checkHeadsetMode))
            } else if (!isConnected && !isConnection) {
                BluetoothService().connectDevice(headphones, uuid)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val isConnected = BluetoothService().isConnected()
        if (!isConnected) {
            CoroutineScope(Dispatchers.IO).launch {
                BluetoothService().connectDevice(headphones, uuid)
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothManager.adapter.isDiscovering) {
            bluetoothManager.adapter.cancelDiscovery()
        }
        if (BluetoothService().isConnected()) {
            BluetoothService().disconnect()
        }
    }

    override fun onStop() {
        super.onStop()
//        if (BluetoothService.connected()) {
//            BluetoothService.disconnect()
//        }
    }


    //@OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        bluetoothManager = applicationContext
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        try {
            val device = bluetoothManager
                .adapter
                .bondedDevices
                .firstOrNull { it.name == "Xiaomi Buds 3T Pro" }

            if (device != null) {
                headphones = device
            }

//            CoroutineScope(Dispatchers.IO).launch {
//                BluetoothService.connectDevice(headphones, uuid)
//            }

//            val intentFilter = IntentFilter().apply {
//                addAction(BluetoothDevice.ACTION_FOUND)
//                addAction(BluetoothDevice.ACTION_UUID)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            }
//            if (bluetoothManager.adapter.isDiscovering) {
//                bluetoothManager.adapter.cancelDiscovery();
//            }
//            registerReceiver(btClassicReceiver, intentFilter)


        } catch (e: NullPointerException) {
            Log.e(TAG, e.message ?: "NullPointerException")

        }
        fun UUID.asBytes(): ByteArray {
            val b = ByteBuffer.wrap(ByteArray(16))
            b.putLong(mostSignificantBits)
            b.putLong(leastSignificantBits)
            return b.array()
        }
        setContent {
            //   MyApplicationTheme {
            Scaffold(
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(onClick = {


                        CoroutineScope(Dispatchers.IO).launch {
                            BluetoothService().connectDevice(headphones, uuid)
//                                val isConnected = BluetoothService.connected()
//                                val isConnection = BluetoothService.statusConnection
//                                if (isConnected && !isConnection) {
//                                    BluetoothService.sendData(byteArrayOfInts(headsetInfo))
//                                    BluetoothService.sendData(byteArrayOfInts(checkHeadsetMode))
//                                } else if (!isConnected && !isConnection) {
//                                    BluetoothService.connectDevice(headphones, uuid)
//                                }
                        }

                    }) {
                        Text("TEST")

                    }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            when (BluetoothService().isConnected()) {
                                true -> BluetoothService().sendData(byteArrayOfInts(headsetInfo))
                                false -> BluetoothService().connectDevice(headphones, uuid)
                            }
                        }
                    }) {
                        Text("Device INFO")
                    }

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                when (BluetoothService().isConnected()) {
                                    true -> BluetoothService().sendData(byteArrayOfInts(arr1))
                                    false -> BluetoothService().connectDevice(headphones, uuid)
                                }
                            }

                        }

                    ) {
                        Text("Шум")
                    }

                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            when (BluetoothService().isConnected()) {
                                true -> BluetoothService().sendData(byteArrayOfInts(arr2))
                                false -> BluetoothService().connectDevice(headphones, uuid)
                            }
                        }


                    }) {
                        Text("Прозрачность")
                    }
                    Button(onClick = {


                        CoroutineScope(Dispatchers.IO).launch {
                            when (BluetoothService().isConnected()) {
                                true -> BluetoothService().sendData(byteArrayOfInts(arr0))
                                false -> BluetoothService().connectDevice(headphones, uuid)
                            }
                        }

                    }) {
                        Text("Отключить")
                    }
                }
            }
            //  }
        }
    }
}


