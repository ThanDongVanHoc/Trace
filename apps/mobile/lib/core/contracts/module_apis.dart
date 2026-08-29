import 'trace_models.dart';
import 'trace_result.dart';

final class EnrollRequest {
  const EnrollRequest({
    required this.tag,
    required this.image,
    required this.roi,
  });

  final String tag;
  final ImageInput image;
  final NormalizedRect roi;
}

final class EnrollResponse {
  const EnrollResponse({
    required this.objectId,
    required this.referenceId,
    required this.qualityScore,
    required this.embeddingCount,
    required this.warnings,
  });

  final String objectId;
  final String referenceId;
  final double qualityScore;
  final int embeddingCount;
  final List<String> warnings;
}

abstract interface class EnrollmentApi {
  Future<TraceResult<EnrollResponse>> enroll(EnrollRequest request);
}

abstract interface class VisualEncoder {
  Future<TraceResult<VisualEmbedding>> encode(
    ImageInput image, {
    NormalizedRect? roi,
  });
}

final class RecognizeRequest {
  const RecognizeRequest({
    required this.image,
    required this.references,
    this.minimumSimilarity = 0.75,
    this.maximumResults = 5,
  });

  final ImageInput image;
  final List<ObjectReference> references;
  final double minimumSimilarity;
  final int maximumResults;
}

final class RecognizeResponse {
  const RecognizeResponse({
    required this.detections,
    required this.processingTime,
    required this.modelVersion,
  });

  final List<ObjectDetection> detections;
  final Duration processingTime;
  final String modelVersion;
}

abstract interface class RecognitionApi {
  Future<TraceResult<RecognizeResponse>> recognize(RecognizeRequest request);
}

final class RecordSightingRequest {
  const RecordSightingRequest({
    required this.objectId,
    required this.detectedAt,
    required this.confidence,
    required this.boundingBox,
    required this.location,
    required this.evidenceImage,
  });

  final String objectId;
  final DateTime detectedAt;
  final double confidence;
  final NormalizedRect? boundingBox;
  final GeoFix? location;
  final ImageInput? evidenceImage;
}

final class RecordSightingResponse {
  const RecordSightingResponse({
    required this.sightingId,
    required this.created,
    required this.deduplicatedWith,
  });

  final String sightingId;
  final bool created;
  final String? deduplicatedWith;
}

final class FindLastSeenResponse {
  const FindLastSeenResponse({
    required this.objectId,
    required this.tag,
    required this.lastSeen,
  });

  final String objectId;
  final String tag;
  final Sighting? lastSeen;
}

abstract interface class MemoryApi {
  Future<TraceResult<RecordSightingResponse>> recordSighting(
    RecordSightingRequest request,
  );

  Future<TraceResult<FindLastSeenResponse>> findLastSeen(String objectId);

  Future<TraceResult<List<Sighting>>> getTimeline(
    String objectId, {
    int limit = 50,
  });
}
