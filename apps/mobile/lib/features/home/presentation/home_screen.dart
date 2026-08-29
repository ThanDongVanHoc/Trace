import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/app/providers.dart';
import 'package:trace_mobile/core/contracts/contracts.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.watch(objectRevisionProvider);
    final user = ref.watch(authControllerProvider).session!.user;
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'TRACE',
          style: TextStyle(fontWeight: FontWeight.w800),
        ),
        actions: const [
          Padding(
            padding: EdgeInsets.all(12),
            child: Icon(Icons.notifications_none),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async =>
            ref.read(objectRevisionProvider.notifier).state++,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              'Chào ${user.displayName} 👋',
              style: Theme.of(context).textTheme.headlineSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 6),
            const Text('Bạn đang muốn tìm gì?'),
            const SizedBox(height: 24),
            Text(
              'Đồ vật đã ghi nhớ',
              style: Theme.of(context).textTheme.titleLarge
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            FutureBuilder<TraceResult<List<ObjectReference>>>(
              future: ref.read(objectStoreProvider).getAllReferences(),
              builder: (context, snapshot) {
                final result = snapshot.data;
                if (result == null) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (result case TraceSuccess<List<ObjectReference>>(
                  :final value,
                )) {
                  if (value.isEmpty) {
                    return const _EmptyObjects();
                  }
                  return Column(
                    children: value
                        .map(
                          (item) => Padding(
                            padding: const EdgeInsets.only(bottom: 10),
                            child: Card(
                              child: ListTile(
                                leading: const CircleAvatar(
                                  child: Icon(Icons.inventory_2_outlined),
                                ),
                                title: Text(item.tag),
                                subtitle: Text(
                                  'Độ tin cậy mẫu ${(item.qualityScore * 100).round()}%',
                                ),
                              ),
                            ),
                          ),
                        )
                        .toList(),
                  );
                }
                return const Text('Không thể đọc dữ liệu cục bộ.');
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _EmptyObjects extends StatelessWidget {
  const _EmptyObjects();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Icon(
              Icons.center_focus_strong,
              size: 48,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 12),
            const Text(
              'Chưa có đồ vật nào',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            const Text(
              'Mở tab Scan để chụp và gắn tag đồ vật đầu tiên.',
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
