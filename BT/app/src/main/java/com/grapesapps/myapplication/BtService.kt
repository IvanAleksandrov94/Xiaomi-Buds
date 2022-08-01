package com.grapesapps.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothService @Inject constructor() {
    companion object {
        private const val TAG = "BT_CONNECT"

        private fun byteArrayOfInts(ints: List<Int>) = ByteArray(ints.size) { pos -> ints[pos].toByte() }


        // headset info and version
        private val service0 = listOf(
            0xfe, 0xdc, 0xba, 0x01, 0xf4, 0x00, 0x0d, 0x00,
            0x05, 0x0a, 0x00, 0x22, 0x00, 0x00, 0x98, 0x42,
            0x0a, 0x00, 0x00, 0x00, 0xef
        )

        // headset info and version
        private val service1 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x02, 0x00, 0x05, 0x05, 0xff, 0xff, 0xff,
            0xff, 0xef, 0xfe, 0xdc, 0xba, 0xc1, 0x09, 0x00, 0x05, 0x06, 0xff,
            0xff, 0xff, 0xff, 0xef
        )

        // headset info and version
        private val service2 = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0xf3, 0x00, 0x11, 0x07, 0x00, 0x01, 0x00,
            0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x0a, 0x00, 0x0b, 0x00, 0x0f,
            0x00, 0x24, 0xef
        )

        // spectral
        private val spectral = listOf(
             0xef, 0x3f, 0x0d, 0x0a, 0x2b, 0x58, 0x49, 0x41, 0x4f, 0x4d, 0x49, 0x3a, 0x20, 0x46, 0x46,
            0x30, 0x31, 0x30, 0x32, 0x30, 0x31, 0x30, 0x33, 0x30, 0x32, 0x30, 0x35, 0x30, 0x31, 0x46, 0x46,
            0x0d, 0x0a, 0x55

        )

//        const val command0 = "\r\nOK\r\n"
//        const val command1 = "\r\n+CIEV: 4,3\r\n"
        const val command2 = "\r\n+XIAOMI: FF01020103020501FF\r\n"


//        const val command0 = "AT+BRSF=20\r"
//        const val command1 = "OK"
//        const val command2 = "+XIAOMI: FF01020103020501FF"


        // headset info and version
        private val headsetInfo = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x02, 0x00, 0x05,
            0x02, 0xff, 0xff, 0xff, 0xff, 0xef
        )

        // Disable mode
        private val off = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
            0x02, 0x04, 0x00, 0xef
        )

        // ШУМ
        private val noise = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
            0x02, 0x04, 0x01, 0xef
        )

        // Прозрачность
        private val transparency = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
            0x02, 0x04, 0x02, 0xef
        )

        private val checkHeadsetMode = listOf(
            0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
            0x02, 0x04, 0x05, 0xef
        )
    }

    private lateinit var outputStream: OutputStream
    lateinit var inputStream: InputStream
    private lateinit var btSocket: BluetoothSocket
    private lateinit var btDevice: BluetoothDevice
    private lateinit var btUUID: UUID
    var statusConnection: Boolean = false

    suspend fun activateOffMode() = sendData(byteArrayOfInts(off))
    suspend fun activateNoiseMode() = sendData(byteArrayOfInts(noise))
    suspend fun activateTransparencyMode() = sendData(byteArrayOfInts(transparency))

//    suspend fun sendServiceMessage() {
//        sendData(byteArrayOfInts(service0))
//        sendData(byteArrayOfInts(service1))
//        sendData(byteArrayOfInts(service2))
//    }

    suspend fun activateSpectralAudio() {
        sendData(command2.map { it.toByte() }.toByteArray())
        // sendData(command1.toByteArray())
        //  sendData(command2.toByteArray())
    }
 //   suspend fun activateSpectralAudio() = sendData(byteArrayOfInts(spectral))

    suspend fun getHeadsetInfo() = sendData(byteArrayOfInts(headsetInfo))
    suspend fun checkHeadsetMode() = sendData(byteArrayOfInts(checkHeadsetMode))

    private suspend fun reconnect(device: BluetoothDevice, uuid: UUID) = connectDevice(device, uuid)

    fun isConnected() = (this::btSocket.isInitialized && btSocket.isConnected)


    @SuppressLint("MissingPermission")
    suspend fun connectDevice(device: BluetoothDevice, uuid: UUID) {
        statusConnection = true
        withContext(Dispatchers.IO) {
            btDevice = device
            btUUID = uuid
            btSocket = device.createRfcommSocketToServiceRecord(uuid)
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


    suspend fun sendData(data: ByteArray) = withContext(Dispatchers.IO) {
        try {
//            val parsed = data.joinToString(" ") { "%02x".format(it) }
//            Log.i("VM Bluetooth", parsed)
            outputStream.write(data)
        } catch (e: IOException) {
            if (e.message == "Broken pipe") {
                Log.e(TAG, "${e.message}, need reconnect")
                try {
                    reconnect(btDevice, btUUID)
                    outputStream.write(data)
                } catch (e: IOException) {
                    Log.e(TAG, "${e.message}")
                }
            } else {
                Log.e(TAG, "${e.message}")
            }
        }
    }
}

