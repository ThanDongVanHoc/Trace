# Thành viên 3 — Last-seen Memory

## Mục tiêu

Ghi nhận lần xuất hiện từ kết quả Recognition và trả lời đồ vật được nhìn thấy lần cuối ở đâu, lúc nào.

## Code sở hữu

`apps/mobile/lib/features/memory/**`

## API phải implement

- `MemoryApi.recordSighting(RecordSightingRequest)`
- `MemoryApi.findLastSeen(objectId)`
- `MemoryApi.getTimeline(objectId, limit)`

## Dependency được dùng

- `ObjectStore`
- `SightingStore`
- `SecureAssetStore`
- Location adapter ở tầng app

Không gọi Recognition và không nhận ảnh khi người dùng thực hiện Find.

## Bảng/API liên quan

- Local: `local_objects`, `local_sightings`, `secure_assets`
- Server: `POST /v1/sightings/batch`
- Server: `GET /v1/objects/{id}/last-seen`

## Yêu cầu

- Chỉ ghi detection `MATCHED` đã vượt threshold.
- Location được phép null; lưu accuracy nếu có.
- Chống trùng: cùng object, dưới 2 phút và dưới 30 m thì cập nhật bản ghi cũ.
- Find nhận `objectId`, không yêu cầu ảnh mới.
- Local-first; đánh dấu pending và batch-sync tối đa 100 sightings/lần.

## Nghiệm thu

- Find trả đúng sighting mới nhất theo UTC.
- Không có sighting trả `lastSeen = null`, không báo lỗi.
- Có test cho dedup theo thời gian, khoảng cách, GPS null và timeline limit.
- Mất mạng không mất dữ liệu; kết nối lại sync không tạo bản ghi trùng.
