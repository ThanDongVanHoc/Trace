import 'dart:math' as math;
import 'dart:typed_data';

import 'package:trace_mobile/core/contracts/contracts.dart';

/// Contract-compatible baseline only. Member 2 replaces this with TFLite.
final class PrototypeVisualEngine implements VisualEncoder, RecognitionApi {
  static const _dimensions = 64;

  @override
  Future<TraceResult<VisualEmbedding>> encode(
    ImageInput image, {
    NormalizedRect? roi,
  }) async {
    if (image.jpegBytes.isEmpty) {
      return const TraceFailure(TraceErrorCode.invalidImage, 'Image is empty');
    }
    final values = Float32List(_dimensions);
    for (var index = 0; index < image.jpegBytes.length; index += 1) {
      values[index % _dimensions] += image.jpegBytes[index] / 255;
    }
    final norm = math.sqrt(
      values.fold<double>(0, (sum, value) => sum + value * value),
    );
    if (norm > 0) {
      for (var index = 0; index < values.length; index += 1) {
        values[index] /= norm;
      }
    }
    return TraceSuccess(
      VisualEmbedding(
        values: values,
        modelName: 'prototype-byte-histogram',
        modelVersion: '0.0.1-not-for-evaluation',
      ),
    );
  }

  @override
  Future<TraceResult<RecognizeResponse>> recognize(
    RecognizeRequest request,
  ) async {
    if (request.references.isEmpty) {
      return const TraceFailure(
        TraceErrorCode.noReferences,
        'No registered objects',
      );
    }
    final stopwatch = Stopwatch()..start();
    final encoded = await encode(request.image);
    if (encoded case TraceFailure<VisualEmbedding>(
      :final code,
      :final message,
    )) {
      return TraceFailure(code, message);
    }
    final query = (encoded as TraceSuccess<VisualEmbedding>).value;
    final candidates = <ObjectDetection>[];
    for (final reference in request.references) {
      final best = reference.embeddings
          .map((embedding) => _cosine(query.values, embedding.values))
          .fold<double>(0, math.max);
      candidates.add(
        ObjectDetection(
          objectId: best >= request.minimumSimilarity
              ? reference.objectId
              : null,
          boundingBox: null,
          similarity: best,
          status: best >= request.minimumSimilarity
              ? MatchStatus.matched
              : MatchStatus.unknown,
        ),
      );
    }
    candidates.sort((a, b) => b.similarity.compareTo(a.similarity));
    stopwatch.stop();
    return TraceSuccess(
      RecognizeResponse(
        detections: candidates
            .take(request.maximumResults)
            .toList(growable: false),
        processingTime: stopwatch.elapsed,
        modelVersion: query.modelVersion,
      ),
    );
  }

  double _cosine(Float32List left, Float32List right) {
    final length = math.min(left.length, right.length);
    var score = 0.0;
    for (var index = 0; index < length; index += 1) {
      score += left[index] * right[index];
    }
    return score.clamp(0, 1);
  }
}
