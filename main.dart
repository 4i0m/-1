import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'data/settings_repository.dart';
import 'presentation/settings_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final repo = await SettingsRepository.create();
  runApp(
    ChangeNotifierProvider(
      create: (_) => SettingsProvider(repo),
      child: const KeyboardProApp(),
    ),
  );
}
