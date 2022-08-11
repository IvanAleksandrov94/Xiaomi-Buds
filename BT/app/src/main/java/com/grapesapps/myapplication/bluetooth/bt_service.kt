package com.grapesapps.myapplication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.*
import com.grapesapps.myapplication.MainActivity
import com.grapesapps.myapplication.R
import com.grapesapps.myapplication.bluetooth.BluetoothBatteryCommands.percentList
import com.grapesapps.myapplication.bluetooth.BluetoothBatteryCommands.percentListBattery
import com.grapesapps.myapplication.entity.FirmwareInfo
import com.grapesapps.myapplication.entity.HeadsetBatteryStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


@AndroidEntryPoint
class BluetoothSDKService : Service(), DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    companion object {
        private const val TAG = "BT_SERVICE"
        private const val CHANNEL_ID = "wear_os_service_channel"
        private const val CHANNEL_NAME = "WearOS service channel"
        private const val CHANNEL_STOP_ACTION = "STOP_ACTION"
        private const val CHANNEL_START_ACTION = "START_ACTION"
        private const val CHANNEL_STOP_MESSAGE = "Стоп"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val QUERY_NOISE_MODE = "/query-noise"
        private const val QUERY_TRANSPARENT_MODE = "/query-transparent"
        private const val QUERY_OFF_MODE = "/query-off"
        private const val NOTIFICATION_TITLE_CONNECTED =
            "Наушники подключены"
        private const val NOTIFICATION_TITLE_DISCONNECTED =
            "Наушники не подключены"
        private const val WATCH_UPDATE_INFO = "/watch_update"

    }

    //device search
    //var isFoundedDeviceName = false

    // Service Binder
    private val binder = LocalBinder()

    // coroutine
    private val jobService = SupervisorJob()
    private val scopeService = CoroutineScope(Dispatchers.IO + jobService)

    // Bluetooth stuff
    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var audioManager: AudioManager
    private lateinit var context: Context
    private var btDevice: BluetoothDevice? = null
    private var btHeadset: BluetoothHeadset? = null
    private var btSocket: BluetoothSocket? = null
    private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    private val btUuid = "0000fd2d-0000-1000-8000-00805f9b34fb"


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        val isDenied = serviceRequestPermission()
        if (isDenied) {
            return
        }
        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        btDevice = btAdapter.bondedDevices?.firstOrNull { it.name == "Xiaomi Buds 3T Pro" }
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        context = this
        dataClient.addListener(this)
        messageClient.addListener(this)
        capabilityClient.addListener(
            this,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
        Log.e("BT_SERVICE", "IS CREATE")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            jobService.cancel()
            unregisterReceiver(discoveryBroadcastReceiver)
            unregisterReceiver(systemBluetoothStateBroadcastReceiver)
            mProfileListener.onServiceDisconnected(BluetoothProfile.HEADSET)
        } catch (e: Exception) {
        }
    }

    // Invoked every service star
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == CHANNEL_STOP_ACTION || intent?.action == null) {
            stopForeground(true)
            return START_STICKY
        }


        /// Init Headphones connect
        initConnectionDevice()

        Log.e("BT_SERVICE", "IS STARTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent(this, MainActivity::class.java)

            val notificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val closeIntent = Intent(this, BluetoothSDKService::class.java)

            closeIntent.action = CHANNEL_STOP_ACTION

            val closePendingIntent =
                PendingIntent.getForegroundService(
                    this,
                    0,
                    closeIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE_CONNECTED)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setSilent(true)
                    .setContentIntent(notificationPendingIntent)
//                    .addAction(
//                        R.drawable.ic_launcher_foreground,
//                        CHANNEL_STOP_MESSAGE,
//                        closePendingIntent
//                    )
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .build()
                startForeground(1, notification)

            } else {
                val notification: Notification = Notification.Builder(this)
                    .setContentTitle(NOTIFICATION_TITLE_CONNECTED)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setChannelId(CHANNEL_ID)
                    .build()
                startForeground(1, notification)

            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice?) {
        val isDenied = serviceRequestPermission()
        if (isDenied) {
            return
        }
        scopeService.launch(Dispatchers.IO) {
            if (device == null) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_DEVICE_NOT_FOUND,
                    null,
                    "ACTION_DEVICE_NOT_FOUND"
                )
                return@launch
            }

            var i = 0
            do {
                try {

                    btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(btUuid))
                    val isConnected = btSocket?.isConnected ?: false
                    if (isConnected) {
                        btSocket?.close()
                    }
                    IOBluetoothService(btSocket!!).connect()

                    binder.stopDiscovery()
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_DEVICE_CONNECTED,
                        device,
                        device.name
                    )
                    Log.i(TAG, "${device.name} (${device.address}) is connected")
                    break

                } catch (e: IOException) {
                    Log.e(TAG, "$${device.name} (${device.address}): BT Connect Error $e")
                }

                i++
                delay(100L)
//                if(i == 2){
//                    pushBroadcastMessage(
//                        BluetoothUtils.ACTION_DEVICE_INITIAL,
//                        null,
//                        "a"
//                    )
//                }
            } while (i < 2)
        }
    }

    private fun serviceRequestPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pushBroadcastMessage(
                BluetoothUtils.ACTION_REQUEST_PERMISSION,
                null,
                "ACTION_REQUEST_PERMISSION"
            )

            return true
        }
        return false
    }

    private fun initConnectionDevice() {
        val isDenied = serviceRequestPermission()
        if (isDenied) {
            return
        }
        scopeService.launch(Dispatchers.IO) {
            btAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET)
            binder.startDiscovery()

            if (!btAdapter.isEnabled) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_BT_OFF,
                    null,
                    "ACTION_BT_OFF"
                )
                startSystemBluetoothStateReceiver()
            }
        }
    }

    private inner class IOBluetoothService(private val socket: BluetoothSocket) {
        private val mmInStream: InputStream = socket.inputStream
        private val mmOutStream: OutputStream = socket.outputStream

        @SuppressLint("MissingPermission")
        fun connect() {

            socket.connect()
            btSocket = socket
            listenData()
        }

        fun sendData(data: ByteArray) {
            scopeService.launch(Dispatchers.IO) {
                try {
//            val parsed = data.joinToString(" ") { "%02x".format(it) }
//            Log.i("VM Bluetooth", parsed)
                    mmOutStream.write(data)
                } catch (e: IOException) {
                    if (e.message == "Broken pipe") {
                        Log.e(TAG, "${e.message}, need reconnect")
                        try {
                            connectDevice(btDevice)
                            mmOutStream.write(data)
                        } catch (e: IOException) {
                            Log.e(TAG, "${e.message}")
                        }
                    } else {
                        Log.e(TAG, "${e.message}")
                    }
                }
            }

        }


        private fun listenData() = scopeService.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val bytes = mmInStream.available()
                    if (bytes != 0) {
                        val tempBuffer = ByteArray(bytes)
                        mmInStream.read(tempBuffer, 0, bytes)

                        if (tempBuffer.size == 25 && tempBuffer[6] == 0x11.toByte()) {
                            val mutableTempBuffer: MutableList<Byte> = tempBuffer.toMutableList()
                            val parameters = gyroConvert(mutableTempBuffer)
                            if (parameters != null) audioManager.setParameters(parameters)

                            val last = mutableTempBuffer[24]

                            mutableTempBuffer[3] = 0x01.toByte()
                            mutableTempBuffer.add(6, 0x12.toByte())
                            mutableTempBuffer[7] = 0x00.toByte()
                            mutableTempBuffer[25] = last
                            /// btService.sendServiceMessage(mutableList.toByteArray())
                        } else {
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_DATA_FROM_HEADPHONES,
                                btDevice,
                                "ACTION_DATA_FROM_HEADPHONES",
                                dataFromHeadset = tempBuffer
                            )
                            sendToWatch(tempBuffer)
                            // LOGS
                            Log.i(TAG, "!!!!!!!")
                            val parsed = tempBuffer.joinToString(" ") { "%02x".format(it) }
                            //  Log.i(TAG, "${tempBuffer.map { it.toByte() }}")
                            Log.i(TAG, parsed)
                        }

                        // Status Headset
                        // byteArr[6] is 0x04 --> headset mode
                        // byteArr[6] is 0x0f --> battery percent
                        // byteArr[6] is 0x3b --> battery charging status
                        when (tempBuffer[6]) {
                            0x04.toByte() -> {
                                // Headset Mode
                                // byteArr[10] is 0x01 --> headset mode
                                // byteArr[10] is 0x02 --> battery percent
                                // byteArr[10] is 0x03 --> battery charging status
                                when (val headsetMode = tempBuffer[10]) {
                                    0x00.toByte() -> {
                                        if (tempBuffer.size < 24) {
                                            ///   btService.checkHeadsetMode()
                                            break
                                        }
                                        Log.i(
                                            "VM Bluetooth",
                                            "${headsetMode.toInt()} ОТКЛЮЧЕНО SIZE:${tempBuffer.size}"
                                        )

//                                        if (state.value is HomeState.HomeStateLoaded) {
//                                            val headsetStatus =
//                                                HeadsetSettingStatus(HeadsetMainSetting.Off, tempBuffer[24].toInt())
//                                            viewState.postValue(
//                                                HomeState.HomeStateLoaded(
//                                                    true,
//                                                    1,
//                                                    (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
//                                                    headsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).fwInfo,
//                                                )
//                                            )
//                                        }
                                    }
                                    0x01.toByte() -> {
                                        if (tempBuffer.size < 24) {
                                            ///    btService.checkHeadsetMode()
                                            break
                                        }
                                        Log.i(
                                            "VM Bluetooth",
                                            "${headsetMode.toInt()} Включен режим: ШУМОДАВ SIZE:${tempBuffer.size}"
                                        )
//                                        if (state.value is HomeState.HomeStateLoaded) {
//                                            val noiseValue = when (tempBuffer[24]) {
//                                                0x03.toByte() -> 0
//                                                0x01.toByte() -> 1
//                                                0x00.toByte() -> 2
//                                                0x02.toByte() -> 3
//                                                else -> 0
//                                            }
//                                            val headsetStatus =
//                                                HeadsetSettingStatus(HeadsetMainSetting.Noise, noiseValue)
//                                            viewState.postValue(
//                                                HomeState.HomeStateLoaded(
//                                                    true,
//                                                    0,
//                                                    (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
//                                                    headsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).fwInfo,
//                                                )
//                                            )
//                                        }
                                    }
                                    0x02.toByte() -> {
                                        if (tempBuffer.size < 24) {
                                            ///  btService.checkHeadsetMode()
                                            break
                                        }
                                        Log.i(
                                            "VM Bluetooth",
                                            "${headsetMode.toInt()} Включен режим: ПРОЗРАЧНОСТЬ SIZE:${tempBuffer.size}"
                                        )
//                                        if (state.value is HomeState.HomeStateLoaded) {
//                                            val headsetStatus =
//                                                HeadsetSettingStatus(
//                                                    HeadsetMainSetting.Transparency,
//                                                    tempBuffer[24].toInt()
//                                                )
//                                            viewState.postValue(
//                                                HomeState.HomeStateLoaded(
//                                                    true,
//                                                    2,
//                                                    (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
//                                                    headsetStatus,
//                                                    (state.value as HomeState.HomeStateLoaded).fwInfo,
//                                                )
//                                            )
//                                        }
                                    }
                                }
                            }
                            0x0f.toByte() -> {
                                Log.i("VM Bluetooth", "Size: ${tempBuffer.size}")
                                // Left earphone battery percent
                                val bLPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[10])
                                // Right earphone battery percent
                                val bRPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[11])
                                // Case battery percent
                                val bCPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[12])
//                                if (state.value is HomeState.HomeStateLoaded) {
//                                    viewState.postValue(
//                                        HomeState.HomeStateLoaded(
//                                            true,
//                                            (state.value as HomeState.HomeStateLoaded).mainHeadsetValue,
//                                            LHeadsetBatteryStatus(bLPercent.battery, bLPercent.isCharging),
//                                            RHeadsetBatteryStatus(bRPercent.battery, bRPercent.isCharging),
//                                            CHeadsetBatteryStatus(bCPercent.battery, bCPercent.isCharging),
//                                            (state.value as HomeState.HomeStateLoaded).headsetStatus,
//                                            (state.value as HomeState.HomeStateLoaded).fwInfo,
//                                        )
//                                    )
//                                }
                                Log.i(
                                    "VM Bluetooth", "Заряд INFO: " +
                                            "L: ${if (bLPercent.isCharging) "🔋" else ""}${bLPercent.battery} " +
                                            "R: ${if (bRPercent.isCharging) "🔋" else ""}${bRPercent.battery} " +
                                            "C: ${if (bCPercent.isCharging) "🔋" else ""}${bCPercent.battery}"
                                )
                            }
                            0x3b.toByte() -> {
                                // Left earphone battery charging status
                                val bLInfoPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[54])
                                // Right earphone battery charging status
                                val bRInfoPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[55])
                                // Case battery charging status
                                val bCInfoPercent: HeadsetBatteryStatus =
                                    bytePercentConverter(tempBuffer[56])
                                // Headset Firmware Version
                                val firmwareInfo = byteFirmwareVersionConverter(tempBuffer[31], tempBuffer[32])


//                                if (state.value is HomeState.HomeStateLoaded) {
//                                    viewState.postValue(
//                                        HomeState.HomeStateLoaded(
//                                            true,
//                                            (state.value as HomeState.HomeStateLoaded).mainHeadsetValue,
//                                            LHeadsetBatteryStatus(bLInfoPercent.battery, bLInfoPercent.isCharging),
//                                            RHeadsetBatteryStatus(bRInfoPercent.battery, bRInfoPercent.isCharging),
//                                            CHeadsetBatteryStatus(bCInfoPercent.battery, bCInfoPercent.isCharging),
//                                            (state.value as HomeState.HomeStateLoaded).headsetStatus,
//                                            (state.value as HomeState.HomeStateLoaded).fwInfo,
//                                        )
//                                    )
//                                } else {
//                                    viewState.postValue(
//                                        HomeState.HomeStateLoaded(
//                                            true,
//                                            -1,
//                                            LHeadsetBatteryStatus(bLInfoPercent.battery, bLInfoPercent.isCharging),
//                                            RHeadsetBatteryStatus(bRInfoPercent.battery, bRInfoPercent.isCharging),
//                                            CHeadsetBatteryStatus(bCInfoPercent.battery, bCInfoPercent.isCharging),
//                                            null,
//                                            firmwareInfo,
//                                        )
//                                    )
//                                }
//                                btService.checkHeadsetMode()
                                Log.i(
                                    "VM Bluetooth", "Заряд INFO: " +
                                            "L: ${if (bLInfoPercent.isCharging) "🔋" else ""}${bLInfoPercent.battery} " +
                                            "R: ${if (bRInfoPercent.isCharging) "🔋" else ""}${bRInfoPercent.battery} " +
                                            "C: ${if (bCInfoPercent.isCharging) "🔋" else ""}${bCInfoPercent.battery} " +
                                            "Firmware version : ${firmwareInfo.version}"
                                )
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("VM Bluetooth", "IOException: ${e.message}")
            } catch (e: IndexOutOfBoundsException) {
                Log.e("VM Bluetooth", "IndexOutOfBoundsException: ${e.message}")
                ///btService.checkHeadsetMode()
            } catch (e: Throwable) {
                Log.e("VM Bluetooth", "${e.message}")
            }

        }


    }


    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {

        /// команды


        fun activateOffMode() = send(BluetoothCommands.off)
        fun activateNoiseMode() = send(BluetoothCommands.noise)
        fun activateTransparencyMode() = send(BluetoothCommands.transparency)

        fun activateAutoSearchEarOn() = send(BluetoothCommands.autoSearchEarOn)
        fun activateAutoSearchEarOff() = send(BluetoothCommands.autoSearchEarOff)
        fun activateAutoPhoneAnswerOn() = send(BluetoothCommands.autoPhoneAnswerOn)
        fun activateAutoPhoneAnswerOff() = send(BluetoothCommands.autoPhoneAnswerOff)
        fun activateSearchLeftHeadphoneOn() = send(BluetoothCommands.searchLeftHeadphoneOn)
        fun activateSearchLeftHeadphoneOff() = send(BluetoothCommands.searchLeftHeadphoneOff)
        fun activateSearchRightHeadphoneOn() = send(BluetoothCommands.searchRightHeadphoneOn)
        fun activateSearchRightHeadphoneOff() = send(BluetoothCommands.searchRightHeadphoneOff)
        fun activateSearchAllHeadphoneOn() = send(BluetoothCommands.searchAllHeadphoneOn)
        fun activateSearchAllHeadphoneOff() = send(BluetoothCommands.searchAllHeadphoneOff)

        fun getHeadsetInfo() = send(BluetoothCommands.headsetInfo)
        fun checkHeadsetMode() = send(BluetoothCommands.checkHeadsetMode)
        fun activateHeadTest() {
            send(BluetoothCommands.startHeadTest)
            // проверка прилегания наушников
            //fe dc ba c7 f4 00 06 0a 04 00 06 01 01 ef
            //fe dc ba c7 f4 00 06 0a 04 00 06 02 02 ef
        }

        @SuppressLint("MissingPermission")
        fun onCheckSurroundStatus() {
            val result = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF010201020101FF"
            )
        }

        @SuppressLint("MissingPermission")
        fun onActivateSurroundOn() {
            val result = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF01020103020501FF"
            )
        }

        @SuppressLint("MissingPermission")
        fun onActivateSurroundOff() {
            val result = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF01020103020500FF"
            )
        }


        /// подключение

        fun getService(): BluetoothSDKService {
            return this@BluetoothSDKService
        }

        fun isNotConnectedSocket(): Boolean {
            val isConnected = btSocket?.isConnected ?: false
            return btSocket == null || !isConnected
        }

        fun startSearchReceiver() = binder.startDiscovery()

        private fun connectDevice() = connectDevice(btDevice)


        fun send(command: List<Int>) {
            try {
                IOBluetoothService(btSocket!!).sendData(byteArrayOfInts(command))
            } catch (e: NullPointerException) {
                connectDevice()
                Log.e(TAG, "btSocket is NULL")
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "ERROR")
            }
        }

        @SuppressLint("MissingPermission")
        fun startDiscovery() {
            val isDenied = serviceRequestPermission()
            if (isDenied) {
                return
            }
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            registerReceiver(discoveryBroadcastReceiver, filter)
            btAdapter.startDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STARTED, null, null)
        }

        @SuppressLint("MissingPermission")
        fun stopDiscovery() {
            val isDenied = serviceRequestPermission()
            if (isDenied) {
                return
            }
            btAdapter.cancelDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STOPPED, null, null)
        }

        fun onPermanentDenied() {
            pushBroadcastMessage(BluetoothUtils.ACTION_REQUEST_PERMANENT_DENIED_PERMISSION, null, null)
        }
    }


    /**
     * Broadcast Receiver for catching ACTION_FOUND aka new device discovered
     */
    private val discoveryBroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val isDenied = serviceRequestPermission()
            if (isDenied) {
                return
            }
            Log.e("discoveryBroadcastReceiver", "${intent.action}")
            if (intent.action == "android.bluetooth.device.action.ACL_CONNECTED") {
                Log.e("discoveryBroadcastReceiver", "${intent.action}")
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d(TAG, "${device?.name}")
                Log.d(TAG, "${device?.address}")
                if (device?.name == "Xiaomi Buds 3T Pro") {
                    //  binder.stopDiscovery()
                    btDevice = device
                    connectDevice(btDevice)
                }
            }
            if (intent.action == "android.bluetooth.device.action.ACL_DISCONNECTED") {
                if (btSocket?.isConnected == true) {
                    btSocket?.close()
                }
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_DEVICE_INITIAL,
                    null,
                    "ACTION_DEVICE_INITIAL"
                )
            }
        }
    }


    private fun startSystemBluetoothStateReceiver() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(systemBluetoothStateBroadcastReceiver, filter)
    }

    private val systemBluetoothStateBroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val isDenied = serviceRequestPermission()
            if (isDenied) {
                return
            }
            Log.e("systemBluetoothStateBroadcastReceiver", "${intent.action}")
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_BT_ON,
                        null,
                        "ACTION_BT_ON"
                    )
                    Log.e("systemBluetoothStateBroadcastReceiver", "BLUETOOTH_ENABLED")
                }
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_BT_OFF,
                        null,
                        "ACTION_BT_OFF"
                    )
                    if (btSocket?.isConnected == true) {
                        btSocket?.close()
                    }
                    Log.e("systemBluetoothStateBroadcastReceiver", "BLUETOOTH_DISABLED")
                }
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun pushBroadcastMessage(
        action: String,
        device: BluetoothDevice?,
        message: String?,
        dataFromHeadset: ByteArray? = null,
    ) {
        val intent = Intent(action)
        if (device != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DEVICE, device)
        }
        if (message != null) {
            intent.putExtra(BluetoothUtils.EXTRA_MESSAGE, message)
        }
        if (dataFromHeadset != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DATA, dataFromHeadset)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private var mProfileListener: BluetoothProfile.ServiceListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            val isDenied = serviceRequestPermission()
            if (isDenied) {
                return
            }
            if (profile == BluetoothProfile.HEADSET) {
                btHeadset = proxy as BluetoothHeadset
                val devices = btHeadset?.connectedDevices
                val device = devices?.firstOrNull { it.name == "Xiaomi Buds 3T Pro" }
                Log.e(TAG, "Bluetooth Headset: CONNECTED RESULT CONNECT $device")

                val isConnected = btSocket?.isConnected ?: false

                if (!isConnected) {
                    if (device == null) {
                        pushBroadcastMessage(
                            BluetoothUtils.ACTION_DEVICE_INITIAL,
                            null,
                            "ACTION_DEVICE_INITIAL"
                        )
                    } else {
                        connectDevice(btDevice)
                        // onCheckSurroundStatus()
                    }
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                if (btSocket?.isConnected == true) {
                    btSocket?.close()
                }
                btHeadset = null
                Log.e(TAG, "Bluetooth Headset: DISCONNECTED")
            }
        }
    }


//    @SuppressLint("MissingPermission")
//    private fun onCheckSurroundStatus() {
//        val result = btHeadset?.sendVendorSpecificResultCode(
//            btDevice,
//            "+XIAOMI",
//            "FF010201020101FF"
//        )
//
//    }

    private fun byteFirmwareVersionConverter(fw1: Byte, fw2: Byte): FirmwareInfo {
        val v = "%02x".format(fw1) + "%02x".format(fw2)
        val b = StringBuilder()
        for (i in v.toList()) {
            if (i == v.last()) {
                b.append("$i")
                break
            }
            b.append("$i.")
        }
        return FirmwareInfo(version = b.toString())
    }

    private fun bytePercentConverter(p: Byte): HeadsetBatteryStatus {
        if (p.toInt() == -1) {
            return HeadsetBatteryStatus("-")
        }
        for (i in percentListBattery.indices) {
            if (percentListBattery[i] == p) {
                return HeadsetBatteryStatus("${percentList[i]}%", true)
            }
        }
        return HeadsetBatteryStatus("$p%")
    }

    private fun gyroConvertPosition(position: String?): Float {
        if (position == null) return 0.0f
        return java.lang.Float.intBitsToFloat(position.toLong(16).toInt())
    }

    private fun gyroConvert(gyroData: MutableList<Byte>): String? {
        val yaw = gyroData.slice(12..15).map { "%02x".format(it) }.asReversed().joinToString(separator = "")
        val pitch = gyroData.slice(16..19).map { "%02x".format(it) }.asReversed().joinToString(separator = "")
        val row = gyroData.slice(20..23).map { "%02x".format(it) }.asReversed().joinToString(separator = "")

        val yawConverted = gyroConvertPosition(yaw)
        val pitchConverted = gyroConvertPosition(pitch)
        val rowConverted = gyroConvertPosition(row)

        if ((yawConverted < 360 || yawConverted > -360) || (pitchConverted < 360 || pitchConverted > -360) || (rowConverted < 360 || rowConverted > -360)) {
            // Log.e(TAG, "SEND GYRO: yaw:${gyroData.yaw}, pitch:${gyroData.pitch}, row:${gyroData.row} ")
            return "pitch=$pitchConverted;row=$rowConverted;yaw=$yawConverted"
        }

        return null
    }


    ///
    ///
    ///   WEAR OS IMPLEMENT
    ///
    ///

    private fun sendToWatch(data: ByteArray) {
        scopeService.launch(Dispatchers.IO) {
            try {
                val nodes = nodeClient.connectedNodes.await()
                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, WATCH_UPDATE_INFO, data)
                            .await()
                    }
                }.awaitAll()

                Log.d("TAG", "Requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("TAG", "Send failed: $exception")
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.e("EVENTS PHONE", "${dataEvents.map { it.dataItem }}")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.e("EVENTS", messageEvent.path)
        when (messageEvent.path) {
            START_ACTIVITY_PATH -> {
                val isRunningBluetoothService = isRunningService()
                if (!isRunningBluetoothService) {
                    val notifyIntent = Intent(this, BluetoothSDKService::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        action = "START_ACTION"
                    }
                    startService(notifyIntent)
                }
            }
            QUERY_NOISE_MODE -> binder.activateNoiseMode()
            QUERY_TRANSPARENT_MODE -> binder.activateTransparencyMode()
            QUERY_OFF_MODE -> binder.activateOffMode()
        }
    }

    private fun isRunningService(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return false
        }
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BluetoothSDKService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {

    }
}