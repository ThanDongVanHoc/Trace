# Module contracts v1

Nguồn chuẩn duy nhất: `apps/android/core/contracts/src/main/kotlin/com/traceapp/core/contracts/`.

## API

```kotlin
interface EnrollmentApi {
    suspend fun enroll(request: EnrollRequest): TraceResult<EnrollResponse>
}

interface VisualEncoder {
    suspend fun encode(image: ImageInput, roi: NormalizedRect? = null): TraceResult<VisualEmbedding>
}

interface RecognitionApi {
    suspend fun recognize(request: RecognizeRequest): TraceResult<RecognizeResponse>
}

interface MemoryApi {
    suspend fun recordSighting(request: RecordSightingRequest): TraceResult<RecordSightingResponse>
    suspend fun findLastSeen(objectId: String): TraceResult<FindLastSeenResponse>
    suspend fun getTimeline(objectId: String, limit: Int = 50): TraceResult<List<Sighting>>
}
```

## Store

| Interface | Owner production | Bảng/dữ liệu |
|---|---|---|
| `ObjectStore` | Thành viên 4 | `local_objects`, `local_object_references`, `local_reference_embeddings` |
| `SightingStore` | Thành viên 4 | `local_sightings` |
| `SecureAssetStore` | Thành viên 4 | `secure_assets`, file ciphertext |

Enrollment chỉ gọi `VisualEncoder`, `ObjectStore`, `SecureAssetStore`. Recognition chỉ nhận `ImageInput` và `ObjectReference[]`, không đọc DB. Memory chỉ gọi Store và nhận location từ app. Secure Vault implement Store, không chứa logic nhận diện.

## Quy ước dữ liệu

- ID: UUID string.
- Thời gian: UTC Unix epoch milliseconds.
- ROI/bounding box: tọa độ chuẩn hóa `0..1`, thứ tự `left < right`, `top < bottom`.
- Similarity, confidence, quality: `0..1`.
- Ảnh: JPEG bytes kèm width, height, rotation.
- Model compatibility: `modelName`, `modelVersion`, số chiều embedding phải khớp.

Không sửa public DTO/interface trên branch cá nhân. Nếu contract thật sự thiếu, mở PR contract riêng và cần cả nhóm duyệt trước khi code feature phụ thuộc vào nó.
