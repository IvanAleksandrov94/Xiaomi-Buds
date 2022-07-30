package com.grapesapps.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.grapesapps.myapplication.entity.FirmwareInfo
import com.grapesapps.myapplication.entity.HeadsetBatteryStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.util.*


object BluetoothService {
    private const val TAG = "BT_CONNECT"
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream
    private lateinit var btSocket: BluetoothSocket
    private lateinit var btDevice: BluetoothDevice
    private lateinit var btUUID: UUID
    var statusConnection: Boolean = false

    //    private val headsetInfo = listOf(
//        0xFE, 0xDC, 0xBA, 0xC1, 0x02, 0x00, 0x05,
//        0x02, 0xFF, 0xFF, 0xFF, 0xFF, 0xEF
//    )
//    private val checkHeadsetMode = listOf(
//        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
//        0x02, 0x04, 0x05, 0xef
//    )
//
//    private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    private val percentList = listOf(
        0x00, 0x05, 0x0a, 0x0f, 0x14, 0x19, 0x1e, 0x23, 0x28,
        0x2d, 0x32, 0x37, 0x3c, 0x41, 0x46, 0x4b, 0x50, 0x55,
        0x5a, 0x5f, 0x64
    )
    private val percentListBattery = listOf<Byte>(
        -128, -123, -118, -113, -108, -103, -98, -93, -88,
        -83, -78, -73, -68, -63, -58, -53, -48, -43,
        -38, -33, -28
    )

    private suspend fun reconnect(device: BluetoothDevice, uuid: UUID) = connectDevice(device, uuid)

    fun connected() = (this::btSocket.isInitialized && btSocket.isConnected)

    @SuppressLint("MissingPermission")
    suspend fun connectDevice(device: BluetoothDevice, uuid: UUID) {
        statusConnection = true
        withContext(Dispatchers.IO) {
            btDevice = device
            btUUID = uuid
            btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            outputStream = btSocket.outputStream
            inputStream = btSocket.inputStream
            try {
                if (btSocket.isConnected) {
                    btSocket.close()
                }
                btSocket.connect()
                Log.i(TAG, "${device.name} (${device.address}) is connected")
                statusConnection = false
            } catch (e: IOException) {
                try {
                    if (btSocket.isConnected) {
                        btSocket.close()
                    }
                    reconnect(device, uuid)
                    Log.i(TAG, "$${device.name} (${device.address}) is reconnected")
                } catch (e: IOException) {
                    Log.e(TAG, "$${device.name} (${device.address}): reconnect Error $e")
                }
                Log.e(TAG, "$${device.name} (${device.address}): BT Connect Error $e")
            }
        }
    }

    fun disconnect() {
        try {
            outputStream.close()
            inputStream.close()
            btSocket.close()
        } catch (e: Throwable) {
            Log.e(TAG, "${e.message}")
        }
    }

    suspend fun sendData(data: ByteArray) = coroutineScope {
        withContext(Dispatchers.IO) {
            try {
                outputStream.write(data)
                listenData()
            } catch (e: IOException) {
                if (e.message == "Broken pipe") {
                    Log.e(TAG, "${e.message}, need reconnect")
                    try {
                        reconnect(btDevice, btUUID)
                        outputStream.write(data)
                        listenData()
                    } catch (e: IOException) {
                        Log.e(TAG, "${e.message}")
                    }
                } else {
                    Log.e(TAG, "${e.message}")
                }
            }
        }
    }

    private suspend fun listenData() = withContext(Dispatchers.IO) {
        try {
            while (true) {
                val bytes = inputStream.available()
                if (bytes != 0) {
                    val tempBuffer = ByteArray(bytes)
                    inputStream.read(tempBuffer, 0, bytes)
                    Log.i(TAG, "${tempBuffer.map { it }}")
                    val parsed = tempBuffer.joinToString(" ") { "%02x".format(it) }
                    Log.i(TAG, parsed)


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
                                0x00.toByte() -> Log.i(TAG, "${headsetMode.toInt()} ОТКЛЮЧЕНО")
                                0x01.toByte() -> Log.i(TAG, "${headsetMode.toInt()} Включен режим: ШУМОДАВ")
                                0x02.toByte() -> Log.i(TAG, "${headsetMode.toInt()} Включен режим: ПРОЗРАЧНОСТЬ")
                            }
                        }
                        0x0f.toByte() -> {
                            // Left earphone battery percent
                            val bLPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[10])
                            // Right earphone battery percent
                            val bRPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[11])
                            // Case battery percent
                            val bCPercent: HeadsetBatteryStatus =
                                bytePercentConverter(tempBuffer[12])

                            Log.i(
                                TAG, "Заряд INFO: " +
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

                            Log.i(
                                TAG, "Заряд INFO: " +
                                        "L: ${if (bLInfoPercent.isCharging) "🔋" else ""}${bLInfoPercent.battery} " +
                                        "R: ${if (bRInfoPercent.isCharging) "🔋" else ""}${bRInfoPercent.battery} " +
                                        "C: ${if (bCInfoPercent.isCharging) "🔋" else ""}${bCInfoPercent.battery} " +
                                        "Firmware version : ${firmwareInfo.version}"
                            )
                        }
                    }
                }
                delay(300L)
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}")
        } catch (e: Throwable) {
            Log.e(TAG, "${e.message}")
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

