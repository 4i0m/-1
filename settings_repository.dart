import 'package:shared_preferences/shared_preferences.dart';

import '../domain/keyboard_settings.dart';

/// Keys are read natively by KeyboardProService.kt as "flutter.<key>".
/// Do NOT store `blur` as a double (the plugin encodes doubles as strings).
class SettingsRepository {
  SettingsRepository(this._p);
  final SharedPreferences _p;

  static Future<SettingsRepository> create() async =>
      SettingsRepository(await SharedPreferences.getInstance());

  KeyboardSettings load() => KeyboardSettings(
        themeId: _p.getString('theme') ?? 'ios_auto',
        blur: (_p.getInt('blur') ?? 12).clamp(0, 30),
        blurPopups: _p.getBool('blurPopups') ?? true,
        haptics: _p.getBool('haptics') ?? true,
        bgImagePath: _p.getString('bgImage'),
      );

  Future<void> save(KeyboardSettings s) async {
    await _p.setString('theme', s.themeId);
    await _p.setInt('blur', s.blur);
    await _p.setBool('blurPopups', s.blurPopups);
    await _p.setBool('haptics', s.haptics);
    final path = s.bgImagePath;
    if (path == null) {
      await _p.remove('bgImage');
    } else {
      await _p.setString('bgImage', path);
    }
  }
}
