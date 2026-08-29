# Module contracts v1

Nguồn chuẩn: `apps/mobile/lib/core/contracts/`.

```text
EnrollmentApi ──uses──> VisualEncoder
EnrollmentApi ──uses──> ObjectStore + SecureAssetStore

RecognitionApi <── receives ObjectReference[] in request

MemoryApi ──uses──> ObjectStore + SightingStore + SecureAssetStore

Secure Vault ──implements──> ObjectStore + SightingStore + SecureAssetStore
```

## Integration flows

```text
EnrollRequest
→ EnrollmentApi
→ ObjectStore
→ EnrollResponse

ImageInput + ObjectReference[]
→ RecognitionApi
→ ObjectDetection[]
→ app obtains optional GeoFix
→ MemoryApi.recordSighting

objectId
→ MemoryApi.findLastSeen
→ FindLastSeenResponse
```

IDs use UUID. Time uses UTC `DateTime`. ROI and bounding boxes use normalized coordinates `0..1`. Similarity and confidence use `0..1`.
