import 'package:trace_mobile/core/contracts/contracts.dart';
import 'package:uuid/uuid.dart';

final class EnrollmentService implements EnrollmentApi {
  EnrollmentService(this._encoder, this._objectStore, this._assetStore);

  final VisualEncoder _encoder;
  final ObjectStore _objectStore;
  final SecureAssetStore _assetStore;
  final _uuid = const Uuid();

  @override
  Future<TraceResult<EnrollResponse>> enroll(EnrollRequest request) async {
    final tag = request.tag.trim();
    if (tag.isEmpty || !request.roi.isValid) {
      return const TraceFailure(
        TraceErrorCode.invalidRoi,
        'A non-empty tag and valid normalized ROI are required',
      );
    }
    final encoded = await _encoder.encode(request.image, roi: request.roi);
    if (encoded case TraceFailure<VisualEmbedding>(
      :final code,
      :final message,
    )) {
      return TraceFailure(code, message);
    }
    final asset = await _assetStore.write(
      type: SecureAssetType.referenceImage,
      plaintext: request.image.jpegBytes,
      mimeType: 'image/jpeg',
    );
    if (asset case TraceFailure<SecureAsset>(:final code, :final message)) {
      return TraceFailure(code, message);
    }

    final objectId = _uuid.v4();
    final referenceId = _uuid.v4();
    final embedding = (encoded as TraceSuccess<VisualEmbedding>).value;
    final storedAsset = (asset as TraceSuccess<SecureAsset>).value;
    final qualityScore = (request.image.jpegBytes.length / 200000).clamp(
      0.1,
      1.0,
    );
    final reference = ObjectReference(
      referenceId: referenceId,
      objectId: objectId,
      tag: tag,
      imageAssetId: storedAsset.assetId,
      roi: request.roi,
      embeddings: [embedding],
      qualityScore: qualityScore,
      createdAt: DateTime.now(),
    );
    final saved = await _objectStore.create(
      ObjectDraft(objectId: objectId, tag: tag, reference: reference),
    );
    if (saved case TraceFailure<ObjectReference>(:final code, :final message)) {
      await _assetStore.delete(storedAsset.assetId);
      return TraceFailure(code, message);
    }
    return TraceSuccess(
      EnrollResponse(
        objectId: objectId,
        referenceId: referenceId,
        qualityScore: qualityScore,
        embeddingCount: 1,
        warnings: const [
          'Prototype encoder must be replaced before model evaluation.',
        ],
      ),
    );
  }
}
