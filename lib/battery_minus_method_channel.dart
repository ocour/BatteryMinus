import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'battery_minus_platform_interface.dart';

/// An implementation of [BatteryMinusPlatform] that uses method channels.
class MethodChannelBatteryMinus extends BatteryMinusPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('battery_minus');

  @visibleForTesting
  final batteryStatusEventChannel = const EventChannel('battery_minus/battery-status');

  @override
  Future<int?> get capacity async {
    final capacity = await methodChannel.invokeMethod<int>("capacity");
    return capacity;
  }

  @override
  Future<bool?> get isCharging async {
    final isCharging = await methodChannel.invokeMethod<bool>("isCharging");
    return isCharging;
  }

  @override
  Stream<String> get batteryStatusStream async* {
    yield* batteryStatusEventChannel
        .receiveBroadcastStream()
        .asyncMap<String>((status) => status);
  }
}
