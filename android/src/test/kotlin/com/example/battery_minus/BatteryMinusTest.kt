package com.example.battery_minus

import android.content.Context
import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class BatteryMinusTest {
    @InjectMocks
    private lateinit var sut: BatteryMinus

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockBatteryManager: BatteryManager

    @Before
    fun setUp() {
        `when`(mockContext.getSystemService(Context.BATTERY_SERVICE)).thenReturn(mockBatteryManager)
    }

    // Does not currently work
    @Test
    fun `isCharging will return correct state`() {
        verify(mockContext, never()).getSystemService(Context.BATTERY_SERVICE)

        `when`(mockBatteryManager.isCharging).thenReturn(true, false)

        val firstResult = sut.isCharging

        verify(mockContext).getSystemService(Context.BATTERY_SERVICE)
        verify(mockBatteryManager).isCharging

        assertEquals(true, firstResult)

        val secondResult = sut.isCharging
        assertEquals(false, secondResult)

        verify(mockContext, times(1)).getSystemService(Context.BATTERY_SERVICE)
    }
}