import 'dart:typed_data';

final class ImageInput {
  const ImageInput({
    required this.jpegBytes,
    required this.width,
    required this.height,
    required this.rotationDegrees,
    required this.capturedAt,
  });

  final Uint8List jpegBytes;
  final int width;
  final int height;
  final int rotationDegrees;
  final DateTime capturedAt;
}

final class NormalizedRect {
  const NormalizedRect({
    required this.left,
    required this.top,
    required this.right,
    required this.bottom,
  });

  final double left;
  final double top;
  final double right;
  final double bottom;

  bool get isValid =>
      left >= 0 &&
      top >= 0 &&
      right <= 1 &&
      bottom <= 1 &&
      left < right &&
      top < bottom;
}

final class VisualEmbedding {
  const VisualEmbedding({
    required this.values,
    required this.modelName,
    required this.modelVersion,
  });

  final Float32List values;
  final String modelName;
  final String modelVersion;
}

final class GeoFix {
  const GeoFix({
    required this.latitude,
    required this.longitude,
    required this.accuracyMeters,
    required this.capturedAt,
  });

  final double latitude;
  final double longitude;
  final double accuracyMeters;
  final DateTime capturedAt;
}

final class ObjectReference {
  const ObjectReference({
    required this.referenceId,
    required this.objectId,
    required this.tag,
    required this.imageAssetId,
    required this.roi,
    required this.embeddings,
    required this.qualityScore,
    required this.createdAt,
  });

  final String referenceId;
  final String objectId;
  final String tag;
  final String imageAssetId;
  final NormalizedRect roi;
  final List<VisualEmbedding> embeddings;
  final double qualityScore;
  final DateTime createdAt;
}

enum MatchStatus { matched, unknown }

final class ObjectDetection {
  const ObjectDetection({
    required this.objectId,
    required this.boundingBox,
    required this.similarity,
    required this.status,
  });

  final String? objectId;
  final NormalizedRect? boundingBox;
  final double similarity;
  final MatchStatus status;
}

final class Sighting {
  const Sighting({
    required this.sightingId,
    required this.objectId,
    required this.detectedAt,
    required this.location,
    required this.confidence,
    required this.evidenceAssetId,
  });

  final String sightingId;
  final String objectId;
  final DateTime detectedAt;
  final GeoFix? location;
  final double confidence;
  final String? evidenceAssetId;
}
