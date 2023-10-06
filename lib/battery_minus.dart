
import 'battery_minus_platform_interface.dart';

class BatteryMinus {
  Future<String?> getPlatformVersion() {
    return BatteryMinusPlatform.instance.getPlatformVersion();
  }
}
