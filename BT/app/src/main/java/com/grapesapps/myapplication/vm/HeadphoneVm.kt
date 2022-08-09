package com.grapesapps.myapplication.vm

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.entity.*


sealed class HeadphoneState {
    object HeadphoneStateInitial : HeadphoneState()
    object HeadphoneStateNeedConnect : HeadphoneState()
    class HeadphoneStateLoaded(
        val isConnected: Boolean,
        val mainHeadsetValue: Int = -1,
        val leftHeadsetStatus: LHeadsetBatteryStatus?,
        val rightHeadsetStatus: RHeadsetBatteryStatus?,
        val caseHeadsetStatus: CHeadsetBatteryStatus?,
        val headsetStatus: HeadsetSettingStatus?,
        val fwInfo: FirmwareInfo?,
    ) : HeadphoneState()

    class HeadphoneStateError(
        val message: String?
    ) : HeadphoneState()

}

class HeadphoneVm() : ViewModel() {
    private val viewState: MutableLiveData<HeadphoneState?> =
        MutableLiveData(HeadphoneState.HeadphoneStateInitial)

    val viewStateHeadphone: LiveData<HeadphoneState?> = viewState

    private val mBinder: MutableLiveData<BluetoothSDKService> = MutableLiveData<BluetoothSDKService>()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
            mBinder.value = binder.getService()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {

        }

    }

    fun getServiceConnection(): ServiceConnection = serviceConnection


    fun load(){
       // mBinder.value?.LocalBinder().send()
    }

}