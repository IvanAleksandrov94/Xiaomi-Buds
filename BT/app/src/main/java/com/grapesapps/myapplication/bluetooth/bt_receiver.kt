package com.grapesapps.myapplication.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
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

            override fun onReceive(context: Context, intent: Intent?) {
                val device =
                    intent?.getParcelableExtra<BluetoothDevice>(BluetoothUtils.EXTRA_DEVICE)
                val message = intent?.getStringExtra(BluetoothUtils.EXTRA_MESSAGE)
                val dataFromHeadset = intent?.getByteArrayExtra(BluetoothUtils.EXTRA_DATA)
                val dataIsAvailableSurround =
                    intent?.getBooleanExtra(BluetoothUtils.EXTRA_DATA_IS_AVAILABLE_SURROUND, false)
                val dataIsEnabledSurround =
                    intent?.getBooleanExtra(BluetoothUtils.EXTRA_DATA_IS_ENABLED_SURROUND, false)
                Log.e("BluetoothUtilsINTENT", "${intent?.action}")
                when (intent?.action) {
                    BluetoothUtils.ACTION_DATA_FROM_HEADPHONES -> {
                        mGlobalListener?.onDataFromHeadPhones(
                            device = device,
                            dataFromHeadset = dataFromHeadset
                        )
                    }
                    BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR -> {
                        mGlobalListener?.onDataUpdateSpecificVendor(
                            device = device,
                            isSupportedSurround = dataIsAvailableSurround,
                            isEnabledSurround = dataIsEnabledSurround,

                            )
                    }
                    BluetoothUtils.ACTION_DEVICE_FOUND -> {
                        mGlobalListener?.onDeviceDiscovered(device)
                    }
                    BluetoothUtils.ACTION_DISCOVERY_STARTED -> {
                        mGlobalListener?.onDiscoveryStarted()
                    }
                    BluetoothUtils.ACTION_DISCOVERY_STOPPED -> {
                        mGlobalListener?.onDiscoveryStopped()
                    }
                    BluetoothUtils.ACTION_DEVICE_CONNECTED -> {
                        mGlobalListener?.onDeviceConnected(device, message ?: "Успешно")
                    }
//                    BluetoothUtils.ACTION_DEVICE_FOUND_CONNECTED -> {
//                        mGlobalListener?.onDeviceFoundConnected(device, message ?: "Успешно")
//                    }
                    BluetoothUtils.ACTION_MESSAGE_RECEIVED -> {
                        mGlobalListener?.onMessageReceived(device, message)
                    }
                    BluetoothUtils.ACTION_MESSAGE_SENT -> {
                        mGlobalListener?.onMessageSent(device)
                    }
                    BluetoothUtils.ACTION_CONNECTION_ERROR -> {
                        mGlobalListener?.onError(message)
                    }
                    BluetoothUtils.ACTION_DEVICE_DISCONNECTED -> {
                        mGlobalListener?.onDeviceDisconnected()
                    }
                    BluetoothUtils.ACTION_DEVICE_NOT_FOUND -> {
                        mGlobalListener?.onDeviceNotFound()
                    }
                    BluetoothUtils.ACTION_BT_OFF -> {
                        mGlobalListener?.onBluetoothDisabled()
                    }
                    BluetoothUtils.ACTION_BT_ON -> {
                        mGlobalListener?.onBluetoothEnabled()
                    }
                    BluetoothUtils.ACTION_DEVICE_INITIAL -> {
                        mGlobalListener?.onBluetoothInitial()
                    }
                    BluetoothUtils.ACTION_REQUEST_PERMISSION -> {
                        mGlobalListener?.onRequestPermission()
                    }
                    BluetoothUtils.ACTION_REQUEST_PERMANENT_DENIED_PERMISSION -> {
                        mGlobalListener?.onRequestPermanentDeniedPermission()
                    }
                }
            }
        }

        fun registerBluetoothSDKListener(
            context: Context,
            listener: IBluetoothSDKListener
        ) {
            if (mBluetoothSDKBroadcastReceiver == null) {
                mBluetoothSDKBroadcastReceiver = BluetoothSDKBroadcastReceiver()

                val intentFilter = IntentFilter().also {
                    it.addAction(BluetoothUtils.ACTION_DATA_FROM_HEADPHONES)
                    it.addAction(BluetoothUtils.ACTION_DATA_SPECIFIC_VENDOR)
                    it.addAction(BluetoothUtils.ACTION_BT_OFF)
                    it.addAction(BluetoothUtils.ACTION_BT_ON)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_FOUND)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STARTED)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STOPPED)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_CONNECTED)
                    // it.addAction(BluetoothUtils.ACTION_DEVICE_FOUND_CONNECTED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_RECEIVED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_SENT)
                    it.addAction(BluetoothUtils.ACTION_CONNECTION_ERROR)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_DISCONNECTED)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_NOT_FOUND)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_INITIAL)
                    it.addAction(BluetoothUtils.ACTION_REQUEST_PERMISSION)
                    it.addAction(BluetoothUtils.ACTION_REQUEST_PERMANENT_DENIED_PERMISSION)

                }


                LocalBroadcastManager.getInstance(context).registerReceiver(
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