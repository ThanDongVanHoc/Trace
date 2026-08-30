# Thành viên 3 — Last-seen Memory

## Mục tiêu

Ghi nhận lần xuất hiện đã được Recognition xác nhận và trả lời đồ vật được thấy lần cuối ở đâu, lúc nào. Người dùng Find bằng tag/object ID; Find không nhận ảnh.

## Phạm vi code

- Branch: `feature/android-memory`
- Sở hữu: `apps/android/feature/memory/**`
- Không gọi Recognition, CameraX, Room DAO hoặc backend trực tiếp.

## API phải implement

```kotlin
suspend fun MemoryApi.recordSighting(request: RecordSightingRequest): TraceResult<RecordSightingResponse>
suspend fun MemoryApi.findLastSeen(objectId: String): TraceResult<FindLastSeenResponse>
suspend fun MemoryApi.getTimeline(objectId: String, limit: Int): TraceResult<List<Sighting>>
```

Chỉ được gọi `ObjectStore`, `SightingStore`, `SecureAssetStore`. Location đã được app truyền qua `RecordSightingRequest` và có thể `null`.

## Yêu cầu

- Chỉ ghi khi object tồn tại, confidence hợp lệ và kết quả upstream là `MATCHED`.
- Lưu thời gian UTC, GPS accuracy và evidence tùy chọn; không có quyền GPS vẫn phải ghi sighting.
- Dedup cùng object trong 2 phút: nếu có hai location thì khoảng cách phải dưới 30 m; nếu thiếu GPS thì dedup theo thời gian.
- Dedup cập nhật timestamp/location/confidence của bản ghi cũ, không tạo ID mới.
- `findLastSeen(objectId)` không có sighting trả `lastSeen = null`, không trả lỗi.
- Timeline mới nhất trước, limit `1..100`.
- Bản ghi mới có `SyncStatus.PENDING`; worker tích hợp backend do app/integration đảm nhiệm.
- Nếu ghi evidence thành công nhưng ghi sighting lỗi, phải xóa evidence.

## Nghiệm thu

- Unit test: latest theo UTC, empty result, GPS null, dedup thời gian, dedup khoảng cách, ngoài cửa sổ và timeline limit.
- Test rollback evidence khi store lỗi.
- Không dùng Android framework trong domain logic để test JVM nhanh.
- Chạy xanh:

```powershell
.\gradlew.bat :feature:memory:testDebugUnitTest
```
