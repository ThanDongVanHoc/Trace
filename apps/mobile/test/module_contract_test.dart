import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:trace_mobile/core/contracts/contracts.dart';
import 'package:trace_mobile/features/enrollment/data/enrollment_service.dart';
import 'package:trace_mobile/features/memory/data/memory_service.dart';
import 'package:trace_mobile/features/recognition/data/prototype_visual_engine.dart';
import 'package:trace_mobile/features/secure_vault/data/in_memory_stores.dart';

void main() {
  group('module contract baseline', () {
    late InMemoryObjectStore objects;
    late InMemorySightingStore sightings;
    late InMemorySecureAssetStore assets;
    late EnrollmentService enrollment;
    late MemoryService memory;

    setUp(() {
      objects = InMemoryObjectStore();
      sightings = InMemorySightingStore();
      assets = InMemorySecureAssetStore();
      enrollment = EnrollmentService(PrototypeVisualEngine(), objects, assets);
      memory = MemoryService(objects, sightings, assets);
    });

    test('enrollment output can be read by downstream modules', () async {
      final result = await enrollment.enroll(
        EnrollRequest(
          tag: 'Balô của tôi',
          image: _image(1),
          roi: const NormalizedRect(
            left: 0.1,
            top: 0.1,
            right: 0.9,
            bottom: 0.9,
          ),
        ),
      );

      expect(result, isA<TraceSuccess<EnrollResponse>>());
      final references = await objects.getAllReferences();
      expect(
        (references as TraceSuccess<List<ObjectReference>>).value,
        hasLength(1),
      );
    });

    test('memory deduplicates sightings inside the time window', () async {
      final enrolled = await enrollment.enroll(
        EnrollRequest(
          tag: 'Laptop',
          image: _image(2),
          roi: const NormalizedRect(left: 0, top: 0, right: 1, bottom: 1),
        ),
      );
      final objectId =
          (enrolled as TraceSuccess<EnrollResponse>).value.objectId;
      final firstTime = DateTime.utc(2026, 8, 29, 10);

      final first = await memory.recordSighting(
        _sightingRequest(objectId, firstTime, 0.8),
      );
      final second = await memory.recordSighting(
        _sightingRequest(
          objectId,
          firstTime.add(const Duration(seconds: 30)),
          0.9,
        ),
      );

      expect(
        (first as TraceSuccess<RecordSightingResponse>).value.created,
        isTrue,
      );
      expect(
        (second as TraceSuccess<RecordSightingResponse>).value.created,
        isFalse,
      );
      final timeline = await memory.getTimeline(objectId);
      expect((timeline as TraceSuccess<List<Sighting>>).value, hasLength(1));
    });
  });
}

ImageInput _image(int seed) {
  return ImageInput(
    jpegBytes: Uint8List.fromList(
      List.generate(1024, (index) => (index + seed) % 255),
    ),
    width: 32,
    height: 32,
    rotationDegrees: 0,
    capturedAt: DateTime.utc(2026, 8, 29),
  );
}

RecordSightingRequest _sightingRequest(
  String objectId,
  DateTime detectedAt,
  double confidence,
) {
  return RecordSightingRequest(
    objectId: objectId,
    detectedAt: detectedAt,
    confidence: confidence,
    boundingBox: null,
    location: GeoFix(
      latitude: 10.7769,
      longitude: 106.7009,
      accuracyMeters: 8,
      capturedAt: DateTime.utc(2026, 8, 29),
    ),
    evidenceImage: null,
  );
}
