package com.example.battery_minus

import io.flutter.plugin.common.EventChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BatteryMinusStreamHandler(private val batteryMinus: BatteryMinus): EventChannel.StreamHandler {
    // Has to be on Main thread
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var statusJob: Job

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        statusJob = coroutineScope.launch {
            batteryMinus.batteryStatus.collect { status ->
                events?.success(status)
            }
        }
    }

    override fun onCancel(arguments: Any?) {
        // Cancel flow when no listeners
        statusJob.cancel()
    }

}