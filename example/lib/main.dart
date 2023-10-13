import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:battery_minus/battery_minus.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _batteryMinusPlugin = BatteryMinus();
  int? _capacity;
  bool? _isCharging;
  late Stream<String> _batteryStatus;

  @override
  void initState() {
    super.initState();
    _fetchBatteryInfo();
    _batteryStatus = _batteryMinusPlugin.batteryStatusStream.take(4);
  }

  Future<void> _fetchBatteryInfo() async {
    int? capacity;
    bool? isCharging;

    try {
      capacity = await _batteryMinusPlugin.capacity;
    } on PlatformException {
      // ignore: avoid_print
      print("Fetching capacity failed.");
    }

    try {
      isCharging = await _batteryMinusPlugin.isCharging;
    } on PlatformException {
      // ignore: avoid_print
      print("Fetching isCharging failed.");
    }

    if (!mounted) return;

    setState(() {
      _capacity = capacity;
      _isCharging = isCharging;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Capacity: $_capacity'),
              Text('isCharging: $_isCharging'),
              StreamBuilder(
                  stream: _batteryStatus,
                  builder: (context, snapshot) {
                    switch(snapshot.connectionState) {
                      case ConnectionState.none:
                      case ConnectionState.waiting:
                        return const CircularProgressIndicator();
                      case ConnectionState.active:
                        if(snapshot.hasData) {
                          return Text("Current status: ${snapshot.data}");
                        } else {
                          // else failed
                          return const Text("Fetching battery status failed.");
                        }
                      case ConnectionState.done:
                        return Text("Current status: ${snapshot.data} (Done)");
                    }
                  },
              ),
              const SizedBox(height: 8.0),
              ElevatedButton(
                onPressed: () {
                  _fetchBatteryInfo();
                },
                child: const Text("Refresh info"),
              )
            ],
          ),
        ),
      ),
    );
  }
}
