import 'package:flutter/material.dart';
import 'package:trace_mobile/features/find/presentation/find_screen.dart';
import 'package:trace_mobile/features/home/presentation/home_screen.dart';
import 'package:trace_mobile/features/profile/presentation/profile_screen.dart';
import 'package:trace_mobile/features/scan/presentation/scan_screen.dart';

class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  var _index = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: switch (_index) {
        0 => const HomeScreen(),
        1 => const ScanScreen(),
        2 => const FindScreen(),
        _ => const ProfileScreen(),
      },
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (value) => setState(() => _index = value),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Trang chủ',
          ),
          NavigationDestination(
            icon: Icon(Icons.camera_alt_outlined),
            selectedIcon: Icon(Icons.camera_alt),
            label: 'Scan',
          ),
          NavigationDestination(icon: Icon(Icons.search), label: 'Tìm'),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'Tài khoản',
          ),
        ],
      ),
    );
  }
}
