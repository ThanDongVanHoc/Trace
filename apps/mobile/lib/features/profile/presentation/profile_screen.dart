import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/app/providers.dart';

class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  bool _enablingNotifications = false;

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(authControllerProvider).session!;
    return Scaffold(
      appBar: AppBar(title: const Text('Tài khoản')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 28,
                    child: Text(
                      session.user.displayName.characters.first.toUpperCase(),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          session.user.displayName,
                          style: Theme.of(context).textTheme.titleLarge
                              ?.copyWith(fontWeight: FontWeight.bold),
                        ),
                        Text(session.user.email),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.notifications_active_outlined),
                  title: const Text('Bật thông báo'),
                  subtitle: const Text('Đăng ký thiết bị với FCM/APNs'),
                  trailing: _enablingNotifications
                      ? const CircularProgressIndicator()
                      : const Icon(Icons.chevron_right),
                  onTap: _enablingNotifications ? null : _enableNotifications,
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.lock_outline),
                  title: const Text('Kho dữ liệu bảo mật'),
                  subtitle: const Text('Keystore/Keychain và AES-256-GCM'),
                  trailing: const Icon(Icons.verified_user_outlined),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: () => ref.read(authControllerProvider.notifier).logout(),
            icon: const Icon(Icons.logout),
            label: const Text('Đăng xuất'),
          ),
        ],
      ),
    );
  }

  Future<void> _enableNotifications() async {
    setState(() => _enablingNotifications = true);
    final session = ref.read(authControllerProvider).session!;
    final enabled = await ref
        .read(pushNotificationProvider)
        .enable(session.accessToken);
    if (enabled) {
      try {
        await ref
            .read(dioProvider)
            .post<void>(
              '/notifications/test',
              options: Options(
                headers: {'Authorization': 'Bearer ${session.accessToken}'},
              ),
            );
      } catch (_) {}
    }
    if (!mounted) return;
    setState(() => _enablingNotifications = false);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          enabled
              ? 'Đã bật thông báo và gửi một thông báo thử.'
              : 'Chưa thể bật. Hãy cấu hình Firebase và kiểm tra quyền.',
        ),
      ),
    );
  }
}
