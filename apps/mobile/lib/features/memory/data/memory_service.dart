import 'dart:math' as math;

import 'package:trace_mobile/core/contracts/contracts.dart';
import 'package:uuid/uuid.dart';

final class MemoryService implements MemoryApi {
  MemoryService(this._objectStore, this._sightingStore, this._assetStore);

  final ObjectStore _objectStore;
  final SightingStore _sightingStore;
  final SecureAssetStore _assetStore;
  final _uuid = const Uuid();

  @override
  Future<TraceResult<FindLastSeenResponse>> findLastSeen(
    String objectId,
  ) async {
    final object = await _objectStore.get(objectId);
    if (object case TraceFailure<ObjectReference>(
      :final code,
      :final message,
    )) {
      return TraceFailure(code, message);
    }
    final latest = await _sightingStore.getLatest(objectId);
    if (latest case TraceFailure<Sighting?>(:final code, :final message)) {
      return TraceFailure(code, message);
    }
    return TraceSuccess(
      FindLastSeenResponse(
        objectId: objectId,
        tag: (object as TraceSuccess<ObjectReference>).value.tag,
        lastSeen: (latest as TraceSuccess<Sighting?>).value,
      ),
    );
  }

  @override
  Future<TraceResult<List<Sighting>>> getTimeline(
    String objectId, {
    int limit = 50,
  }) => _sightingStore.getTimeline(objectId, limit: limit);

  @override
  Future<TraceResult<RecordSightingResponse>> recordSighting(
    RecordSightingRequest request,
  ) async {
    final existing = await _sightingStore.getLatest(request.objectId);
    if (existing case TraceFailure<Sighting?>(:final code, :final message)) {
      return TraceFailure(code, message);
    }
    final latest = (existing as TraceSuccess<Sighting?>).value;
    final shouldDeduplicate =
        latest != null &&
        request.detectedAt.difference(latest.detectedAt).abs() <
            const Duration(minutes: 2) &&
        _nearby(latest.location, request.location);
    if (shouldDeduplicate) {
      final updated = Sighting(
        sightingId: latest.sightingId,
        objectId: latest.objectId,
        detectedAt: request.detectedAt,
        location: request.location ?? latest.location,
        confidence: math.max(latest.confidence, request.confidence),
        evidenceAssetId: latest.evidenceAssetId,
      );
      final result = await _sightingStore.update(updated);
      if (result case TraceFailure<Sighting>(:final code, :final message)) {
        return TraceFailure(code, message);
      }
      return TraceSuccess(
        RecordSightingResponse(
          sightingId: latest.sightingId,
          created: false,
          deduplicatedWith: latest.sightingId,
        ),
      );
    }

    String? evidenceAssetId;
    if (request.evidenceImage != null) {
      final asset = await _assetStore.write(
        type: SecureAssetType.sightingEvidence,
        plaintext: request.evidenceImage!.jpegBytes,
        mimeType: 'image/jpeg',
      );
      if (asset case TraceSuccess<SecureAsset>(:final value)) {
        evidenceAssetId = value.assetId;
      }
    }
    final sighting = Sighting(
      sightingId: _uuid.v4(),
      objectId: request.objectId,
      detectedAt: request.detectedAt,
      location: request.location,
      confidence: request.confidence,
      evidenceAssetId: evidenceAssetId,
    );
    final inserted = await _sightingStore.insert(sighting);
    if (inserted case TraceFailure<Sighting>(:final code, :final message)) {
      return TraceFailure(code, message);
    }
    return TraceSuccess(
      RecordSightingResponse(
        sightingId: sighting.sightingId,
        created: true,
        deduplicatedWith: null,
      ),
    );
  }

  bool _nearby(GeoFix? first, GeoFix? second) {
    if (first == null || second == null) return true;
    const earthRadiusMeters = 6371000.0;
    final lat1 = first.latitude * math.pi / 180;
    final lat2 = second.latitude * math.pi / 180;
    final deltaLat = (second.latitude - first.latitude) * math.pi / 180;
    final deltaLon = (second.longitude - first.longitude) * math.pi / 180;
    final a =
        math.sin(deltaLat / 2) * math.sin(deltaLat / 2) +
        math.cos(lat1) *
            math.cos(lat2) *
            math.sin(deltaLon / 2) *
            math.sin(deltaLon / 2);
    return earthRadiusMeters * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)) <=
        30;
  }
}
