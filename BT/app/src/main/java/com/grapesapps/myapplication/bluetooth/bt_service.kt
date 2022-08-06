package com.grapesapps.myapplication.bluetooth

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.grapesapps.myapplication.MainActivity
import com.grapesapps.myapplication.R
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothSDKService : Service() {

    companion object {
        private const val CHANNEL_ID = "wear_os_service_channel"
        private const val CHANNEL_NAME = "WearOS service channel"
        private const val CHANNEL_STOP_ACTION = "STOP_ACTION"
        private const val CHANNEL_START_ACTION = "START_ACTION"
        private const val CHANNEL_STOP_MESSAGE = "Стоп"
        private const val NOTIFICATION_TITLE_CONNECTED =
            "Наушники подключены"
        private const val NOTIFICATION_TITLE_DISCONNECTED =
            "Наушники не подключены"
    }

    // Service Binder
    private val binder = LocalBinder()

    // Bluetooth stuff
    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var btHeadset: BluetoothHeadset
    private lateinit var audioManager: AudioManager
    private lateinit var pairedDevices: MutableSet<BluetoothDevice>
    private var btDevice: BluetoothDevice? = null
    private val btUuid = "0000fd2d-0000-1000-8000-00805f9b34fb"


    // Bluetooth connections
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var mAcceptThread: AcceptThread? = null

    // Invoked only first time
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Log.e("BT_SERVICE", "IS CREATE")
        btManager = applicationContext
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        btDevice = btAdapter.bondedDevices?.firstOrNull { it.name == "Xiaomi Buds 3T Pro" }
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // Invoked every service star
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == CHANNEL_STOP_ACTION || intent?.action == null) {
            stopForeground(true)
            return START_STICKY
        }
        AcceptThread().start()


        Log.e("BT_SERVICE", "IS START")

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

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothSDKService {
            return this@BluetoothSDKService
        }

        @SuppressLint("MissingPermission")
        fun startDiscovery(context: Context) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            registerReceiver(discoveryBroadcastReceiver, filter)
            btAdapter.startDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STARTED, null, null)
        }

        @SuppressLint("MissingPermission")
        fun stopDiscovery() {
            btAdapter.cancelDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STOPPED, null, null)
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(128) // mmBuffer store for the stream

        override fun run() {
            // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            var numBytes: Int

            while (true) {
                try {
                } catch (e: Exception) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_CONNECTION_ERROR,
                        null,
                        "Input stream was disconnected"
                    )
                    break
                }

                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_CONNECTION_ERROR,
                        null,
                        "Input stream was disconnected"
                    )
                    break
                }

                val message = String(mmBuffer, 0, numBytes)

                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_RECEIVED,
                    mmSocket.remoteDevice,
                    message
                )

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)

                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_SENT,
                    mmSocket.remoteDevice,
                    null
                )
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Error occurred when sending data"
                )
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Could not close the connect socket"
                )
            }
        }
    }


    /**
     * Broadcast Receiver for catching ACTION_FOUND aka new device discovered
     */
    private val discoveryBroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("discoveryBroadcastReceiver", "${intent.action}")
            if (intent.action == "android.bluetooth.device.action.ACL_CONNECTED") {
                Log.e("discoveryBroadcastReceiver", "${intent.action}")
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d("TAG", "${device?.name}")
                Log.d("TAG", "${device?.address}")
                if (device?.name == "Xiaomi Buds 3T Pro") {
                    binder.stopDiscovery()
                    btDevice = device
                    AcceptThread().start()
                }
            }
        }
    }

    private inner class AcceptThread : Thread() {
        override fun run() {
            while (true) {
                sleep(120L)
                if (btDevice != null) {
                    ConnectThread(btDevice!!).start()
                    interrupt()
                    break
                }
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val btDevice: BluetoothDevice = device

        @SuppressLint("MissingPermission")
        override fun run() {
            var y = 0
            val socket: BluetoothSocket
            try {
                socket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(btUuid))
                binder.startDiscovery(applicationContext)
                do {
                    try {
                        socket.connect()
                        pushBroadcastMessage(
                            BluetoothUtils.ACTION_DEVICE_CONNECTED,
                            socket.remoteDevice,
                            "CONNECTION SUCCESS"
                        )

                        startConnectedThread(socket)
                        break
                    } catch (e: IOException) {
                        Log.e("L", "$y")
                        if (y == 3) {
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_CONNECTION_NOT_FOUND,
                                null,
                                "CONNECTION NOT_FOUND"
                            )
                        }
                        y++
                        continue
                    }
                } while (y < 1)
                interrupt()
            } catch (e: Exception) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_NOT_FOUND,
                    null,
                    "CONNECTION NOT_FOUND"
                )
                interrupt()
            }
        }
    }

    @Synchronized
    private fun startConnectedThread(
        bluetoothSocket: BluetoothSocket?,
    ) {
        connectedThread = ConnectedThread(bluetoothSocket!!)
        connectedThread!!.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(discoveryBroadcastReceiver)
        } catch (e: Exception) {
            // already unregistered
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun pushBroadcastMessage(action: String, device: BluetoothDevice?, message: String?) {
        val intent = Intent(action)
        if (device != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DEVICE, device)
        }
        if (message != null) {
            intent.putExtra(BluetoothUtils.EXTRA_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

}