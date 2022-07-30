package com.grapesapps.myapplication.entity

data class HeadsetBatteryStatus(val battery: String, val isCharging: Boolean = false)

data class FirmwareInfo(val version: String)