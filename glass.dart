import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/theme/kb_themes.dart';
import '../settings_provider.dart';

class GlassCard extends StatelessWidget {
  const GlassCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.radius = 24,
    this.sigma = 18,
  });

  final Widget child;
  final EdgeInsets padding;
  final double radius;
  final double sigma;

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    final tint = dark ? const Color.fromRGBO(255, 255, 255, 0.12) : const Color.fromRGBO(255, 255, 255, 0.55);
    final edge = dark ? const Color.fromRGBO(255, 255, 255, 0.25) : const Color.fromRGBO(255, 255, 255, 0.8);
    final r = BorderRadius.circular(radius);
    return ClipRRect(
      borderRadius: r,
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: sigma, sigmaY: sigma),
        child: Container(
          padding: padding,
          decoration: BoxDecoration(
            color: tint,
            borderRadius: r,
            border: Border.all(color: edge, width: 1),
          ),
          child: child,
        ),
      ),
    );
  }
}

class GlassScaffold extends StatelessWidget {
  const GlassScaffold({super.key, required this.title, required this.body});

  final String title;
  final Widget body;

  @override
  Widget build(BuildContext context) {
    final id = context.watch<SettingsProvider>().settings.themeId;
    final preset = resolvePreset(id, Theme.of(context).brightness);
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topRight,
          end: Alignment.bottomLeft,
          colors: preset.colors,
        ),
      ),
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          elevation: 0,
          scrolledUnderElevation: 0,
          centerTitle: true,
          title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 22)),
        ),
        body: SafeArea(child: body),
      ),
    );
  }
}
