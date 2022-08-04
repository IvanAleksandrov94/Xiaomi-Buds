package com.grapesapps.myapplication.vm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grapesapps.myapplication.BluetoothService
import com.grapesapps.myapplication.entity.*
import com.grapesapps.myapplication.model.DaggerRepositoryComponent
import com.grapesapps.myapplication.model.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject


sealed class HomeState {
    object HomeStateInitial : HomeState()
    object HomeStateNeedConnect : HomeState()
    class HomeStateLoaded(
        val isConnected: Boolean,
        val mainHeadsetValue: Int = -1,
        val leftHeadsetStatus: LHeadsetBatteryStatus?,
        val rightHeadsetStatus: RHeadsetBatteryStatus?,
        val caseHeadsetStatus: CHeadsetBatteryStatus?,
        val headsetStatus: HeadsetSettingStatus?,
        val fwInfo: FirmwareInfo?,
    ) : HomeState()

    class HomeStateError(
        val message: String?
    ) : HomeState()

}

@HiltViewModel
class Home @Inject constructor(
    @ApplicationContext appContext: Context,
    private val btService: BluetoothService,
) : ViewModel() {
    private val viewState: MutableLiveData<HomeState> = MutableLiveData(HomeState.HomeStateInitial)
    private val errorViewState: MutableLiveData<String?> = MutableLiveData()
    val state: LiveData<HomeState> = viewState
    val errorState: LiveData<String?> = errorViewState
    private val sharedPrefManager: SharedPrefManager
    private val uuid: UUID = UUID.fromString("0000fd2d-0000-1000-8000-00805f9b34fb")
    private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    private lateinit var inputStream: InputStream
    private lateinit var audioManager: AudioManager


    // in Service
    private val percentList = listOf(
        0x00, 0x05, 0x0a, 0x0f, 0x14, 0x19, 0x1e, 0x23, 0x28,
        0x2d, 0x32, 0x37, 0x3c, 0x41, 0x46, 0x4b, 0x50, 0x55,
        0x5a, 0x5f, 0x64
    )

    // in Service
    private val percentListBattery = listOf<Byte>(
        -128, -123, -118, -113, -108, -103, -98, -93, -88,
        -83, -78, -73, -68, -63, -58, -53, -48, -43,
        -38, -33, -28
    )


    init {
        DaggerRepositoryComponent.create().injectHome(this)
        sharedPrefManager = SharedPrefManager(appContext)
    }

    fun searchDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            viewState.value = (HomeState.HomeStateNeedConnect)
        }
    }


    @SuppressLint("MissingPermission")
    fun connectDevice(btDevice: BluetoothDevice, audio: AudioManager) {
        val isConnected = btService.isConnected()
        val isConnection = btService.statusConnection
        if (isConnected && !isConnection) {
            viewModelScope.launch(Dispatchers.IO) {
                btService.connectDevice(btDevice, uuid)
                inputStream = btService.inputStream
                btService.getHeadsetInfo()
                listenData()
                audioManager = audio
            }
        } else if (!isConnected && !isConnection) {
            viewModelScope.launch(Dispatchers.IO) {
                btService.connectDevice(btDevice, uuid)
                inputStream = btService.inputStream
                btService.getHeadsetInfo()
                listenData()
                audioManager = audio
            }

        }
    }

    fun onSelectSpectralAudio() {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {


        }
    }

    fun onSelectAutoSearchEar() {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {
            if (isConnected) {
                btService.activateAutoSearchEarOn()
            }

        }
    }

    fun onSelectAutoPhoneAnswer() {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {
            if (isConnected) {
                btService.activateAutoPhoneAnswerOn()
            }

        }
    }

    fun onStartHeadTest() {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {
            if (isConnected) {

                btService.activateHeadTest()
            }

        }
    }

    fun changeMainSetting(mainHeadsetValue: Int, state: HomeState.HomeStateLoaded) {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (mainHeadsetValue) {
                    0 -> btService.activateNoiseMode()
                    1 -> btService.activateOffMode()
                    2 -> btService.activateTransparencyMode()
                }
                inputStream = btService.inputStream
                listenData()
            } catch (e: Exception) {
                delay(150L)
                errorViewState.postValue("Проверьте прилегание наушников")
                viewState.postValue(state)
            }
        }
        viewState.postValue(
            HomeState.HomeStateLoaded(
                isConnected,
                mainHeadsetValue,
                state.leftHeadsetStatus,
                state.rightHeadsetStatus,
                state.caseHeadsetStatus,
                state.headsetStatus,
                state.fwInfo,
            )
        )
    }

    private suspend fun listenData() = withContext(Dispatchers.IO) {
        try {

            while (true) {
                val bytes = inputStream.available()
                if (bytes != 0) {
                    errorViewState.postValue(null)
                    val tempBuffer = ByteArray(bytes)
                    inputStream.read(tempBuffer, 0, bytes)


                    if (tempBuffer.size == 25) {
                        Log.i("VM Bluetooth", "!!!!!!!")
//                        val parsed = tempBuffer.joinToString(" ") { "%02x".format(it) }
//                        Log.i("VM Bluetooth", parsed)
                        val mutableList: MutableList<Byte> = tempBuffer.toMutableList()

                        val gyroConverted = gyroConvert(mutableList)

                        audioManager.setParameters("pitch=${gyroConverted.pitch};row=${gyroConverted.row};yaw=${gyroConverted.yaw}")
                        //audioManager.setParameters("pitch=-8.154936;row=-39.00848;yaw=326.34033")

                        val last = mutableList[24]

                        mutableList[3] = 0x01.toByte()
                        mutableList.add(6, 0x12.toByte())
                        mutableList[7] = 0x00.toByte()
                        mutableList[25] = last


                        //0F 00 21 01 CC EB B3 43 E6 74 E4 C0 B7 C9 1D C2
                        //String=yaw=359.84216;pitch=-7.13927;row=-39.446987;
                        //[15, 0, 33, 1, -52, -21, -77, 67, -26, 116, -28, -64, -73, -55, 29, -62]

//                        val bytesT = listOf(15, 0, 33, 1, -52, -21, -77, 67, -26, 116, -28, -64, -73, -55, 29, -62)
//                        val parsedT = bytesT.joinToString(" ") { "%02x".format(it) }

                        //  val bytesT = listOf(0x43, 0xB3, 0xEB, 0xCC)
//                        val float2 = (3*10).toInt().pow(3)


                        //   Log.i("VM Bluetooth", "$bytesT")


                        btService.sendServiceMessage(mutableList.toByteArray())
                        //    btService.sendServiceMessage()
                    }

                    // Status Headset
                    // byteArr[6] is 0x04 --> headset mode
                    // byteArr[6] is 0x0f --> battery percent
                    // byteArr[6] is 0x3b --> battery charging status
                    when (tempBuffer[6]) {
                        0x04.toByte() -> {
                            // Headset Mode
                            // byteArr[10] is 0x01 --> headset mode
                            // byteArr[10] is 0x02 --> battery percent
                            // byteArr[10] is 0x03 --> battery charging status
                            when (val headsetMode = tempBuffer[10]) {
                                0x00.toByte() -> {
                                    if (tempBuffer.size < 24) {
                                        btService.checkHeadsetMode()
                                        break
                                    }
                                    Log.i("VM Bluetooth", "${headsetMode.toInt()} ОТКЛЮЧЕНО SIZE:${tempBuffer.size}")
                                    if (state.value is HomeState.HomeStateLoaded) {
                                        val headsetStatus =
                                            HeadsetSettingStatus(HeadsetMainSetting.Off, tempBuffer[24].toInt())
                                        viewState.postValue(
                                            HomeState.HomeStateLoaded(
                                                true,
                                                1,
                                                (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
                                                headsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).fwInfo,
                                            )
                                        )
                                    }
                                }
                                0x01.toByte() -> {
                                    if (tempBuffer.size < 24) {
                                        btService.checkHeadsetMode()
                                        break
                                    }
                                    Log.i(
                                        "VM Bluetooth",
                                        "${headsetMode.toInt()} Включен режим: ШУМОДАВ SIZE:${tempBuffer.size}"
                                    )
                                    if (state.value is HomeState.HomeStateLoaded) {
                                        val noiseValue = when (tempBuffer[24]) {
                                            0x03.toByte() -> 0
                                            0x01.toByte() -> 1
                                            0x00.toByte() -> 2
                                            0x02.toByte() -> 3
                                            else -> 0
                                        }
                                        val headsetStatus =
                                            HeadsetSettingStatus(HeadsetMainSetting.Noise, noiseValue)
                                        viewState.postValue(
                                            HomeState.HomeStateLoaded(
                                                true,
                                                0,
                                                (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
                                                headsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).fwInfo,
                                            )
                                        )
                                    }
                                }
                                0x02.toByte() -> {
                                    if (tempBuffer.size < 24) {
                                        btService.checkHeadsetMode()
                                        break
                                    }
                                    Log.i(
                                        "VM Bluetooth",
                                        "${headsetMode.toInt()} Включен режим: ПРОЗРАЧНОСТЬ SIZE:${tempBuffer.size}"
                                    )
                                    if (state.value is HomeState.HomeStateLoaded) {
                                        val headsetStatus =
                                            HeadsetSettingStatus(
                                                HeadsetMainSetting.Transparency,
                                                tempBuffer[24].toInt()
                                            )
                                        viewState.postValue(
                                            HomeState.HomeStateLoaded(
                                                true,
                                                2,
                                                (state.value as HomeState.HomeStateLoaded).leftHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).rightHeadsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).caseHeadsetStatus,
                                                headsetStatus,
                                                (state.value as HomeState.HomeStateLoaded).fwInfo,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        0x0f.toByte() -> {
                            Log.i("VM Bluetooth", "Size: ${tempBuffer.size}")
                            // Left earphone battery percent
                            val bLPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[10])
                            // Right earphone battery percent
                            val bRPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[11])
                            // Case battery percent
                            val bCPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[12])
                            if (state.value is HomeState.HomeStateLoaded) {
                                viewState.postValue(
                                    HomeState.HomeStateLoaded(
                                        true,
                                        (state.value as HomeState.HomeStateLoaded).mainHeadsetValue,
                                        LHeadsetBatteryStatus(bLPercent.battery, bLPercent.isCharging),
                                        RHeadsetBatteryStatus(bRPercent.battery, bRPercent.isCharging),
                                        CHeadsetBatteryStatus(bCPercent.battery, bCPercent.isCharging),
                                        (state.value as HomeState.HomeStateLoaded).headsetStatus,
                                        (state.value as HomeState.HomeStateLoaded).fwInfo,
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
                                bytePercentConverter(tempBuffer[54])
                            // Right earphone battery charging status
                            val bRInfoPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[55])
                            // Case battery charging status
                            val bCInfoPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[56])
                            // Headset Firmware Version
                            val firmwareInfo = byteFirmwareVersionConverter(tempBuffer[31], tempBuffer[32])


                            if (state.value is HomeState.HomeStateLoaded) {
                                viewState.postValue(
                                    HomeState.HomeStateLoaded(
                                        true,
                                        (state.value as HomeState.HomeStateLoaded).mainHeadsetValue,
                                        LHeadsetBatteryStatus(bLInfoPercent.battery, bLInfoPercent.isCharging),
                                        RHeadsetBatteryStatus(bRInfoPercent.battery, bRInfoPercent.isCharging),
                                        CHeadsetBatteryStatus(bCInfoPercent.battery, bCInfoPercent.isCharging),
                                        (state.value as HomeState.HomeStateLoaded).headsetStatus,
                                        (state.value as HomeState.HomeStateLoaded).fwInfo,
                                    )
                                )
                            } else {
                                viewState.postValue(
                                    HomeState.HomeStateLoaded(
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
                            btService.checkHeadsetMode()
                            Log.i(
                                "VM Bluetooth", "Заряд INFO: " +
                                        "L: ${if (bLInfoPercent.isCharging) "🔋" else ""}${bLInfoPercent.battery} " +
                                        "R: ${if (bRInfoPercent.isCharging) "🔋" else ""}${bRInfoPercent.battery} " +
                                        "C: ${if (bCInfoPercent.isCharging) "🔋" else ""}${bCInfoPercent.battery} " +
                                        "Firmware version : ${firmwareInfo.version}"
                            )
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("VM Bluetooth", "IOException: ${e.message}")
        } catch (e: IndexOutOfBoundsException) {
            Log.e("VM Bluetooth", "IndexOutOfBoundsException: ${e.message}")
            btService.checkHeadsetMode()
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
        for (i in percentListBattery.indices) {
            if (percentListBattery[i] == p) {
                return HeadsetBatteryStatus("${percentList[i]}%", true)
            }
        }
        return HeadsetBatteryStatus("$p%")
    }

    private fun gyroConvert(gyroData: MutableList<Byte>): HeadsetGyro {
        val yaw = gyroData.slice(12..15).map { "%02x".format(it) }.asReversed().joinToString(separator = "")
        val pitch = gyroData.slice(16..19).map { "%02x".format(it) }.asReversed().joinToString(separator = "")
        val row = gyroData.slice(20..23).map { "%02x".format(it) }.asReversed().joinToString(separator = "")

        val yawConverted = gyroConvertPosition(yaw)
        val pitchConverted = gyroConvertPosition(pitch)
        val rowConverted = gyroConvertPosition(row)
        return HeadsetGyro(yaw = yawConverted, pitch = pitchConverted, row = rowConverted)
    }

    private fun gyroConvertPosition(position: String?): Float {
        if (position == null) return 0.0f
        return java.lang.Float.intBitsToFloat(position.toLong(16).toInt())
    }
}

