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
        private const val CHANNEL_NAME = "Xiaomi Buds Service (WEAR OS)"
        private const val CHANNEL_STOP_ACTION = "STOP_ACTION"
        private const val CHANNEL_START_ACTION = "START_ACTION"
        private const val CHANNEL_STOP_MESSAGE = "Стоп"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val QUERY_NOISE_MODE = "/query-noise"
        private const val QUERY_TRANSPARENT_MODE = "/query-transparent"
        private const val QUERY_OFF_MODE = "/query-off"
        private const val NOTIFICATION_TITLE_CONNECTED =
            "Xiaomi Buds Service"
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

    private var mmInStream: InputStream? = null
    private var mmOutStream: OutputStream? = null

    private var isEnabledHeadTracker = false

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
                    .setSmallIcon(R.drawable.notification_icon_new)
                    .setSilent(true)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(LongArray(0))
                    .setContentIntent(notificationPendingIntent)
                    .setShowWhen(false)
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
                    .setSmallIcon(R.drawable.notification_icon_new)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setVibrate(LongArray(0))
                    .setChannelId(CHANNEL_ID)
                    .build()
                startForeground(1, notification)

            }
        }
        return START_STICKY
    }


    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice?, data: ByteArray?) {
        val isDenied = serviceRequestPermission()
        if (isDenied) {
            return
        }
        if (device == null) {
            pushBroadcastMessage(
                BluetoothUtils.ACTION_DEVICE_NOT_FOUND,
                null,
                "ACTION_DEVICE_NOT_FOUND"
            )
            return
        }
        scopeService.launch(Dispatchers.Main) {
            val isConnected = runProcessConnect(device, UUID.fromString(btUuid))
            if (isConnected) {
                try {
                    runProcessListenInit(data, device)
                } catch (e: Exception) {
                    Log.wtf(TAG, "WTF $e")
                }
            } else {
                var isConnectedOther: Boolean?
                val uuids = btDevice?.uuids
                if (uuids != null && uuids.isNotEmpty()) {
                    for (i in uuids.toSet()) {
                        if (i.uuid.version() == 1) {
                            isConnectedOther = runProcessConnect(device, i.uuid, 4)
                            if (isConnectedOther) {
                                try {
                                    runProcessListenInit(data, device)
                                    break
                                } catch (e: Exception) {
                                    Log.wtf(TAG, "WTF $e")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun runProcessConnect(
        device: BluetoothDevice,
        uuid: UUID,
        retry: Int = 8
    ): Boolean {
        var i = 0
        var isSecure = true
        btAdapter.cancelDiscovery()
        do {
            try {
                if (btSocket != null) {
                    btSocket?.close()
                }
                btSocket = if (isSecure) {
                    device.createRfcommSocketToServiceRecord(uuid)
                } else {
                    device.createInsecureRfcommSocketToServiceRecord(uuid)
                }
                btSocket?.connect()
                mmInStream = btSocket?.inputStream
                mmOutStream = btSocket?.outputStream
                return true
            } catch (e: IOException) {
                i++
                if (i == (retry / 2)) {
                    isSecure = false
                }
                Log.e("CONNECT", "BT Connect Error $e")
            }
        } while (i < retry)
        return false
    }

    @SuppressLint("MissingPermission")
    private fun runProcessListenInit(
        data: ByteArray?,
        device: BluetoothDevice
    ) {
        sendData(byteArrayOfInts(BluetoothCommands.headsetInfo))
        // sendData(byteArrayOfInts(BluetoothCommands.checkHeadsetMode))
        if (data != null) {
            sendData(data)
        }
        listenData()


        pushBroadcastMessage(
            BluetoothUtils.ACTION_DEVICE_CONNECTED,
            device,
            device.name
        )
    }


    private fun serviceRequestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
        } else {
            return false
        }
    }

    private fun initConnectionDevice() {
        val isDenied = serviceRequestPermission()
        if (isDenied) {
            return
        }
        scopeService.launch(Dispatchers.IO) {
            binder.startDiscovery()
            btAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET)

            if (!btAdapter.isEnabled) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_BT_OFF,
                    null,
                    "ACTION_BT_OFF"
                )
                startSystemBluetoothStateReceiver()
            }
            val isConnected = btSocket?.isConnected ?: false


            if (isConnected && btDevice != null) {
                connectDevice(btDevice, null)
            }
        }
    }

    private fun sendData(data: ByteArray) {
        scopeService.launch(Dispatchers.IO) {
            try {
                mmOutStream?.write(data)
            } catch (e: IOException) {
                if (e.message == "Broken pipe") {
                    Log.e(TAG, "${e.message}, need reconnect")
                    try {
                        connectDevice(btDevice, data)
                    } catch (e: IOException) {
                        Log.e(TAG, "${e.message}")
                    }
                } else {
                    connectDevice(btDevice, data)
                    Log.e(TAG, "${e.message}")
                }
            }
        }
    }

    private fun listenData() {
        scopeService.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val bytes = mmInStream?.available()
                    if (bytes != null && bytes != 0) {
                        val tempBuffer = ByteArray(bytes)
                        mmInStream?.read(tempBuffer, 0, bytes)
                        if (tempBuffer.size == 25 && tempBuffer[6] == 0x11.toByte()) {

                            val mutableTempBuffer: MutableList<Byte> = tempBuffer.toMutableList()

                            if (isEnabledHeadTracker) {
                                val parameters = gyroConvert(mutableTempBuffer)
                                if (parameters != null) audioManager.setParameters(parameters)
                            } else {
                                audioManager.setParameters("pitch=0.0;row=0.0;yaw=0.0")
                            }

                            val last = mutableTempBuffer[24]
                            mutableTempBuffer[3] = 0x01.toByte()
                            mutableTempBuffer.add(6, 0x12.toByte())
                            mutableTempBuffer[7] = 0x00.toByte()
                            mutableTempBuffer[25] = last
                            sendData(mutableTempBuffer.toByteArray())
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
        fun onActivateSurroundOn() {
            val result = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF01020103020501FF"
            )
            print(result)
        }


        @SuppressLint("MissingPermission")
        fun onActivateSurroundOff() {
            val result = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF01020103020500FF"
            )
            print(result)

        }

        //Разорвать пару
        fun removeBond() {
            try {
                scopeService.launch {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_DEVICE_INITIAL,
                        null,
                        "ACTION_DEVICE_INITIAL"
                    )
                    delay(500L)
                    btDevice!!::class.java.getMethod("removeBond").invoke(btDevice)
                }


            } catch (e: Exception) {
                Log.e(TAG, "Removing bond has been failed. ${e.message}")
            }
        }
        /// подключение

        fun getService(): BluetoothSDKService {
            return this@BluetoothSDKService
        }

        fun onChangeHeadTrack(isEnable: Boolean) {
            if (!isEnable) {
                try {
                    isEnabledHeadTracker = false
                    audioManager.setParameters("pitch=0.0;row=0.0;yaw=0.0")
                    return
                } catch (e: Exception) {
                }
            }
            isEnabledHeadTracker = true
        }

        fun isNotConnectedSocket(): Boolean {
            val isConnected = btSocket?.isConnected ?: false
            return btSocket == null || !isConnected
        }

        fun startSearchReceiver() = binder.startDiscovery()

        @SuppressLint("MissingPermission")
        fun onCheckSurroundStatus() {
            val isAvailable = btHeadset?.sendVendorSpecificResultCode(
                btDevice,
                "+XIAOMI",
                "FF010201020101FF"
            )
            pushBroadcastMessage(
                BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR,
                null,
                "ACTION_DATA_SPECIFIC_VENDOR",
                dataIsAvailableSurround = isAvailable,
            )
            Log.e("onCheckSurroundStatus", "$isAvailable")
        }

        private fun connectDevice(data: ByteArray?) = connectDevice(btDevice, data)


        private fun send(command: List<Int>) {
            try {
                sendData(byteArrayOfInts(command))
            } catch (e: NullPointerException) {
                connectDevice(byteArrayOfInts(command))
                // sendData(byteArrayOfInts(command))
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
            filter.addCategory(
                BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY +
                        '.' + 911
            )
            filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
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

            if (intent.action == "android.bluetooth.device.action.ACL_CONNECTED") {
                Log.e("discoveryBroadcastReceiver", "${intent.action}")
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d(TAG, "${device?.name}")
                Log.d(TAG, "${device?.address}")
                if (device?.name == "Xiaomi Buds 3T Pro") {
                    btDevice = device
                    connectDevice(btDevice, null)

                }
            }
            if (intent.action == "android.bluetooth.device.action.ACL_DISCONNECTED") {
                if (btSocket?.isConnected == true) {
                    btSocket?.close()
                }
                binder.stopDiscovery()
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_DEVICE_INITIAL,
                    null,
                    "ACTION_DEVICE_INITIAL"
                )
            }

            if (intent.action == "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT") {
                try {
                    val objArr =
                        intent.extras?.get("android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_ARGS") as Array<*>?
                    if (objArr != null) {
                        val strArr = arrayOfNulls<String>(objArr.size)
                        var str = ""
                        for (i in objArr.indices) {
                            strArr[i] = objArr[i].toString()
                            str = StringBuilder(java.lang.String.valueOf(str)).append(strArr[i]).append(" ").toString()
                        }
                        Log.i("VENDOR_SPECIFIC_HEADSET_EVENT", str)
                        if (str.trim() == "FF01020103020500FF") {
                            //  disabled surround
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR,
                                null,
                                "ACTION_DATA_SPECIFIC_VENDOR",
                                dataIsEnabledSurround = false,
                                dataIsAvailableSurround = true,
                            )

                        }
                        if (str.trim() == "FF01020103020501FF") {
                            // enabled surround
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR,
                                null,
                                "ACTION_DATA_SPECIFIC_VENDOR",
                                dataIsEnabledSurround = true,
                                dataIsAvailableSurround = true,
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.i("VENDOR_SPECIFIC_HEADSET_EVENT", "Error: $e")
                }
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
        dataIsAvailableSurround: Boolean? = null,
        dataIsEnabledSurround: Boolean? = null,
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
        if (dataIsAvailableSurround != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DATA_IS_AVAILABLE_SURROUND, dataIsAvailableSurround)
        }
        if (dataIsEnabledSurround != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DATA_IS_ENABLED_SURROUND, dataIsEnabledSurround)
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
                        connectDevice(btDevice, null)
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


    @SuppressLint("MissingPermission")
    fun onCheckSurroundStatus() {
        val isAvailable = btHeadset?.sendVendorSpecificResultCode(
            btDevice,
            "+XIAOMI",
            "FF010201020101FF"
        )
        pushBroadcastMessage(
            BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR,
            null,
            "ACTION_DATA_SPECIFIC_VENDOR",
            dataIsAvailableSurround = isAvailable,
        )
        Log.e("onCheckSurroundStatus", "$isAvailable")
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