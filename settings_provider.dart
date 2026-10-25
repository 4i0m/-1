import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

import '../data/settings_repository.dart';
import '../domain/keyboard_settings.dart';

class SettingsProvider extends ChangeNotifier {
  SettingsProvider(this._repo) : _settings = _repo.load();

  final SettingsRepository _repo;
  KeyboardSettings _settings;
  KeyboardSettings get settings => _settings;

  Future<void> _apply(KeyboardSettings s, {bool persist = true}) async {
    _settings = s;
    notifyListeners();
    if (persist) await _repo.save(s);
  }

  Future<void> setTheme(String id) => _apply(_settings.copyWith(themeId: id));

  /// Call with persist:false while dragging, then once with persist:true.
  Future<void> setBlur(int v, {bool persist = true}) =>
      _apply(_settings.copyWith(blur: v.clamp(0, 30)), persist: persist);

  Future<void> setBlurPopups(bool v) => _apply(_settings.copyWith(blurPopups: v));
  Future<void> setHaptics(bool v) => _apply(_settings.copyWith(haptics: v));

  Future<bool> pickBackground() async {
    try {
      final x = await ImagePicker().pickImage(
        source: ImageSource.gallery,
        maxWidth: 1440,
        imageQuality: 88,
      );
      if (x == null) return false;
      final dir = await getApplicationSupportDirectory(); // same app sandbox as the IME
      final dest = File('${dir.path}/kb_bg_${DateTime.now().millisecondsSinceEpoch}.jpg');
      await File(x.path).copy(dest.path);
      final old = _settings.bgImagePath;
      await _apply(_settings.copyWith(bgImagePath: dest.path));
      await _deleteQuietly(old);
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<void> removeBackground() async {
    final old = _settings.bgImagePath;
    await _apply(_settings.copyWith(removeImage: true));
    await _deleteQuietly(old);
  }

  Future<void> _deleteQuietly(String? path) async {
    if (path == null) return;
    try {
      final f = File(path);
      if (await f.exists()) await f.delete();
    } catch (_) {}
  }
}
