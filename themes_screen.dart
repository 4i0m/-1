import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/theme/kb_themes.dart';
import '../settings_provider.dart';
import '../widgets/glass.dart';
import '../widgets/keyboard_preview.dart';

class ThemesScreen extends StatelessWidget {
  const ThemesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SettingsProvider>();
    final s = provider.settings;
    final theme = Theme.of(context);
    final titleStyle = theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700);

    return GlassScaffold(
      title: 'المظهر والثيمات',
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
        children: [
          const KeyboardPreview(),
          const SizedBox(height: 20),
          Text('الثيمات', style: titleStyle),
          const SizedBox(height: 10),
          GridView.count(
            crossAxisCount: 3,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 10,
            crossAxisSpacing: 10,
            childAspectRatio: 1,
            children: [
              for (final p in kPresets)
                _ThemeTile(
                  preset: p,
                  selected: p.id == s.themeId,
                  onTap: () => provider.setTheme(p.id),
                ),
            ],
          ),
          const SizedBox(height: 20),
          GlassCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('الخلفية والزجاج', style: titleStyle),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    FilledButton.tonalIcon(
                      onPressed: () async {
                        final ok = await provider.pickBackground();
                        if (!ok && context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('تعذّر اختيار الصورة')),
                          );
                        }
                      },
                      icon: const Icon(Icons.image_rounded),
                      label: const Text('اختيار صورة خلفية'),
                    ),
                    if (s.bgImagePath != null)
                      TextButton.icon(
                        onPressed: provider.removeBackground,
                        icon: const Icon(Icons.delete_outline_rounded),
                        label: const Text('إزالة الصورة'),
                      ),
                  ],
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    const Text('قوة التضبيب'),
                    const Spacer(),
                    Text('${s.blur}px', style: const TextStyle(fontWeight: FontWeight.w700)),
                  ],
                ),
                Slider(
                  min: 0,
                  max: 30,
                  divisions: 30,
                  value: s.blur.toDouble(),
                  label: '${s.blur}',
                  onChanged: (v) => provider.setBlur(v.round(), persist: false),
                  onChangeEnd: (v) => provider.setBlur(v.round()),
                ),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('معاينة وقوائم مضببة'),
                  value: s.blurPopups,
                  onChanged: provider.setBlurPopups,
                ),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('اهتزاز عند الضغط'),
                  value: s.haptics,
                  onChanged: provider.setHaptics,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ThemeTile extends StatelessWidget {
  const _ThemeTile({required this.preset, required this.selected, required this.onTap});

  final ThemePreset preset;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          gradient: LinearGradient(
            begin: Alignment.topRight,
            end: Alignment.bottomLeft,
            colors: preset.colors,
          ),
          border: Border.all(
            color: selected ? Colors.white : const Color.fromRGBO(255, 255, 255, 0.25),
            width: selected ? 3 : 1,
          ),
        ),
        padding: const EdgeInsets.all(8),
        child: Stack(
          children: [
            if (selected)
              const Positioned(
                top: 0,
                left: 0,
                child: Icon(Icons.check_circle_rounded, color: Colors.white, size: 20),
              ),
            Align(
              alignment: Alignment.bottomCenter,
              child: Text(
                preset.nameAr,
                textAlign: TextAlign.center,
                maxLines: 2,
                style: TextStyle(
                  color: preset.id == 'ios_light' || preset.id == 'ios_auto'
                      ? const Color(0xFF111111)
                      : Colors.white,
                  fontWeight: FontWeight.w700,
                  fontSize: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
