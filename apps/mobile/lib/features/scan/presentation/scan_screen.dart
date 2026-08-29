import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:trace_mobile/app/providers.dart';
import 'package:trace_mobile/core/contracts/contracts.dart';

class ScanScreen extends ConsumerStatefulWidget {
  const ScanScreen({super.key});

  @override
  ConsumerState<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends ConsumerState<ScanScreen>
    with WidgetsBindingObserver {
  CameraController? _controller;
  String? _error;
  bool _working = false;

  static const _roi = NormalizedRect(
    left: 0.15,
    top: 0.2,
    right: 0.85,
    bottom: 0.78,
  );

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initializeCamera();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.paused) {
      _controller?.dispose();
      _controller = null;
    } else if (state == AppLifecycleState.resumed && _controller == null) {
      _initializeCamera();
    }
  }

  Future<void> _initializeCamera() async {
    try {
      final cameras = await availableCameras();
      if (cameras.isEmpty) {
        throw StateError('No camera found');
      }
      final controller = CameraController(
        cameras.first,
        ResolutionPreset.medium,
        enableAudio: false,
        imageFormatGroup: ImageFormatGroup.jpeg,
      );
      await controller.initialize();
      if (!mounted) {
        await controller.dispose();
        return;
      }
      setState(() => _controller = controller);
    } catch (_) {
      if (mounted) {
        setState(
          () => _error = 'Không thể mở camera. Hãy kiểm tra quyền truy cập.',
        );
      }
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final controller = _controller;
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        title: const Text('Scan'),
      ),
      body: _error != null
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Text(
                  _error!,
                  style: const TextStyle(color: Colors.white),
                ),
              ),
            )
          : controller == null || !controller.value.isInitialized
          ? const Center(child: CircularProgressIndicator())
          : Stack(
              fit: StackFit.expand,
              children: [
                CameraPreview(controller),
                const Positioned(
                  left: 48,
                  right: 48,
                  top: 100,
                  bottom: 150,
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      border: Border.fromBorderSide(
                        BorderSide(color: Colors.white, width: 2),
                      ),
                      borderRadius: BorderRadius.all(Radius.circular(24)),
                    ),
                  ),
                ),
                Positioned(
                  left: 16,
                  right: 16,
                  bottom: 24,
                  child: SafeArea(
                    child: Row(
                      children: [
                        Expanded(
                          child: FilledButton.tonalIcon(
                            onPressed: _working ? null : _enroll,
                            icon: const Icon(Icons.add_box_outlined),
                            label: const Text('Gắn tag'),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: FilledButton.icon(
                            onPressed: _working ? null : _recognize,
                            icon: _working
                                ? const SizedBox.square(
                                    dimension: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(Icons.center_focus_strong),
                            label: const Text('Nhận diện'),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
    );
  }

  Future<ImageInput?> _capture() async {
    final controller = _controller;
    if (controller == null || !controller.value.isInitialized) {
      return null;
    }
    final file = await controller.takePicture();
    final bytes = await file.readAsBytes();
    final size = controller.value.previewSize;
    return ImageInput(
      jpegBytes: Uint8List.fromList(bytes),
      width: size?.width.round() ?? 0,
      height: size?.height.round() ?? 0,
      rotationDegrees: controller.description.sensorOrientation,
      capturedAt: DateTime.now().toUtc(),
    );
  }

  Future<void> _enroll() async {
    final tag = await _askForTag();
    if (tag == null || !mounted) {
      return;
    }
    await _run(() async {
      final image = await _capture();
      if (image == null) {
        return 'Không thể chụp ảnh.';
      }
      final result = await ref
          .read(enrollmentApiProvider)
          .enroll(EnrollRequest(tag: tag, image: image, roi: _roi));
      if (result case TraceSuccess<EnrollResponse>()) {
        ref.read(objectRevisionProvider.notifier).state++;
        return 'Đã ghi nhớ “$tag”.';
      }
      return 'Không thể ghi nhớ đồ vật.';
    });
  }

  Future<void> _recognize() async {
    await _run(() async {
      final image = await _capture();
      if (image == null) {
        return 'Không thể chụp ảnh.';
      }
      final referencesResult = await ref
          .read(objectStoreProvider)
          .getAllReferences();
      if (referencesResult is! TraceSuccess<List<ObjectReference>> ||
          referencesResult.value.isEmpty) {
        return 'Hãy gắn tag ít nhất một đồ vật trước.';
      }
      final result = await ref
          .read(recognitionApiProvider)
          .recognize(
            RecognizeRequest(image: image, references: referencesResult.value),
          );
      if (result is! TraceSuccess<RecognizeResponse>) {
        return 'Nhận diện thất bại.';
      }
      final matches = result.value.detections.where(
        (item) => item.status == MatchStatus.matched,
      );
      if (matches.isEmpty) {
        return 'Không tìm thấy đồ vật đã ghi nhớ.';
      }
      final match = matches.first;
      final location = await _location();
      await ref
          .read(memoryApiProvider)
          .recordSighting(
            RecordSightingRequest(
              objectId: match.objectId!,
              detectedAt: image.capturedAt,
              confidence: match.similarity,
              boundingBox: match.boundingBox,
              location: location,
              evidenceImage: image,
            ),
          );
      final reference = referencesResult.value.firstWhere(
        (item) => item.objectId == match.objectId,
      );
      return 'Đã thấy “${reference.tag}” (${(match.similarity * 100).round()}%).';
    });
  }

  Future<GeoFix?> _location() async {
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      return null;
    }
    final position = await Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(
        accuracy: LocationAccuracy.high,
        timeLimit: Duration(seconds: 8),
      ),
    );
    return GeoFix(
      latitude: position.latitude,
      longitude: position.longitude,
      accuracyMeters: position.accuracy,
      capturedAt: position.timestamp,
    );
  }

  Future<String?> _askForTag() {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Đặt tên đồ vật'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(hintText: 'Ví dụ: Balô của tôi'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Hủy'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, controller.text.trim()),
            child: const Text('Lưu'),
          ),
        ],
      ),
    );
  }

  Future<void> _run(Future<String> Function() operation) async {
    setState(() => _working = true);
    try {
      final message = await operation();
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Có lỗi xảy ra khi xử lý ảnh.')),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _working = false);
      }
    }
  }
}
