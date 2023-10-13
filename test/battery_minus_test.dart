import 'package:flutter_test/flutter_test.dart';
import 'package:battery_minus/battery_minus.dart';
import 'package:battery_minus/battery_minus_platform_interface.dart';
import 'package:battery_minus/battery_minus_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBatteryMinusPlatform
    with MockPlatformInterfaceMixin
    implements BatteryMinusPlatform {

  final capacityResults = [100, 99, 98];
  final isChargingResults = [true, false];

  @override
  // TODO: implement batteryStatusStream
  Stream<String> get batteryStatusStream => throw UnimplementedError();

  @override
  Future<int?> get capacity {
    // returns [100, 99, 98] one at a time in order
    return Future.value(capacityResults.removeAt(0));
  }

  @override
  Future<bool?> get isCharging {
    // [true, false]
    return Future.value(isChargingResults.removeAt(0));
  }
}

void main() {
  final BatteryMinusPlatform initialPlatform = BatteryMinusPlatform.instance;

  late BatteryMinus batteryMinusPlugin;
  late MockBatteryMinusPlatform fakePlatform;

  setUp(() {
    batteryMinusPlugin = BatteryMinus();
    fakePlatform = MockBatteryMinusPlatform();
    BatteryMinusPlatform.instance = fakePlatform;
  });

  test('$MethodChannelBatteryMinus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBatteryMinus>());
  });

  test("capacity", () async {
    expect(await batteryMinusPlugin.capacity, 100);
    expect(await batteryMinusPlugin.capacity, 99);
    expect(await batteryMinusPlugin.capacity, 98);
  });

  test("capacity", () async {
    expect(await batteryMinusPlugin.isCharging, true);
    expect(await batteryMinusPlugin.isCharging, false);
  });
}
