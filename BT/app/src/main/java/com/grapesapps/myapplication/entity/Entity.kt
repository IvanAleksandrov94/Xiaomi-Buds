package com.grapesapps.myapplication.entity

enum class HeadsetMainSetting { Off, Noise, Transparency }
data class LHeadsetBatteryStatus(val battery: String, val isCharging: Boolean = false)
data class RHeadsetBatteryStatus(val battery: String, val isCharging: Boolean = false)
data class CHeadsetBatteryStatus(val battery: String, val isCharging: Boolean = false)
data class HeadsetBatteryStatus(val battery: String, val isCharging: Boolean = false)
data class HeadsetSettingStatus(val setting: HeadsetMainSetting, val value: Int = 0)
data class FirmwareInfo(val version: String)


