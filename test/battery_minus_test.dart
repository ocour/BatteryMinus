import 'package:flutter_test/flutter_test.dart';
import 'package:battery_minus/battery_minus.dart';
import 'package:battery_minus/battery_minus_platform_interface.dart';
import 'package:battery_minus/battery_minus_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBatteryMinusPlatform
    with MockPlatformInterfaceMixin
    implements BatteryMinusPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final BatteryMinusPlatform initialPlatform = BatteryMinusPlatform.instance;

  test('$MethodChannelBatteryMinus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBatteryMinus>());
  });

  test('getPlatformVersion', () async {
    BatteryMinus batteryMinusPlugin = BatteryMinus();
    MockBatteryMinusPlatform fakePlatform = MockBatteryMinusPlatform();
    BatteryMinusPlatform.instance = fakePlatform;

    expect(await batteryMinusPlugin.getPlatformVersion(), '42');
  });
}
