import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'battery_minus_platform_interface.dart';

/// An implementation of [BatteryMinusPlatform] that uses method channels.
class MethodChannelBatteryMinus extends BatteryMinusPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('battery_minus');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
