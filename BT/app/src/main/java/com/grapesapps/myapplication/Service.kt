//package com.grapesapps.myapplication
//
//import android.annotation.SuppressLint
//import android.app.*
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.android.gms.wearable.*
//
//class ForegroundService : Service(), DataClient.OnDataChangedListener,
//    MessageClient.OnMessageReceivedListener,
//    CapabilityClient.OnCapabilityChangedListener {
//    companion object {
//        private const val CHANNEL_ID = "wear_os_service_channel"
//        private const val CHANNEL_NAME = "WearOS service channel"
//        private const val CHANNEL_STOP_ACTION = "STOP_ACTION"
//        private const val CHANNEL_START_ACTION = "START_ACTION"
//        private const val CHANNEL_STOP_MESSAGE = "Стоп"
//        private const val NOTIFICATION_TITLE =
//            "Xiaomi Buds 3t Pro wear service is running."
//    }
//
//    private val dataClient by lazy { Wearable.getDataClient(this) }
//    private val messageClient by lazy { Wearable.getMessageClient(this) }
//    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
//    private val nodeClient by lazy { Wearable.getNodeClient(this) }
//
//
//    @SuppressLint("WrongConstant")
//    override fun onStartCommand(
//        intent: Intent?,
//        flags: Int,
//        startId: Int
//    ): Int {
//        if (intent?.action == CHANNEL_STOP_ACTION || intent?.action == null) {
//            stopForeground(true)
//            return 1
//        }
//
//        println("!@#$%!^@#^")
//
//        // Api >= 26
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                CHANNEL_NAME,
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//
//            val notificationManager =
//                getSystemService(NotificationManager::class.java)
//            notificationManager?.createNotificationChannel(serviceChannel)
//
//            val notificationIntent = Intent(this, MainActivity::class.java)
//
//            val notificationPendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                notificationIntent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//
//            val closeIntent = Intent(this, ForegroundService::class.java)
//
//            closeIntent.action = CHANNEL_STOP_ACTION
//
//            val closePendingIntent =
//                PendingIntent.getForegroundService(
//                    this,
//                    0,
//                    closeIntent,
//                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//                )
//
//            val notification =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    NotificationCompat.Builder(this, CHANNEL_ID)
//                        .setContentTitle(NOTIFICATION_TITLE)
//                        .setSmallIcon(R.drawable.ic_launcher_foreground)
//                        .setSilent(true)
//                        .setContentIntent(notificationPendingIntent)
//                        .addAction(
//                            R.drawable.ic_launcher_foreground,
//                            CHANNEL_STOP_MESSAGE,
//                            closePendingIntent
//                        )
//                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
//                        .build()
//                } else {
//                    TODO("VERSION.SDK_INT < S")
//                }
//            startForeground(1, notification)
//        }
//        return START_NOT_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onDataChanged(p0: DataEventBuffer) {
////        val a = p0.map { dataEvent ->
////            val title = when (dataEvent.type) {
////                DataEvent.TYPE_CHANGED -> R.string.data_item_changed
////                DataEvent.TYPE_DELETED -> R.string.data_item_deleted
////                else -> R.string.data_item_unknown
////            }
////
////            Event(
////                title = title,
////                text = dataEvent.dataItem.toString()
////            )
//    }
//
//
////        Log.d("DATA SERVICE", "$a")
////        TODO("Not yet implemented")
//    // }
//
//    override fun onMessageReceived(p0: MessageEvent) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onCapabilityChanged(p0: CapabilityInfo) {
//        TODO("Not yet implemented")
//    }
//}
//
