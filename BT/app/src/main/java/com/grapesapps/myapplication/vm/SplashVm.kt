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
    object SplashStateIdle : SplashState()
    object SplashBluetoothDisabled : SplashState()
    object SplashDeviceNotFound : SplashState()
    object SplashReceiverStartSearch : SplashState()
    object SplashReceiverEndSearch : SplashState()
    class SplashSuccessConnected(
        val deviceName: String
    ) : SplashState()

    object SplashSuccessNavigate : SplashState()
}

class Splash : ViewModel() {
    private val viewState: MutableLiveData<SplashState?> =
        MutableLiveData(SplashState.SplashStateInitial)

    val viewStateSplash: LiveData<SplashState?> = viewState

    private val mBinder: MutableLiveData<BluetoothSDKService> = MutableLiveData<BluetoothSDKService>()


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
            mBinder.value = binder.getService()
            if (!binder.isNotConnectedSocket()) {
                viewState.postValue(SplashState.SplashSuccessNavigate)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }


    fun getServiceConnection(): ServiceConnection = serviceConnection

    fun load() {
        viewState.postValue(SplashState.SplashStateIdle)
    }


    fun onDeviceNotFound() = viewState.postValue(SplashState.SplashDeviceNotFound)

    fun onBluetoothDisabled() = viewState.postValue(SplashState.SplashBluetoothDisabled)

    fun onBluetoothEnabled() {
        mBinder.value?.LocalBinder()?.startSearchReceiver()
//        mBinder.value?.LocalBinder()?.connectDevice()
        viewState.postValue(SplashState.SplashReceiverStartSearch)
    }


    fun onDeviceConnected(deviceName: String, deviceFounded: Boolean = false) {
//        if (deviceFounded) {
//            viewState.postValue(SplashState.SplashSuccessNavigate)
//        } else
            viewModelScope.launch(Dispatchers.IO) {
                viewState.postValue(SplashState.SplashSuccessConnected(deviceName = deviceName))
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
            val isNotExist = mBinder.value?.LocalBinder()?.isNotConnectedSocket() ?: true
            if (isNotExist) {
                viewState.postValue(SplashState.SplashReceiverEndSearch)
            }
        }
    }
}