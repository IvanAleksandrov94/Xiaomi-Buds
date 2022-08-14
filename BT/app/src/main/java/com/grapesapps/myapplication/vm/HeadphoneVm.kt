package com.grapesapps.myapplication.vm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grapesapps.myapplication.bluetooth.BluetoothBatteryCommands
import com.grapesapps.myapplication.bluetooth.BluetoothSDKService
import com.grapesapps.myapplication.entity.*
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*


sealed class HeadphoneState {
    object HeadphoneStateInitial : HeadphoneState()
    object HeadphoneStateNeedConnect : HeadphoneState()
    data class HeadphoneStateLoaded(
        val isConnected: Boolean,
        val isAvailableSurround: Boolean,
        val isEnableSurround: Boolean,
        val isEnableHeadTracking: Boolean,
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

    companion object {
        private const val TAG = "HEADPHONE_VM"
    }

    private val viewState: MutableLiveData<HeadphoneState> =
        MutableLiveData(HeadphoneState.HeadphoneStateInitial)

    private val state: LiveData<HeadphoneState> = viewState

    val viewStateHeadphone: LiveData<HeadphoneState?> = viewState

    private val mBinder: MutableLiveData<BluetoothSDKService> = MutableLiveData<BluetoothSDKService>()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
            mBinder.value = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }


    fun getServiceConnection(): ServiceConnection = serviceConnection


    fun changeMainSetting(mainHeadsetValue: Int) {
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            try {
                when (mainHeadsetValue) {
                    0 -> mBinder.value?.LocalBinder()?.activateNoiseMode()
                    1 -> mBinder.value?.LocalBinder()?.activateOffMode()
                    2 -> mBinder.value?.LocalBinder()?.activateTransparencyMode()
                }
            } catch (e: Exception) {
                viewState.postValue(currentState)
            }
            viewState.postValue(currentState.copy(mainHeadsetValue = mainHeadsetValue))
        }
    }

    fun onSelectAutoSearchEar(isEnabled: Boolean) {
        try {
            if (isEnabled) {
                mBinder.value?.LocalBinder()?.activateAutoSearchEarOff()
                return
            }
            mBinder.value?.LocalBinder()?.activateAutoSearchEarOn()
        } catch (e: Exception) {
            Log.e(TAG, "onSelectAutoSearchEar Error: $e")
        }

    }

    fun onSelectAutoPhoneAnswer(isEnabled: Boolean) {
        try {
            if (isEnabled) {
                mBinder.value?.LocalBinder()?.activateAutoPhoneAnswerOff()

                return
            }
            mBinder.value?.LocalBinder()?.activateAutoPhoneAnswerOn()

        } catch (e: Exception) {
            Log.e(TAG, "onSelectAutoPhoneAnswer Error: $e")
        }
    }

    fun onChangeSurroundAudio() {
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            try {
                if (currentState.isEnableSurround) {
                    mBinder.value?.LocalBinder()?.onActivateSurroundOff()
                    viewState.postValue(currentState.copy(isEnableSurround = false))
                    return
                }
                mBinder.value?.LocalBinder()?.onActivateSurroundOn()
                viewState.postValue(currentState.copy(isEnableSurround = true))

            } catch (e: Exception) {
                Log.e(TAG, "onSelectSpectralAudio Error: $e")
            }
        }
    }

    fun onChangeHeadTracker() {
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            mBinder.value?.LocalBinder()?.onChangeHeadTrack(!currentState.isEnableHeadTracking)
            viewState.postValue(currentState.copy(isEnableHeadTracking = !currentState.isEnableHeadTracking))
        }
    }

    fun removeBond() = mBinder.value?.LocalBinder()?.removeBond()

    fun load() {
        mBinder.value?.LocalBinder()?.checkHeadsetMode()
//        mBinder.value?.LocalBinder()?.getHeadsetInfo()
    }


    fun disconnect() {
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            viewState.postValue(currentState.copy(isConnected = false))
        }
    }

    @SuppressLint("MissingPermission")
    fun updateVendorSpecific(
        device: BluetoothDevice?,
        isSupportedSurround: Boolean?,
        isEnabledSurround: Boolean?
    ) {
        Log.i("UPDATEVENDOR", "isSupportedSurround:$isSupportedSurround,isEnabledSurround:$isEnabledSurround ")
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            viewState.postValue(
                currentState.copy(
                    isAvailableSurround = isSupportedSurround!!,
                    isEnableSurround = isEnabledSurround!!,
                )
            )
        } else {
            viewState.postValue(
                HeadphoneState.HeadphoneStateLoaded(
                    isConnected = true,
                    isAvailableSurround = isSupportedSurround ?: false,
                    isEnableSurround = isEnabledSurround ?: false,
                    isEnableHeadTracking = false,
                    leftHeadsetStatus = LHeadsetBatteryStatus("-", false),
                    rightHeadsetStatus = RHeadsetBatteryStatus("-", false),
                    caseHeadsetStatus = CHeadsetBatteryStatus("-", false),
                    headsetStatus = HeadsetSettingStatus(setting = HeadsetMainSetting.Off, 0),
                    fwInfo = FirmwareInfo("-")
                )
            )
        }

    }


    @SuppressLint("MissingPermission")
    fun update(
        device: BluetoothDevice?,
        dataFromHeadset: ByteArray?
    ) {
        if (dataFromHeadset == null) {
            return
        }
        //  Log.e(TAG, "DATA FROM ${device?.name}: ${dataFromHeadset.map { it }}")
        try {
            // Status Headset
            // byteArr[6] is 0x04 --> headset mode
            // byteArr[6] is 0x0f --> battery percent
            // byteArr[6] is 0x3b --> battery charging status
            when (dataFromHeadset[6]) {
                0x04.toByte() -> {
                    // Headset Mode
                    // byteArr[10] is 0x01 --> headset mode
                    // byteArr[10] is 0x02 --> battery percent
                    // byteArr[10] is 0x03 --> battery charging status
                    when (val headsetMode = dataFromHeadset[10]) {
                        0x00.toByte() -> {
                            if (dataFromHeadset.size < 24) {
                                mBinder.value?.LocalBinder()?.checkHeadsetMode()
                                return
                            }
                            Log.i(
                                "VM Bluetooth",
                                "${headsetMode.toInt()} ОТКЛЮЧЕНО SIZE:${dataFromHeadset.size}"
                            )

                            if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                                val headsetStatus =
                                    HeadsetSettingStatus(HeadsetMainSetting.Off, dataFromHeadset[24].toInt())
                                val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
                                viewState.postValue(
                                    currentState.copy(
                                        mainHeadsetValue = 1,
                                        headsetStatus = headsetStatus,
                                    )
                                )
                            }
                        }
                        0x01.toByte() -> {
                            if (dataFromHeadset.size < 24) {
                                mBinder.value?.LocalBinder()?.checkHeadsetMode()
                                ///    btService.checkHeadsetMode()
                                return
                            }
                            Log.i(
                                "VM Bluetooth",
                                "${headsetMode.toInt()} Включен режим: ШУМОДАВ SIZE:${dataFromHeadset.size}"
                            )
                            if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                                val noiseValue = when (dataFromHeadset[24]) {
                                    0x03.toByte() -> 0
                                    0x01.toByte() -> 1
                                    0x00.toByte() -> 2
                                    0x02.toByte() -> 3
                                    else -> 0
                                }
                                val headsetStatus =
                                    HeadsetSettingStatus(HeadsetMainSetting.Noise, noiseValue)
                                val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
                                viewState.postValue(
                                    currentState.copy(
                                        mainHeadsetValue = 0,
                                        headsetStatus = headsetStatus,
                                    )
                                )
                            }
                        }
                        0x02.toByte() -> {
                            if (dataFromHeadset.size < 24) {
                                mBinder.value?.LocalBinder()?.checkHeadsetMode()
                                return
                            }
                            Log.i(
                                "VM Bluetooth",
                                "${headsetMode.toInt()} Включен режим: ПРОЗРАЧНОСТЬ SIZE:${dataFromHeadset.size}"
                            )
                            if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                                val headsetStatus =
                                    HeadsetSettingStatus(
                                        HeadsetMainSetting.Transparency,
                                        dataFromHeadset[24].toInt()
                                    )
                                val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
                                viewState.postValue(
                                    currentState.copy(
                                        mainHeadsetValue = 2,
                                        headsetStatus = headsetStatus,
                                    )
                                )
                            }
                        }
                    }
                }
                0x0f.toByte() -> {
                    Log.i("VM Bluetooth", "Size: ${dataFromHeadset.size}")
                    // Left earphone battery percent
                    val bLPercent: HeadsetBatteryStatus =
                        bytePercentConverter(dataFromHeadset[10])
                    // Right earphone battery percent
                    val bRPercent: HeadsetBatteryStatus =
                        bytePercentConverter(dataFromHeadset[11])
                    // Case battery percent
                    val bCPercent: HeadsetBatteryStatus =
                        bytePercentConverter(dataFromHeadset[12])
                    if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                        val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
                        viewState.postValue(
                            currentState.copy(
                                leftHeadsetStatus = LHeadsetBatteryStatus(bLPercent.battery, bLPercent.isCharging),
                                rightHeadsetStatus = RHeadsetBatteryStatus(bRPercent.battery, bRPercent.isCharging),
                                caseHeadsetStatus = CHeadsetBatteryStatus(bCPercent.battery, bCPercent.isCharging),
                            )
                        )
                    }
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
                        bytePercentConverter(dataFromHeadset[54])
                    // Right earphone battery charging status
                    val bRInfoPercent: HeadsetBatteryStatus =
                        bytePercentConverter(dataFromHeadset[55])
                    // Case battery charging status
                    val bCInfoPercent: HeadsetBatteryStatus =
                        bytePercentConverter(dataFromHeadset[56])
                    // Headset Firmware Version
                    val firmwareInfo = byteFirmwareVersionConverter(dataFromHeadset[31], dataFromHeadset[32])


                    if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                        val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
                        viewState.postValue(
                            currentState.copy(
                                leftHeadsetStatus = LHeadsetBatteryStatus(
                                    bLInfoPercent.battery,
                                    bLInfoPercent.isCharging
                                ),
                                rightHeadsetStatus = RHeadsetBatteryStatus(
                                    bRInfoPercent.battery,
                                    bRInfoPercent.isCharging
                                ),
                                caseHeadsetStatus = CHeadsetBatteryStatus(
                                    bCInfoPercent.battery,
                                    bCInfoPercent.isCharging
                                ),
                                fwInfo = firmwareInfo
                            )
                        )
                    } else {
                        viewState.postValue(
                            HeadphoneState.HeadphoneStateLoaded(
                                true,
                                isAvailableSurround = true,
                                isEnableSurround = false,
                                isEnableHeadTracking = false,
                                mainHeadsetValue = -1,
                                leftHeadsetStatus = LHeadsetBatteryStatus(
                                    bLInfoPercent.battery,
                                    bLInfoPercent.isCharging
                                ),
                                rightHeadsetStatus = RHeadsetBatteryStatus(
                                    bRInfoPercent.battery,
                                    bRInfoPercent.isCharging
                                ),
                                caseHeadsetStatus = CHeadsetBatteryStatus(
                                    bCInfoPercent.battery,
                                    bCInfoPercent.isCharging
                                ),
                                headsetStatus = null,
                                fwInfo = firmwareInfo,
                            )
                        )
                    }
                    mBinder.value?.LocalBinder()?.checkHeadsetMode()
                    Log.i(
                        "VM Bluetooth", "Заряд INFO: " +
                                "L: ${if (bLInfoPercent.isCharging) "🔋" else ""}${bLInfoPercent.battery} " +
                                "R: ${if (bRInfoPercent.isCharging) "🔋" else ""}${bRInfoPercent.battery} " +
                                "C: ${if (bCInfoPercent.isCharging) "🔋" else ""}${bCInfoPercent.battery} " +
                                "Firmware version : ${firmwareInfo.version}"
                    )
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
        for (i in BluetoothBatteryCommands.percentListBattery.indices) {
            if (BluetoothBatteryCommands.percentListBattery[i] == p) {
                return HeadsetBatteryStatus("${BluetoothBatteryCommands.percentList[i]}%", true)
            }
        }
        return HeadsetBatteryStatus("$p%")
    }

}