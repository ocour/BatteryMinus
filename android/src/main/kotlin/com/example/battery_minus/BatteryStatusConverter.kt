package com.example.battery_minus

import android.os.BatteryManager

class BatteryStatusConverter {
    companion object {
        const val BATTERY_STATUS_ERROR = "BATTERY_STATUS_ERROR"
        const val BATTERY_STATUS_UNKNOWN = "BATTERY_STATUS_UNKNOWN"
        const val BATTERY_STATUS_CHARGING = "BATTERY_STATUS_CHARGING"
        const val BATTERY_STATUS_DISCHARGING = "BATTERY_STATUS_DISCHARGING"
        const val BATTERY_STATUS_NOT_CHARGING = "BATTERY_STATUS_NOT_CHARGING"
        const val BATTERY_STATUS_FULL = "BATTERY_STATUS_FULL"

        private val batteryStatusMap = mapOf<Int, String>(
            BatteryManager.BATTERY_STATUS_UNKNOWN to BATTERY_STATUS_UNKNOWN,
            BatteryManager.BATTERY_STATUS_CHARGING to BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_DISCHARGING to BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING to BATTERY_STATUS_NOT_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL to BATTERY_STATUS_FULL,
        )

        fun batteryStatusToString(status: Int): String {
            return batteryStatusMap[status] ?: BATTERY_STATUS_ERROR
        }
    }
}