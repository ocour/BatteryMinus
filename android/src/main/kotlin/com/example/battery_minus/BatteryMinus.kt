package com.example.battery_minus

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryMinus(private val context: Context) {
    @delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    /// Get battery capacity as integer percentage
    val capacity get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    } else {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        val level = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        (level * 100 / scale)
    }

    // Currently BatteryManager.isCharging does NOT work
    val isCharging get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        batteryManager.isCharging
    } else {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        status == BatteryManager.BATTERY_STATUS_CHARGING
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @OptIn(ExperimentalCoroutinesApi::class)
    val batteryStatus: Flow<String> = callbackFlow {
        val batteryBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val convertedStatus = getBatteryStatusAndConvert(intent)
                    trySend(convertedStatus)
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                batteryBroadcastReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                batteryBroadcastReceiver,
                filter
            )
        }

        // Immediately send current battery status
        val convertedStatus = getBatteryStatusAndConvert(intent)
        trySend(convertedStatus)

        awaitClose { context.unregisterReceiver(batteryBroadcastReceiver) }
    }

    private fun getBatteryStatusAndConvert(intent: Intent?): String {
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return BatteryStatusConverter.batteryStatusToString(status)
    }
}