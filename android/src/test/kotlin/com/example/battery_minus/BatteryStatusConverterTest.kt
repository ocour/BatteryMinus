package com.example.battery_minus

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryStatusConverterTest {
    @Test
    fun `batteryStatusString correctly converts value to string`() {
        val result = BatteryStatusConverter.batteryStatusToString(BatteryManager.BATTERY_STATUS_UNKNOWN)
        assertEquals(BatteryStatusConverter.BATTERY_STATUS_UNKNOWN, result)
    }

    @Test
    fun `batteryStatusString will return BATTERY_STATUS_ERROR on invalid input parameter`() {
        val result = BatteryStatusConverter.batteryStatusToString(88)
        assertEquals(BatteryStatusConverter.BATTERY_STATUS_ERROR, result)
    }
}