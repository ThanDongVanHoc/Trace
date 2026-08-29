import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/app/providers.dart';
import 'package:trace_mobile/app/trace_theme.dart';
import 'package:trace_mobile/features/auth/presentation/auth_controller.dart';
import 'package:trace_mobile/features/auth/presentation/login_screen.dart';
import 'package:trace_mobile/features/shell/main_shell.dart';

class TraceApp extends ConsumerWidget {
  const TraceApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);
    return MaterialApp(
      title: 'TRACE',
      debugShowCheckedModeBanner: false,
      theme: TraceTheme.light(),
      home: switch (auth.status) {
        AuthStatus.booting => const _BootScreen(),
        AuthStatus.authenticated => const MainShell(),
        AuthStatus.unauthenticated || AuthStatus.busy => const LoginScreen(),
      },
    );
  }
}

class _BootScreen extends StatelessWidget {
  const _BootScreen();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(body: Center(child: CircularProgressIndicator()));
  }
}
