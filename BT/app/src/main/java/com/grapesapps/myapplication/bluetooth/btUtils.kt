package com.grapesapps.myapplication.bluetooth

class BluetoothUtils {
    companion object {
        const val ACTION_DATA_FROM_HEADPHONES = "ACTION_DATA_FROM_HEADPHONES"
        const val ACTION_DATA_SPECIFIC_VENDOR = "ACTION_DATA_SPECIFIC_VENDOR"
        const val ACTION_REQUEST_PERMISSION = "ACTION_REQUEST_PERMISSION"
        const val ACTION_REQUEST_PERMANENT_DENIED_PERMISSION = "ACTION_REQUEST_PERMANENT_DENIED_PERMISSION"
        const val ACTION_BT_ON = "ACTION_BT_ON"
        const val ACTION_BT_OFF = "ACTION_BT_OFF"
        const val ACTION_DEVICE_INITIAL = "ACTION_DEVICE_INITIAL"
        const val ACTION_DISCOVERY_STARTED = "ACTION_DISCOVERY_STARTED"
        const val ACTION_DISCOVERY_STOPPED = "ACTION_DISCOVERY_STOPPED"
        const val ACTION_DEVICE_NOT_FOUND = "ACTION_DEVICE_NOT_FOUND"
        const val ACTION_DEVICE_FOUND = "ACTION_DEVICE_FOUND"
        const val ACTION_DEVICE_CONNECTED = "ACTION_DEVICE_CONNECTED"
        const val ACTION_DEVICE_FOUND_CONNECTED = "ACTION_DEVICE_FOUND_CONNECTED"
        const val ACTION_DEVICE_DISCONNECTED = "ACTION_DEVICE_DISCONNECTED"
        const val ACTION_MESSAGE_RECEIVED = "ACTION_MESSAGE_RECEIVED"
        const val ACTION_MESSAGE_SENT = "ACTION_MESSAGE_SENT"
        const val ACTION_CONNECTION_ERROR = "ACTION_CONNECTION_ERROR"
        const val EXTRA_DEVICE = "EXTRA_DEVICE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        const val EXTRA_DATA = "EXTRA_DATA"
        const val EXTRA_DATA_IS_ENABLED_SURROUND = "EXTRA_DATA_IS_ENABLED_SURROUND"
        const val EXTRA_DATA_IS_AVAILABLE_SURROUND = "EXTRA_DATA_IS_AVAILABLE_SURROUND"
    }
}

object BluetoothBatteryCommands {
    // in Service
    val percentList = listOf(
        0x00, 0x05, 0x0a, 0x0f, 0x14, 0x19, 0x1e, 0x23, 0x28,
        0x2d, 0x32, 0x37, 0x3c, 0x41, 0x46, 0x4b, 0x50, 0x55,
        0x5a, 0x5f, 0x64
    )

    // in Service
    val percentListBattery = listOf<Byte>(
        -128, -123, -118, -113, -108, -103, -98, -93, -88,
        -83, -78, -73, -68, -63, -58, -53, -48, -43,
        -38, -33, -28
    )
}

object BluetoothCommands {

    val headsetInfo = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x02, 0x00, 0x05,
        0x02, 0xff, 0xff, 0xff, 0xff, 0xef
    )

    // Disable mode
    val off = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
        0x02, 0x04, 0x00, 0xef
    )

    // ШУМ
    val noise = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
        0x02, 0x04, 0x01, 0xef
    )

    // Прозрачность
    val transparency = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05,
        0x02, 0x04, 0x02, 0xef
    )

    val checkHeadsetMode = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07,
        0x02, 0x04, 0x05, 0xef
    )

    // Обнаружения уха вкл
    val autoSearchEarOn = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x0f,
        0x02, 0x06, 0x00, 0xef
    )

    // Обнаружения уха выкл
    val autoSearchEarOff = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x0e,
        0x02, 0x06, 0x01, 0xef
    )

    // Автоответ на звонок вкл
    val autoPhoneAnswerOn = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x05, 0x14,
        0x03, 0x00, 0x03, 0x01, 0xef
    )

    // Автоответ на звонок выкл
    val autoPhoneAnswerOff = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x05, 0x15,
        0x03, 0x00, 0x03, 0x00, 0xef
    )

    // Поиск левого наушника вкл
    val searchLeftHeadphoneOn = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x1e,
        0x04, 0x00, 0x09, 0x01, 0x01, 0xef
    )

    // Поиск левого наушника выкл
    val searchLeftHeadphoneOff = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x20,
        0x04, 0x00, 0x09, 0x00, 0x01, 0xef
    )

    // Поиск правого наушника вкл
    val searchRightHeadphoneOn = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x22,
        0x04, 0x00, 0x09, 0x01, 0x02, 0xef
    )

    // Поиск правого наушника выкл
    val searchRightHeadphoneOff = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x24,
        0x04, 0x00, 0x09, 0x00, 0x02, 0xef
    )


    // Поиск обоих наушников вкл
    val searchAllHeadphoneOn = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x26,
        0x04, 0x00, 0x09, 0x01, 0x03, 0xef
    )

    // Поиск обоих наушников выкл
    val searchAllHeadphoneOff = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x06, 0x28,
        0x04, 0x00, 0x09, 0x00, 0x03, 0xef
    )

    // проверка прилегания наушников
    val startHeadTest = listOf(
        0xfe, 0xdc, 0xba, 0xc1, 0xf2, 0x00, 0x05, 0x08, 0x03, 0x00, 0x05, 0x01, 0xef
    )
}