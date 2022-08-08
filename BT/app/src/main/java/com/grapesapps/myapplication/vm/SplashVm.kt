package com.grapesapps.myapplication.vm

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class SplashState {
    object SplashStateInitial : SplashState()
    object SplashBluetoothDisabled : SplashState()
    object SplashDeviceNotFound : SplashState()
    object SplashReceiverStartSearch : SplashState()
    object SplashReceiverEndSearch : SplashState()
    object SplashSuccessConnected : SplashState()
    object SplashSuccessNavigate : SplashState()
}

class Splash : ViewModel() {
    private val viewState: MutableLiveData<SplashState> =
        MutableLiveData(SplashState.SplashStateInitial)

    val viewStateSplash: LiveData<SplashState> = viewState

    private val mBinder: MutableLiveData<BluetoothSDKService> = MutableLiveData<BluetoothSDKService>()


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
            mBinder.value = binder.getService()
            if (!binder.isNotConnected()) {
                viewState.postValue(SplashState.SplashSuccessNavigate)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }


    fun getServiceConnection(): ServiceConnection = serviceConnection


    fun getCheckBluetoothStatus() = mBinder.value?.LocalBinder()?.isEnabledBluetooth() ?: false


    fun onDeviceNotFound() = viewState.postValue(SplashState.SplashDeviceNotFound)

    fun onBluetoothDisabled() = viewState.postValue(SplashState.SplashBluetoothDisabled)

    fun onBluetoothEnabled() {
        mBinder.value?.LocalBinder()?.startSearchReceiver()
        mBinder.value?.LocalBinder()?.connectDevice()
        viewState.postValue(SplashState.SplashStateInitial)
    }


    fun onDeviceConnected() {
        viewModelScope.launch(Dispatchers.IO) {
            viewState.postValue(SplashState.SplashSuccessConnected)
            delay(2000L)
            viewState.postValue(SplashState.SplashSuccessNavigate)
        }
    }


    fun startSearchReceiver() {
        mBinder.value?.LocalBinder()?.startSearchReceiver()
        viewState.postValue(SplashState.SplashReceiverStartSearch)
    }

    fun onEndSearchReceiver() {
        if (viewStateSplash.value !is SplashState.SplashSuccessConnected) {
            val isNotExist = mBinder.value?.LocalBinder()?.isNotConnected() ?: true
            if (isNotExist) {
                viewState.postValue(SplashState.SplashReceiverEndSearch)
            }
        }
    }
}