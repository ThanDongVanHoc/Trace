import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/app/providers.dart';
import 'package:trace_mobile/core/contracts/contracts.dart';

class FindScreen extends ConsumerWidget {
  const FindScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.watch(objectRevisionProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Tìm đồ vật')),
      body: FutureBuilder<TraceResult<List<ObjectReference>>>(
        future: ref.read(objectStoreProvider).getAllReferences(),
        builder: (context, snapshot) {
          final result = snapshot.data;
          if (result == null) {
            return const Center(child: CircularProgressIndicator());
          }
          if (result is! TraceSuccess<List<ObjectReference>> ||
              result.value.isEmpty) {
            return const Center(child: Text('Chưa có đồ vật để tìm.'));
          }
          return ListView.separated(
            padding: const EdgeInsets.all(20),
            itemCount: result.value.length,
            separatorBuilder: (_, _) => const SizedBox(height: 10),
            itemBuilder: (context, index) {
              final item = result.value[index];
              return Card(
                child: ListTile(
                  leading: const CircleAvatar(
                    child: Icon(Icons.inventory_2_outlined),
                  ),
                  title: Text(item.tag),
                  subtitle: const Text('Xem vị trí được nhìn thấy gần nhất'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showLastSeen(context, ref, item.objectId),
                ),
              );
            },
          );
        },
      ),
    );
  }

  Future<void> _showLastSeen(
    BuildContext context,
    WidgetRef ref,
    String objectId,
  ) async {
    final result = await ref.read(memoryApiProvider).findLastSeen(objectId);
    if (!context.mounted) return;
    if (result is! TraceSuccess<FindLastSeenResponse>) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Không thể đọc lịch sử.')));
      return;
    }
    final lastSeen = result.value.lastSeen;
    await showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(result.value.tag),
        content: lastSeen == null
            ? const Text('Chưa có lần nhận diện nào sau khi gắn tag.')
            : Text(
                'Lần cuối: ${lastSeen.detectedAt.toLocal()}\n'
                '${lastSeen.location == null ? 'Không có vị trí' : 'Vị trí: ${lastSeen.location!.latitude.toStringAsFixed(5)}, ${lastSeen.location!.longitude.toStringAsFixed(5)}'}\n'
                'Độ tin cậy: ${(lastSeen.confidence * 100).round()}%',
              ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Đóng'),
          ),
        ],
      ),
    );
  }
}
