import 'package:flutter/material.dart';

import '../../core/platform/ime_channel.dart';
import '../widgets/glass.dart';
import 'themes_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  bool _enabled = false;
  bool _selected = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    final e = await ImeChannel.isEnabled();
    final s = await ImeChannel.isSelected();
    if (!mounted) return;
    setState(() {
      _enabled = e;
      _selected = s;
    });
  }

  @override
  Widget build(BuildContext context) {
    final ok = _enabled;
    final theme = Theme.of(context);
    return GlassScaffold(
      title: 'كيبورد برو',
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
        children: [
          GlassCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 14,
                      height: 14,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: ok ? const Color(0xFF34C759) : const Color(0xFFFF9F0A),
                        boxShadow: [
                          BoxShadow(
                            color: (ok ? const Color(0xFF34C759) : const Color(0xFFFF9F0A)).withAlpha(140),
                            blurRadius: 10,
                            spreadRadius: 2,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        ok ? 'الكيبورد مفعل وجاهز' : 'الكيبورد غير مفعل',
                        style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  ok
                      ? (_selected ? 'كيبورد برو هو الكيبورد الحالي.' : 'فعّلته بنجاح — اختره من قائمة لوحات المفاتيح للبدء.')
                      : 'فعّل كيبورد برو من إعدادات النظام ثم ارجع إلى التطبيق.',
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    if (!ok)
                      FilledButton.icon(
                        onPressed: ImeChannel.openSettings,
                        icon: const Icon(Icons.settings_rounded),
                        label: const Text('تفعيل الكيبورد'),
                      ),
                    if (ok && !_selected)
                      FilledButton.tonalIcon(
                        onPressed: ImeChannel.showPicker,
                        icon: const Icon(Icons.keyboard_rounded),
                        label: const Text('اختيار الكيبورد'),
                      ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          GlassCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('جرّب الكتابة',
                    style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 12),
                const TextField(
                  minLines: 3,
                  maxLines: 6,
                  textInputAction: TextInputAction.newline,
                  decoration: InputDecoration(
                    hintText: 'اكتب هنا لتجربة الكيبورد...',
                    border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(16))),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          GlassCard(
            padding: EdgeInsets.zero,
            child: ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
              leading: const Icon(Icons.palette_rounded),
              title: const Text('المظهر والثيمات', style: TextStyle(fontWeight: FontWeight.w700)),
              subtitle: const Text('الألوان، الخلفية، قوة التضبيب'),
              trailing: const Icon(Icons.chevron_left_rounded),
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute<void>(builder: (_) => const ThemesScreen()),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
