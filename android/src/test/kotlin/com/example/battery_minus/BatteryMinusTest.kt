package com.example.battery_minus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class BatteryMinusTest {
    private lateinit var closeable: AutoCloseable

    @InjectMocks
    private lateinit var sut: BatteryMinus

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockBatteryManager: BatteryManager

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSystemService(Context.BATTERY_SERVICE)).thenReturn(mockBatteryManager)
    }

    @After
    fun cleanUp() {
        closeable.close()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `isCharging will return correct state through BatteryManager_isCharging on SDK larger_or_equal to 23`() {
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `isCharging will return correct state through intent on SDK smaller_or_equal to 22`() {
        val mockIntent: Intent = mock()
        `when`(mockIntent.getIntExtra(eq(BatteryManager.EXTRA_STATUS), anyInt()))
            .thenReturn(
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_NOT_CHARGING
            )

        `when`(mockContext.registerReceiver(eq(null), any(IntentFilter::class.java)))
            .thenReturn(mockIntent)

        val firstResult = sut.isCharging
        assertEquals(true, firstResult)

        val secondResult = sut.isCharging
        assertEquals(false, secondResult)

        verify(mockContext, never()).getSystemService(Context.BATTERY_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `get correct capacity through BatteryManager_getIntProperty on SDK larger_or_equal to 21`() {
        `when`(mockBatteryManager.getIntProperty(eq(BatteryManager.BATTERY_PROPERTY_CAPACITY)))
            .thenReturn(100, 99)

        val firstResult = sut.capacity
        assertEquals(100, firstResult)

        val secondResult = sut.capacity
        assertEquals(99, secondResult)

        verify(mockContext, times(1)).getSystemService(Context.BATTERY_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.KITKAT]) // API level 20 is not available, KITKAT is 19
    fun `get correct capacity through intent on SDK smaller_or_equal to 20`() {
        val mockIntent: Intent = mock()
        // current battery level
        `when`(mockIntent.getIntExtra(eq(BatteryManager.EXTRA_LEVEL), anyInt()))
            .thenReturn(100, 99)
        // Maximum battery level
        `when`(mockIntent.getIntExtra(eq(BatteryManager.EXTRA_SCALE), anyInt()))
            .thenReturn(100)

        `when`(mockContext.registerReceiver(eq(null), any(IntentFilter::class.java)))
            .thenReturn(mockIntent)

        val firstResult = sut.capacity
        assertEquals(100, firstResult)

        val secondResult = sut.capacity
        assertEquals(99, secondResult)

        verify(mockContext, never()).getSystemService(Context.BATTERY_SERVICE)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `batteryState will return converted battery state, test for SDK larger_or_equal to 33`() =
        runTest {
            val mockIntent: Intent = mock()
            `when`(mockIntent.action).thenReturn(Intent.ACTION_BATTERY_CHANGED)
            `when`(mockIntent.getIntExtra(eq(BatteryManager.EXTRA_STATUS), anyInt())).thenReturn(
                BatteryManager.BATTERY_STATUS_UNKNOWN,
                BatteryManager.BATTERY_STATUS_UNKNOWN,
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_DISCHARGING,
                BatteryManager.BATTERY_STATUS_NOT_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL,
            )

            val broadcastReceiverCaptor = ArgumentCaptor.forClass(BroadcastReceiver::class.java)
            `when`(
                mockContext.registerReceiver(
                    broadcastReceiverCaptor.capture(),
                    any(),
                    eq(Context.RECEIVER_NOT_EXPORTED)
                )
            ).thenReturn(mockIntent)
            doNothing().`when`(mockContext).unregisterReceiver(any())

            val collectedItems = mutableListOf<String>()
            val batteryStatusFlow = sut.batteryStatus.onEach { collectedItems.add(it) }
            val job = launch { batteryStatusFlow.collect() }
            advanceUntilIdle() // wait for callbackFlow builder to call registerReceiver

            repeat(5) { _ ->
                broadcastReceiverCaptor.value.onReceive(
                    mockContext,
                    mockIntent,
                )
            }

            advanceUntilIdle() // wait flow collection to end
            job.cancel()
            advanceUntilIdle() // wait for awaitClose

            verify(mockContext).registerReceiver(
                eq(broadcastReceiverCaptor.value),
                any(IntentFilter::class.java),
                eq(Context.RECEIVER_NOT_EXPORTED)
            )
            verify(mockContext).unregisterReceiver(eq(broadcastReceiverCaptor.value))

            val expectedResultList = listOf(
                BatteryStatusConverter.BATTERY_STATUS_UNKNOWN,
                BatteryStatusConverter.BATTERY_STATUS_UNKNOWN,
                BatteryStatusConverter.BATTERY_STATUS_CHARGING,
                BatteryStatusConverter.BATTERY_STATUS_DISCHARGING,
                BatteryStatusConverter.BATTERY_STATUS_NOT_CHARGING,
                BatteryStatusConverter.BATTERY_STATUS_FULL,
            )

            assertEquals(expectedResultList, collectedItems.toList())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun `batteryState will return converted battery state, test for SDK smaller_or_equal to 32`() =
        runTest {
            val mockIntent: Intent = mock()
            `when`(mockIntent.action).thenReturn(Intent.ACTION_BATTERY_CHANGED)
            `when`(mockIntent.getIntExtra(eq(BatteryManager.EXTRA_STATUS), anyInt())).thenReturn(
                BatteryManager.BATTERY_STATUS_UNKNOWN,
                BatteryManager.BATTERY_STATUS_UNKNOWN,
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_DISCHARGING,
                BatteryManager.BATTERY_STATUS_NOT_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL,
            )

            val broadcastReceiverCaptor = ArgumentCaptor.forClass(BroadcastReceiver::class.java)
            `when`(
                mockContext.registerReceiver(
                    broadcastReceiverCaptor.capture(),
                    any()
                )
            ).thenReturn(mockIntent)
            doNothing().`when`(mockContext).unregisterReceiver(any())

            val collectedItems = mutableListOf<String>()
            val batteryStatusFlow = sut.batteryStatus.onEach { collectedItems.add(it) }
            val job = launch { batteryStatusFlow.collect() }
            advanceUntilIdle() // wait for callbackFlow builder to call registerReceiver

            repeat(5) { _ ->
                broadcastReceiverCaptor.value.onReceive(
                    mockContext,
                    mockIntent,
                )
            }

            advanceUntilIdle() // wait flow collection to end
            job.cancel()
            advanceUntilIdle() // wait for awaitClose

            verify(mockContext).registerReceiver(
                eq(broadcastReceiverCaptor.value),
                any(IntentFilter::class.java)
            )
            verify(mockContext).unregisterReceiver(eq(broadcastReceiverCaptor.value))

            val expectedResultList = listOf(
                BatteryStatusConverter.BATTERY_STATUS_UNKNOWN,
                BatteryStatusConverter.BATTERY_STATUS_UNKNOWN,
                BatteryStatusConverter.BATTERY_STATUS_CHARGING,
                BatteryStatusConverter.BATTERY_STATUS_DISCHARGING,
                BatteryStatusConverter.BATTERY_STATUS_NOT_CHARGING,
                BatteryStatusConverter.BATTERY_STATUS_FULL,
            )

            assertEquals(expectedResultList, collectedItems.toList())
        }
}