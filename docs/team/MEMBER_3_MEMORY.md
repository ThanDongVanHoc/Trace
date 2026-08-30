# Thành viên 3 — Last-seen Memory API

## Mục tiêu

Ghi một lần xuất hiện đã được Recognition xác nhận và trả lời đồ vật được thấy lần
cuối ở đâu, lúc nào. Người dùng tìm bằng tag hoặc object UUID; Find không nhận ảnh.

## Phạm vi code

- Branch: `feature/api-memory`
- Sở hữu: `services/api/src/memory/**`
- Không sửa Recognition, camera, auth, database entity hoặc HTTP contract.

## Chạy khi phát triển

```powershell
Set-Location services/api
npm ci
npm run start:dev
```

Mở Swagger tại `http://localhost:3000/docs`. Tạo object bằng Enrollment/Object API
một lần, sau đó test các endpoint Memory; backend và database tự reload/kết nối.

## HTTP contract

```text
POST /v1/memory/sightings
POST /v1/memory/find
GET  /v1/memory/objects/{objectId}/timeline?limit=50
```

`/sightings` nhận object UUID, UTC ISO time, confidence, bounding box/location và
evidence asset UUID tùy chọn. `/find` chỉ nhận `{ "query": "Balo của tôi" }`.

Controller, validation, object/sighting store adapter và Swagger đã có. Thay
`PrototypeMemoryEngine` bằng logic thật qua `MemoryEngine`; không đổi DTO/controller.

## Yêu cầu kỹ thuật

- Chỉ ghi object tồn tại và confidence `0..1`; location có thể `null`.
- Dedup cùng object trong 2 phút; nếu có GPS thì khoảng cách phải dưới 30 m.
- Dedup cập nhật record cũ, không sinh ID mới.
- Find không có sighting trả `lastSeen: null`; timeline mới nhất trước.
- Limit `1..100`; thời gian lưu UTC; không log tag/location.

## Nghiệm thu

- Unit test latest, empty, GPS null, dedup thời gian/khoảng cách và timeline limit.
- HTTP test record JSON, find bằng tag và timeline.
- Swagger/Postman không gửi ảnh vào Find và vẫn nhận đúng kết quả.
- Chạy xanh:

```powershell
npm test -- memory
npm run lint
npm run build
```
