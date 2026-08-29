import 'dart:typed_data';

import 'package:trace_mobile/core/contracts/contracts.dart';
import 'package:uuid/uuid.dart';

final class InMemoryObjectStore implements ObjectStore {
  final Map<String, ObjectReference> _references = {};

  @override
  Future<TraceResult<ObjectReference>> create(ObjectDraft draft) async {
    _references[draft.objectId] = draft.reference;
    return TraceSuccess(draft.reference);
  }

  @override
  Future<TraceResult<void>> delete(String objectId) async {
    _references.remove(objectId);
    return const TraceSuccess(null);
  }

  @override
  Future<TraceResult<ObjectReference>> get(String objectId) async {
    final reference = _references[objectId];
    return reference == null
        ? const TraceFailure(TraceErrorCode.objectNotFound, 'Object not found')
        : TraceSuccess(reference);
  }

  @override
  Future<TraceResult<List<ObjectReference>>> getAllReferences() async {
    return TraceSuccess(List.unmodifiable(_references.values));
  }
}

final class InMemorySightingStore implements SightingStore {
  final Map<String, Sighting> _sightings = {};

  @override
  Future<TraceResult<Sighting?>> getLatest(String objectId) async {
    final values =
        _sightings.values.where((item) => item.objectId == objectId).toList()
          ..sort((a, b) => b.detectedAt.compareTo(a.detectedAt));
    return TraceSuccess(values.firstOrNull);
  }

  @override
  Future<TraceResult<List<Sighting>>> getTimeline(
    String objectId, {
    int limit = 50,
  }) async {
    final values =
        _sightings.values.where((item) => item.objectId == objectId).toList()
          ..sort((a, b) => b.detectedAt.compareTo(a.detectedAt));
    return TraceSuccess(values.take(limit).toList(growable: false));
  }

  @override
  Future<TraceResult<Sighting>> insert(Sighting sighting) async {
    _sightings[sighting.sightingId] = sighting;
    return TraceSuccess(sighting);
  }

  @override
  Future<TraceResult<Sighting>> update(Sighting sighting) async {
    _sightings[sighting.sightingId] = sighting;
    return TraceSuccess(sighting);
  }
}

final class InMemorySecureAssetStore implements SecureAssetStore {
  final _uuid = const Uuid();
  final Map<String, Uint8List> _assets = {};

  @override
  Future<TraceResult<void>> delete(String assetId) async {
    _assets.remove(assetId);
    return const TraceSuccess(null);
  }

  @override
  Future<TraceResult<Uint8List>> read(String assetId) async {
    final bytes = _assets[assetId];
    return bytes == null
        ? const TraceFailure(TraceErrorCode.storageError, 'Asset not found')
        : TraceSuccess(Uint8List.fromList(bytes));
  }

  @override
  Future<TraceResult<SecureAsset>> write({
    required SecureAssetType type,
    required Uint8List plaintext,
    required String mimeType,
  }) async {
    final id = _uuid.v4();
    _assets[id] = Uint8List.fromList(plaintext);
    return TraceSuccess(
      SecureAsset(
        assetId: id,
        type: type,
        mimeType: mimeType,
        createdAt: DateTime.now(),
      ),
    );
  }
}
