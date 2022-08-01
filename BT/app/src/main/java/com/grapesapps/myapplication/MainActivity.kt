@file:OptIn(ExperimentalComposeUiApi::class)

package com.grapesapps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.grapesapps.myapplication.model.SharedPrefManager
import com.grapesapps.myapplication.view.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.*

@AndroidEntryPoint
@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var pref: SharedPrefManager

    @OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btClassicReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {

                if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED == intent.action) {
                    Log.e("BroadcastReceiver", "${intent.action}")
                }
                if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED == intent.action) {
                    Log.e("BroadcastReceiver", "${intent.action}")
                }
                if (BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT == intent.action) {
                    Log.e("BroadcastReceiver", "${intent.action}")
                }

                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Log.d("BroadcastReceiver", "Device discovery started")
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.d("BroadcastReceiver", "Device discovery finished")
                        //  MainActivity1.myDevice?.fetchUuidsWithSdp()

                    }
                    BluetoothDevice.ACTION_UUID -> {
                        val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                        Log.d("BroadcastReceiver", "${uuidExtra?.toSet()}")
                    }

                    BluetoothDevice.ACTION_FOUND -> {
                        Log.d("BroadcastReceiver", "ACTION_FOUND")
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        Log.d("BroadcastReceiver", "${device?.name}")
                        Log.d("BroadcastReceiver", "${device?.address}")
                        if (device?.name == "Xiaomi Buds 3T Pro") {
                            //  MainActivity1.myDevice = device
                        }
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
//            addAction(BluetoothDevice.ACTION_FOUND)
//            addAction(BluetoothDevice.ACTION_UUID)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
            addAction(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY+"."+ BluetoothAssignedNumbers.PLANTRONICS)
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
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


