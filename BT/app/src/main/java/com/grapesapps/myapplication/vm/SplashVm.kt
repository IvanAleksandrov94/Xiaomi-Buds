package com.grapesapps.myapplication.vm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grapesapps.myapplication.BluetoothService
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SplashState {
    object SplashRequestPermission : SplashState()
    object SplashStateInitial : SplashState()
    object SplashStateIdle : SplashState()
    object SplashBluetoothDisabled : SplashState()
    object SplashDeviceNotFound : SplashState()
    object SplashReceiverStartSearch : SplashState()
    object SplashReceiverEndSearch : SplashState()
    class SplashSuccessConnected(
        val deviceName: String
    ) : SplashState()
}

sealed class SplashStatePermission {
    object SplashStatePermissionInitial : SplashStatePermission()
    object SplashStatePermissionRequested : SplashStatePermission()
    object SplashStatePermissionDenied : SplashStatePermission()
    object SplashStatePermissionGranted : SplashStatePermission()
    object SplashStateSuccessLoaded : SplashStatePermission()
}

@HiltViewModel
class Splash @Inject constructor() : ViewModel() {
    private val viewState: MutableLiveData<SplashState?> =
        MutableLiveData(SplashState.SplashStateInitial)
    val viewStateSplash: LiveData<SplashState?> = viewState
    private val viewStateNavigate: MutableLiveData<Boolean> =
        MutableLiveData(false)
    val viewStateSplashNavigate: LiveData<Boolean> = viewStateNavigate

    private val viewStatePermission: MutableLiveData<SplashStatePermission> =
        MutableLiveData(SplashStatePermission.SplashStatePermissionInitial)
    val viewStateSplashPermission: LiveData<SplashStatePermission> = viewStatePermission


    private val mBinder: MutableLiveData<BluetoothSDKService> = MutableLiveData<BluetoothSDKService>()


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
            mBinder.value = binder.getService()
            if (!binder.isNotConnectedSocket()) {
                viewStateNavigate.postValue(true)
                //   viewState.postValue(SplashState.SplashSuccessNavigate)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }


    fun getServiceConnection(): ServiceConnection = serviceConnection

    fun load() {
        viewState.postValue(SplashState.SplashStateIdle)
        //  viewStateNavigate.postValue(false)
    }

    fun loadBeforeRequestPermission() = viewState.postValue(SplashState.SplashReceiverStartSearch)


    fun onRequestPermission() = viewState.postValue(SplashState.SplashRequestPermission)

    fun onRequestPermanentDeniedPermission (){
        mBinder.value?.LocalBinder()?.onPermanentDenied()
    }

    fun onDeviceNotFound() = viewState.postValue(SplashState.SplashDeviceNotFound)

    fun onBluetoothDisabled() = viewState.postValue(SplashState.SplashBluetoothDisabled)

    fun onBluetoothEnabled() {
        mBinder.value?.LocalBinder()?.startSearchReceiver()
//        mBinder.value?.LocalBinder()?.connectDevice()
        viewState.postValue(SplashState.SplashReceiverStartSearch)
    }


    fun onDeviceConnected(deviceName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            viewState.postValue(SplashState.SplashSuccessConnected(deviceName = deviceName))
            delay(700L)
            viewStateNavigate.postValue(true)
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

    fun onChangePermission(permission: SplashStatePermission) {
        viewStatePermission.postValue(permission)
    }
}