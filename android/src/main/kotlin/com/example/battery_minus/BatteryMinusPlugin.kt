package com.example.battery_minus

import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BatteryMinusPlugin */
class BatteryMinusPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var methodChannel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var applicationContext: Context
  private lateinit var batteryMinus: BatteryMinus
  private lateinit var batteryMinusStreamHandler: BatteryMinusStreamHandler

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    batteryMinus = BatteryMinus(applicationContext)
    batteryMinusStreamHandler = BatteryMinusStreamHandler(batteryMinus)

    methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "battery_minus")
    methodChannel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "battery_minus/battery-status")
    eventChannel.setStreamHandler(batteryMinusStreamHandler)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method) {
      "capacity" -> result.success(batteryMinus.capacity)
      "isCharging" -> result.success(batteryMinus.isCharging)
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }
}
