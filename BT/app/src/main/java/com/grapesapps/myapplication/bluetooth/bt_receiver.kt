package com.grapesapps.myapplication.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BluetoothSDKListenerHelper {
    companion object {

        private var mBluetoothSDKBroadcastReceiver: BluetoothSDKBroadcastReceiver? = null

        class BluetoothSDKBroadcastReceiver : BroadcastReceiver() {
            private var mGlobalListener: IBluetoothSDKListener? = null

            fun setBluetoothSDKListener(listener: IBluetoothSDKListener) {
                mGlobalListener = listener
            }

            fun removeBluetoothSDKListener(listener: IBluetoothSDKListener): Boolean {
                if (mGlobalListener == listener) {
                    mGlobalListener = null
                }

                return mGlobalListener == null
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent!!.getParcelableExtra<BluetoothDevice>(BluetoothUtils.EXTRA_DEVICE)
                val message = intent.getStringExtra(BluetoothUtils.EXTRA_MESSAGE)

                when (intent.action) {
                    BluetoothUtils.ACTION_DEVICE_FOUND -> {
                        mGlobalListener!!.onDeviceDiscovered(device)
                    }
                    BluetoothUtils.ACTION_DISCOVERY_STARTED -> {
                        mGlobalListener!!.onDiscoveryStarted()
                    }
                    BluetoothUtils.ACTION_DISCOVERY_STOPPED -> {
                        mGlobalListener!!.onDiscoveryStopped()
                    }
                    BluetoothUtils.ACTION_DEVICE_CONNECTED -> {
                        mGlobalListener!!.onDeviceConnected(device)
                    }
                    BluetoothUtils.ACTION_MESSAGE_RECEIVED -> {
                        mGlobalListener!!.onMessageReceived(device, message)
                    }
                    BluetoothUtils.ACTION_MESSAGE_SENT -> {
                        mGlobalListener!!.onMessageSent(device)
                    }
                    BluetoothUtils.ACTION_CONNECTION_ERROR -> {
                        mGlobalListener!!.onError(message)
                    }
                    BluetoothUtils.ACTION_DEVICE_DISCONNECTED -> {
                        mGlobalListener!!.onDeviceDisconnected()
                    }
                    BluetoothUtils.ACTION_CONNECTION_NOT_FOUND -> {
                        mGlobalListener!!.onDeviceNotFound()
                    }
                }
            }
        }

        fun registerBluetoothSDKListener(
            context: Context?,
            listener: IBluetoothSDKListener
        ) {
            if (mBluetoothSDKBroadcastReceiver == null) {
                mBluetoothSDKBroadcastReceiver = BluetoothSDKBroadcastReceiver()

                val intentFilter = IntentFilter().also {
                    it.addAction(BluetoothUtils.ACTION_DEVICE_FOUND)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STARTED)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STOPPED)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_CONNECTED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_RECEIVED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_SENT)
                    it.addAction(BluetoothUtils.ACTION_CONNECTION_ERROR)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_DISCONNECTED)
                    it.addAction(BluetoothUtils.ACTION_CONNECTION_NOT_FOUND)
                }


                LocalBroadcastManager.getInstance(context!!).registerReceiver(
                    mBluetoothSDKBroadcastReceiver!!, intentFilter
                )
            }

            mBluetoothSDKBroadcastReceiver!!.setBluetoothSDKListener(listener)
        }

        fun unregisterBluetoothSDKListener(
            context: Context?,
            listener: IBluetoothSDKListener
        ) {

            if (mBluetoothSDKBroadcastReceiver != null) {
                val empty = mBluetoothSDKBroadcastReceiver!!.removeBluetoothSDKListener(listener)


                if (empty) {
                    LocalBroadcastManager.getInstance(context!!)
                        .unregisterReceiver(mBluetoothSDKBroadcastReceiver!!)
                    mBluetoothSDKBroadcastReceiver = null
                }
            }
        }
    }
}