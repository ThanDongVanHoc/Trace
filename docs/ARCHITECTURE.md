# TRACE architecture

## Runtime

```text
Android app (Kotlin + Compose)
  ├─ CameraX capture + ROI selection
  ├─ feature/enrollment
  ├─ feature/recognition (on-device model)
  ├─ feature/memory (last-seen rules)
  ├─ feature/securevault (Room + encrypted files)
  ├─ Fused Location + FCM
  └─ Retrofit sync client
             │ HTTPS /v1
NestJS API
  ├─ authentication + rotated sessions
  ├─ four development HTTP harnesses
  ├─ object and sighting sync
  ├─ device registry
  └─ PostgreSQL outbox → FCM
             │
         PostgreSQL
```

Ảnh reference, evidence và embedding phải ở local theo mặc định. Backend chỉ đồng bộ tài khoản, metadata đồ vật và sighting. Các route `/enrollments`, `/recognitions`, `/memory` và `/vault` hiện là HTTP harness để bốn thành viên gửi request và phát triển độc lập; Android chưa gọi chúng và server không ghi JPEG upload xuống disk. Việc dùng chúng trong bản phát hành chỉ được quyết định sau khi review tích hợp, consent, retention và xóa dữ liệu.

## Dependency rule

```text
app ────────────────┬──> core/network
                    ├──> core/database
                    └──> four feature modules

feature/* ─────────────> core/contracts
feature/securevault ───> core/database
```

- Feature module không import implementation của feature module khác.
- Giao tiếp chéo module chỉ dùng interface và DTO trong `core/contracts`.
- Hilt module là composition root; thay implementation không yêu cầu sửa Compose UI.
- `core/database/schemas/` được commit. Thay schema phải có migration và review riêng ở giai đoạn tích hợp Android.

## Luồng chính

```text
Chụp ảnh → chọn ROI + tag → EnrollmentApi → reference + embedding → encrypted vault

Frame mới → RecognitionApi(reference[]) → MATCHED
         → lấy location nếu được cấp quyền → MemoryApi.recordSighting

Chọn tag/objectId → MemoryApi.findLastSeen → thời gian + vị trí
```

Find không nhận ảnh. Ảnh chỉ xuất hiện ở enrollment, recognition và evidence của một sighting.

## Security boundary

- Access token ngắn hạn; refresh token xoay vòng và chỉ lưu hash ở server.
- Token Android được mã hóa AES-GCM bằng key trong Android Keystore.
- Vault production phải mã hóa tag, embedding, location, reference image và evidence trước khi persistence.
- Release không cho cleartext HTTP; signing key, Firebase credential và database secret không commit.
