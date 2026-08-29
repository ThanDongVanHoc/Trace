import 'dart:typed_data';

import 'trace_models.dart';
import 'trace_result.dart';

final class ObjectDraft {
  const ObjectDraft({
    required this.objectId,
    required this.tag,
    required this.reference,
  });

  final String objectId;
  final String tag;
  final ObjectReference reference;
}

abstract interface class ObjectStore {
  Future<TraceResult<ObjectReference>> create(ObjectDraft draft);
  Future<TraceResult<ObjectReference>> get(String objectId);
  Future<TraceResult<List<ObjectReference>>> getAllReferences();
  Future<TraceResult<void>> delete(String objectId);
}

abstract interface class SightingStore {
  Future<TraceResult<Sighting>> insert(Sighting sighting);
  Future<TraceResult<Sighting>> update(Sighting sighting);
  Future<TraceResult<Sighting?>> getLatest(String objectId);
  Future<TraceResult<List<Sighting>>> getTimeline(
    String objectId, {
    int limit = 50,
  });
}

enum SecureAssetType { referenceImage, sightingEvidence }

final class SecureAsset {
  const SecureAsset({
    required this.assetId,
    required this.type,
    required this.mimeType,
    required this.createdAt,
  });

  final String assetId;
  final SecureAssetType type;
  final String mimeType;
  final DateTime createdAt;
}

abstract interface class SecureAssetStore {
  Future<TraceResult<SecureAsset>> write({
    required SecureAssetType type,
    required Uint8List plaintext,
    required String mimeType,
  });

  Future<TraceResult<Uint8List>> read(String assetId);
  Future<TraceResult<void>> delete(String assetId);
}
