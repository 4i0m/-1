import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/theme/kb_themes.dart';
import '../settings_provider.dart';

/// Lightweight mock of the real keyboard so changes are visible instantly.
class KeyboardPreview extends StatelessWidget {
  const KeyboardPreview({super.key});

  static const _rows = ['ضصثقفغعه', 'شسيبلاتن', 'ئءؤرىةوز'];

  @override
  Widget build(BuildContext context) {
    final s = context.watch<SettingsProvider>().settings;
    final preset = resolvePreset(s.themeId, Theme.of(context).brightness);
    final path = s.bgImagePath;
    final hasImage = path != null && File(path).existsSync();
    final sigma = s.blur / 2.0;
    final keyBorder = Border.all(color: const Color.fromRGBO(255, 255, 255, 0.35));

    Widget key(String label, {int flex = 1, Color? fill}) => Expanded(
          flex: flex,
          child: Container(
            margin: const EdgeInsets.all(3),
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: fill ?? preset.keyFill,
              borderRadius: BorderRadius.circular(9),
              border: keyBorder,
            ),
            child: Text(label, style: TextStyle(color: preset.textColor, fontSize: 17)),
          ),
        );

    return AspectRatio(
      aspectRatio: 1.9,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: Stack(
          fit: StackFit.expand,
          children: [
            if (hasImage)
              ImageFiltered(
                imageFilter: ui.ImageFilter.blur(sigmaX: sigma, sigmaY: sigma, tileMode: TileMode.mirror),
                child: Transform.scale(scale: 1.1, child: Image.file(File(path), fit: BoxFit.cover)),
              )
            else
              DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topRight,
                    end: Alignment.bottomLeft,
                    colors: preset.colors,
                  ),
                ),
              ),
            Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                children: [
                  for (final r in _rows)
                    Expanded(child: Row(children: [for (final ch in r.split('')) key(ch)])),
                  Expanded(
                    child: Row(children: [
                      key('?123', flex: 2),
                      key('🙂', flex: 1),
                      key('كيبورد برو', flex: 5),
                      key('↵', flex: 2, fill: preset.accent),
                    ]),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
