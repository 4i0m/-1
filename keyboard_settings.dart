class KeyboardSettings {
  const KeyboardSettings({
    this.themeId = 'ios_auto',
    this.blur = 12,
    this.blurPopups = true,
    this.haptics = true,
    this.bgImagePath,
  });

  final String themeId;
  final int blur; // 0..30
  final bool blurPopups;
  final bool haptics;
  final String? bgImagePath;

  KeyboardSettings copyWith({
    String? themeId,
    int? blur,
    bool? blurPopups,
    bool? haptics,
    String? bgImagePath,
    bool removeImage = false,
  }) {
    return KeyboardSettings(
      themeId: themeId ?? this.themeId,
      blur: blur ?? this.blur,
      blurPopups: blurPopups ?? this.blurPopups,
      haptics: haptics ?? this.haptics,
      bgImagePath: removeImage ? null : (bgImagePath ?? this.bgImagePath),
    );
  }
}
