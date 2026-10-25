import 'package:flutter/material.dart';

/// Must stay in sync with KbThemes.kt (same ids).
class ThemePreset {
  const ThemePreset({
    required this.id,
    required this.nameAr,
    required this.colors,
    required this.dark,
    required this.keyFill,
    required this.textColor,
    required this.accent,
  });

  final String id;
  final String nameAr;
  final List<Color> colors;
  final bool dark;
  final Color keyFill;
  final Color textColor;
  final Color accent;
}

const _glass = Color(0x38FFFFFF);

const List<ThemePreset> kPresets = [
  ThemePreset(
    id: 'ios_light', nameAr: 'آيفون فاتح',
    colors: [Color(0xFFD3D6DD), Color(0xFFC4C8D0)], dark: false,
    keyFill: Color(0xE6FFFFFF), textColor: Color(0xFF111111), accent: Color(0xFF007AFF),
  ),
  ThemePreset(
    id: 'ios_dark', nameAr: 'آيفون داكن',
    colors: [Color(0xFF2A2A2D), Color(0xFF1B1B1D)], dark: true,
    keyFill: Color(0x55FFFFFF), textColor: Colors.white, accent: Color(0xFF0A84FF),
  ),
  ThemePreset(
    id: 'ios_auto', nameAr: 'آيفون تلقائي',
    colors: [Color(0xFFD3D6DD), Color(0xFF1B1B1D)], dark: false,
    keyFill: Color(0x99FFFFFF), textColor: Color(0xFF111111), accent: Color(0xFF007AFF),
  ),
  ThemePreset(
    id: 'aura', nameAr: 'أورا (متدرج)',
    colors: [Color(0xFFFF6FD8), Color(0xFF3813C2)], dark: true,
    keyFill: _glass, textColor: Colors.white, accent: Color(0xFFFF4D9D),
  ),
  ThemePreset(
    id: 'violet', nameAr: 'بنفسجي',
    colors: [Color(0xFF8B5CF6), Color(0xFF4C1D95)], dark: true,
    keyFill: _glass, textColor: Colors.white, accent: Color(0xFFC084FC),
  ),
  ThemePreset(
    id: 'turquoise', nameAr: 'تركوازي',
    colors: [Color(0xFF2DD4BF), Color(0xFF0F766E)], dark: true,
    keyFill: _glass, textColor: Colors.white, accent: Color(0xFF14B8A6),
  ),
  ThemePreset(
    id: 'emerald', nameAr: 'أخضر زمردي',
    colors: [Color(0xFF34D399), Color(0xFF065F46)], dark: true,
    keyFill: _glass, textColor: Colors.white, accent: Color(0xFF059669),
  ),
  ThemePreset(
    id: 'amber', nameAr: 'برتقالي / عنبري',
    colors: [Color(0xFFFBBF24), Color(0xFFEA580C)], dark: true,
    keyFill: _glass, textColor: Colors.white, accent: Color(0xFFC2410C),
  ),
  ThemePreset(
    id: 'oled', nameAr: 'أسود (OLED)',
    colors: [Color(0xFF000000), Color(0xFF000000)], dark: true,
    keyFill: Color(0x1FFFFFFF), textColor: Color(0xFFEDEDED), accent: Color(0xFF0A84FF),
  ),
];

/// Resolves "ios_auto" to light/dark using the given system brightness.
ThemePreset resolvePreset(String id, Brightness system) {
  var key = id;
  if (key == 'ios_auto') key = system == Brightness.dark ? 'ios_dark' : 'ios_light';
  return kPresets.firstWhere((p) => p.id == key, orElse: () => kPresets.first);
}
