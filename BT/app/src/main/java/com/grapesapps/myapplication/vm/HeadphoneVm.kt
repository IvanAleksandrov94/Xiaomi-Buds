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
import java.io.IOException


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

        override fun onServiceDisconnected(arg0: ComponentName) {

        }

    }

    fun getServiceConnection(): ServiceConnection = serviceConnection


    fun changeMainSetting(mainHeadsetValue: Int, state: HeadphoneState.HeadphoneStateLoaded) {
        //  val isConnected = btService.isConnected()

        try {
            when (mainHeadsetValue) {
                0 -> mBinder.value?.LocalBinder()?.activateNoiseMode()
                1 -> mBinder.value?.LocalBinder()?.activateOffMode()
                2 -> mBinder.value?.LocalBinder()?.activateTransparencyMode()
            }
        } catch (e: Exception) {
            // delay(150L)
            // errorViewState.postValue("Проверьте прилегание наушников")
            viewState.postValue(state)
        }

        viewState.postValue(
            HeadphoneState.HeadphoneStateLoaded(
                true,
                mainHeadsetValue,
                state.leftHeadsetStatus,
                state.rightHeadsetStatus,
                state.caseHeadsetStatus,
                state.headsetStatus,
                state.fwInfo,
            )
        )
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

    fun onSelectSurroundAudio(isEnabled: Boolean) {
        try {
            if (isEnabled) {
                mBinder.value?.LocalBinder()?.onActivateSurroundOff()
                return
            }
            mBinder.value?.LocalBinder()?.onActivateSurroundOn()
        } catch (e: Exception) {
            Log.e(TAG, "onSelectSpectralAudio Error: $e")
        }
    }

    fun removeBond() = mBinder.value?.LocalBinder()?.removeBond()

    fun load() {
        mBinder.value?.LocalBinder()?.getHeadsetInfo()

//        viewState.postValue(
//            HeadphoneState.HeadphoneStateLoaded(
//                isConnected = false,
//                leftHeadsetStatus = LHeadsetBatteryStatus("-", false),
//                rightHeadsetStatus = RHeadsetBatteryStatus("-", false),
//                caseHeadsetStatus = CHeadsetBatteryStatus("-", false),
//                headsetStatus = HeadsetSettingStatus(setting = HeadsetMainSetting.Off, 0),
//                fwInfo = FirmwareInfo("-")
//            )
//        )
    }

    fun disconnect() {
        if (state.value is HeadphoneState.HeadphoneStateLoaded) {
            val currentState = state.value as HeadphoneState.HeadphoneStateLoaded
            viewState.postValue(
                HeadphoneState.HeadphoneStateLoaded(
                    isConnected = false,
                    leftHeadsetStatus = currentState.leftHeadsetStatus,
                    rightHeadsetStatus = currentState.rightHeadsetStatus,
                    caseHeadsetStatus = currentState.caseHeadsetStatus,
                    headsetStatus = currentState.headsetStatus,
                    fwInfo = currentState.fwInfo
                )
            )
        }
    }


    @SuppressLint("MissingPermission")
    fun update(
        device: BluetoothDevice?,
        isSupportedSurround: Boolean?,
        dataFromHeadset: ByteArray?
    ) {
        if (dataFromHeadset == null) {
            return
        }

        Log.e(TAG, "DATA FROM ${device?.name}: ${dataFromHeadset.map { it }}")
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
                                // btService.checkHeadsetMode()
                                return
                            }
                            Log.i(
                                "VM Bluetooth",
                                "${headsetMode.toInt()} ОТКЛЮЧЕНО SIZE:${dataFromHeadset.size}"
                            )

                            if (state.value is HeadphoneState.HeadphoneStateLoaded) {
                                val headsetStatus =
                                    HeadsetSettingStatus(HeadsetMainSetting.Off, dataFromHeadset[24].toInt())
                                viewState.postValue(
                                    HeadphoneState.HeadphoneStateLoaded(
                                        true,
                                        1,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).leftHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).rightHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).caseHeadsetStatus,
                                        headsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).fwInfo,
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
                                viewState.postValue(
                                    HeadphoneState.HeadphoneStateLoaded(
                                        true,
                                        0,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).leftHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).rightHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).caseHeadsetStatus,
                                        headsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).fwInfo,
                                    )
                                )
                            }
                        }
                        0x02.toByte() -> {
                            if (dataFromHeadset.size < 24) {
                                mBinder.value?.LocalBinder()?.checkHeadsetMode()
                                ///  btService.checkHeadsetMode()
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
                                viewState.postValue(
                                    HeadphoneState.HeadphoneStateLoaded(
                                        true,
                                        2,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).leftHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).rightHeadsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).caseHeadsetStatus,
                                        headsetStatus,
                                        (state.value as HeadphoneState.HeadphoneStateLoaded).fwInfo,
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
                        viewState.postValue(
                            HeadphoneState.HeadphoneStateLoaded(
                                true,
                                (state.value as HeadphoneState.HeadphoneStateLoaded).mainHeadsetValue,
                                LHeadsetBatteryStatus(bLPercent.battery, bLPercent.isCharging),
                                RHeadsetBatteryStatus(bRPercent.battery, bRPercent.isCharging),
                                CHeadsetBatteryStatus(bCPercent.battery, bCPercent.isCharging),
                                (state.value as HeadphoneState.HeadphoneStateLoaded).headsetStatus,
                                (state.value as HeadphoneState.HeadphoneStateLoaded).fwInfo,
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
                        viewState.postValue(
                            HeadphoneState.HeadphoneStateLoaded(
                                true,
                                (state.value as HeadphoneState.HeadphoneStateLoaded).mainHeadsetValue,
                                LHeadsetBatteryStatus(bLInfoPercent.battery, bLInfoPercent.isCharging),
                                RHeadsetBatteryStatus(bRInfoPercent.battery, bRInfoPercent.isCharging),
                                CHeadsetBatteryStatus(bCInfoPercent.battery, bCInfoPercent.isCharging),
                                (state.value as HeadphoneState.HeadphoneStateLoaded).headsetStatus,
                                firmwareInfo
                                // (state.value as HeadphoneState.HeadphoneStateLoaded).fwInfo,
                            )
                        )
                    } else {
                        viewState.postValue(
                            HeadphoneState.HeadphoneStateLoaded(
                                true,
                                -1,
                                LHeadsetBatteryStatus(bLInfoPercent.battery, bLInfoPercent.isCharging),
                                RHeadsetBatteryStatus(bRInfoPercent.battery, bRInfoPercent.isCharging),
                                CHeadsetBatteryStatus(bCInfoPercent.battery, bCInfoPercent.isCharging),
                                null,
                                firmwareInfo,
                            )
                        )
                    }
                    mBinder.value?.LocalBinder()?.checkHeadsetMode()
//                                btService.checkHeadsetMode()
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