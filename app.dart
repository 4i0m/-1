import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:provider/provider.dart';

import 'core/theme/kb_themes.dart';
import 'presentation/screens/home_screen.dart';
import 'presentation/settings_provider.dart';

class KeyboardProApp extends StatefulWidget {
  const KeyboardProApp({super.key});

  @override
  State<KeyboardProApp> createState() => _KeyboardProAppState();
}

class _KeyboardProAppState extends State<KeyboardProApp> with WidgetsBindingObserver {
  Brightness _system = WidgetsBinding.instance.platformDispatcher.platformBrightness;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangePlatformBrightness() {
    setState(() => _system = WidgetsBinding.instance.platformDispatcher.platformBrightness);
  }

  @override
  Widget build(BuildContext context) {
    final s = context.watch<SettingsProvider>().settings;
    final preset = resolvePreset(s.themeId, _system);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'كيبورد برو',
      // Arabic locale => the whole app is RTL.
      locale: const Locale('ar'),
      supportedLocales: const [Locale('ar')],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: ThemeData(
        useMaterial3: true,
        brightness: preset.dark ? Brightness.dark : Brightness.light,
        colorSchemeSeed: preset.accent,
      ),
      home: const HomeScreen(),
    );
  }
}
