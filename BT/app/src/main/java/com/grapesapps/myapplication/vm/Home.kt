package com.grapesapps.myapplication.vm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grapesapps.myapplication.BluetoothService
import com.grapesapps.myapplication.R
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
    private lateinit var inputStream: InputStream


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
    fun connectDevice(btDevice: BluetoothDevice) {
        val isConnected = btService.isConnected()
        val isConnection = btService.statusConnection
        if (isConnected && !isConnection) {
            viewModelScope.launch(Dispatchers.IO) {
                btService.connectDevice(btDevice, uuid)
                inputStream = btService.inputStream
                btService.getHeadsetInfo()
                listenData()
            }
        } else if (!isConnected && !isConnection) {
            viewModelScope.launch(Dispatchers.IO) {
                btService.connectDevice(btDevice, uuid)
                inputStream = btService.inputStream
                btService.getHeadsetInfo()
                listenData()
            }

        }
    }

    fun onSelectSpectralAudio() {
        val isConnected = btService.isConnected()
        viewModelScope.launch(Dispatchers.IO) {
            if (isConnected) {
                btService.sendData(
                    byteArrayOfInts(
                        listOf(
                            0xfe,0xdc,0xba,0xc1,0xf4,0x00,0x11,0x1f,0x0f,0x00,0x21,0x01,0x38,0x4c,0x9e,0x43,0xaa,0xb3,0xac,0xc1,0x79,0xc4,0x32,0xc2,0xef
                        )
                    )
                )
               // btService.activateSpectralAudio()
            }

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


                    // Log.i("VM Bluetooth", "${tempBuffer.map { it.toUByte() }}")
                    if(tempBuffer.size != 25) {
                        val parsed = tempBuffer.joinToString(" ") { "%02x".format(it) }
                        Log.i("VM Bluetooth", parsed)
                    }


                    //fe dc ba c1 f4 00 11 22 0f 00 21 00 01 e6 b2 43 9b 1d 1c c1 74 86 93 41 ef
                    //fe dc ba 01 f4 00 12 00 22 0f 00 21 01 01 e6 b2 43 9b 1d 1c c1 74 86 93 41 ef

                    //fe dc ba c1 f4 00 11 63 0f 00 21 01 12 a9 a7 43 1b 26 13 c1 56 07 04 c2 ef
                    //fe dc ba 01 f4 00 12 00 63 0f 00 21 01 12 a9 a7 43 1b 26 13 c1 56 07 04 c2 ef


                    //fe dc ba c1 f4 00 11 65 0f 00 21 01 0f 8f a7 43 63 fd 0d c1 05 af fb c1 ef
                    //fe dc ba 01 f4 00 12 00 65 0f 00 21 01 0f 8f a7 43 63 fd 0d c1 05 af fb c1 ef


                    //fe dc ba c1 f4 00 11 f9 0f 00 21 01 db 01 8b 43 ef 9a 76 c1 a6 c0 df c1 ef
                    //fe dc ba 01 f4 00 12 00 f9 0f 00 21 01 db 01 8b 43 ef 9a 76 c1 a6 c0 df c1 ef

                    //fe dc ba c1 f4 00 11 35 0f 00 21 01 80 6c 5f 40 b4 88 bd be 67 54 ef c1 ef
                    //fe dc ba 01 f4 00 12 00 35 0f 00 21 01 80 6c 5f 40 b4 88 bd be 67 54 ef c1 ef



                    if(tempBuffer.size == 27 ){
                        btService.sendData(
                            byteArrayOfInts(
                                listOf(
                                    0xfe,0xdc,0xba,0xc1,0x51,0x00,0x03,0x1a,0x01,0x00,0xef
                                )
                            )
                        )

                    }

                    if (tempBuffer.size == 26) {
                        btService.sendData(
                            byteArrayOfInts(
                                listOf(
                                    0xfe,0xdc,0xba,0xc1,0x50,0x00,0x12,0x19,0x01,0x0c,0xaf,0xd8,0xf7,0xf2,0x42,0xd8,0x5a,0x14,0x4b,0xf0,0xeb,0xe5,0xa5,0xa7,0x5e,0xef
                                )
                            )
                        )
                    }

                    if(tempBuffer.size == 37){
                        btService.sendData(
                            byteArrayOfInts(
                                listOf(
                                    0xfe,0xdc,0xba,0x01,0x50,0x00,0x13,0x00,0x02,0x01,0x05,0x63,0x7f,0x35,0x5b,0xe8,0xe6,0xf4,0xa6,0x8c,0x04,
                                    0x81,0xc6,0x50,0x3c,0xb8,0xef
                                )
                            )
                        )
                    }
                    if(tempBuffer.size == 11){
                        btService.sendData(
                            byteArrayOfInts(
                                listOf(
                                    0xfe,0xdc,0xba,0x01,0x51,0x00,0x03,0x00,0x01,0x01,0xef
                                )
                            )
                        )
                        btService.sendData(
                            byteArrayOfInts(
                                listOf(
                                    0xfe,0xdc,0xba,0xc1,0xf3,0x00,0x11,0x04,0x00,0x01,0x00,0x02,0x00,0x03,0x00,0x04,0x00,0x0a,0x00,0x0b,0x00,0x0f,0x00,0x24,0xef
                                )
                            )
                        )
                    }




                    if (tempBuffer.size == 25) {
//                        Log.i("VM Bluetooth", "!!!!!!!")
//                        val parsed = tempBuffer.joinToString(" ") { "%02x".format(it) }
//                        Log.i("VM Bluetooth", parsed)
                        val mutableList: MutableList<Byte> = tempBuffer.toMutableList()
                        val last = mutableList[24]

                        mutableList[3] = 0x01.toByte()
                        mutableList.add(6, 0x12.toByte())
                        mutableList[7] = 0x00.toByte()
                        mutableList[25] = last

//                        Log.i("VM Bluetooth", data.map { it.toByte() }.joinToString(" ") { "%02x".format(it) })
//                        Log.i(
//                            "VM Bluetooth",
//                            mutableList.toImmutableList().map { it.toByte() }.joinToString(" ") { "%02x".format(it) })
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
}