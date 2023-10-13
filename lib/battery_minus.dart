import 'battery_minus_platform_interface.dart';

class BatteryMinus {
  Future<int?> get capacity => BatteryMinusPlatform.instance.capacity;

  Future<bool?> get isCharging => BatteryMinusPlatform.instance.isCharging;

  Stream<String> get batteryStatusStream =>
      BatteryMinusPlatform.instance.batteryStatusStream;
}
