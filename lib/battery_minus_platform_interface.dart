import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'battery_minus_method_channel.dart';

abstract class BatteryMinusPlatform extends PlatformInterface {
  /// Constructs a BatteryMinusPlatform.
  BatteryMinusPlatform() : super(token: _token);

  static final Object _token = Object();

  static BatteryMinusPlatform _instance = MethodChannelBatteryMinus();

  /// The default instance of [BatteryMinusPlatform] to use.
  ///
  /// Defaults to [MethodChannelBatteryMinus].
  static BatteryMinusPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BatteryMinusPlatform] when
  /// they register themselves.
  static set instance(BatteryMinusPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
