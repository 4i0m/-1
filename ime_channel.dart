import 'package:flutter/services.dart';

/// Bridge to MainActivity.kt (Android input-method APIs).
class ImeChannel {
  static const MethodChannel _c = MethodChannel('keyboard_pro/ime');

  static Future<bool> _bool(String m) async {
    try {
      return (await _c.invokeMethod<bool>(m)) ?? false;
    } on PlatformException {
      return false;
    } on MissingPluginException {
      return false;
    }
  }

  static Future<bool> isEnabled() => _bool('isEnabled');
  static Future<bool> isSelected() => _bool('isSelected');

  static Future<void> openSettings() async {
    try {
      await _c.invokeMethod<void>('openSettings');
    } on PlatformException {
      // ignored
    } on MissingPluginException {
      // ignored
    }
  }

  static Future<void> showPicker() async {
    try {
      await _c.invokeMethod<void>('showPicker');
    } on PlatformException {
      // ignored
    } on MissingPluginException {
      // ignored
    }
  }
}
