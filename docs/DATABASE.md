# Local database schema v1

Schema máy đọc: `apps/android/core/database/schemas/com.traceapp.core.database.TraceDatabase/1.json`.

| Bảng | Khóa/cột chính | Nội dung nhạy cảm |
|---|---|---|
| `local_objects` | `object_id`, `encrypted_tag`, `reference_revision`, timestamps | tag đã mã hóa |
| `secure_assets` | `asset_id`, `type`, `relative_path`, `nonce`, `mime_type` | file tại path là ciphertext |
| `local_object_references` | `reference_id`, `object_id`, `image_asset_id`, ROI, quality | liên kết tới ảnh mã hóa |
| `local_reference_embeddings` | `reference_id + ordinal`, `encrypted_values`, dimensions, model | vector đã mã hóa |
| `local_sightings` | `sighting_id`, `object_id`, time, `encrypted_location`, confidence, sync status | location đã mã hóa |

Quan hệ:

```text
local_objects 1 ── n local_object_references 1 ── n local_reference_embeddings
      │                         │
      └── n local_sightings     └── 1 secure_assets
```

- Android Enrollment adapter dùng `ObjectStore` và `SecureAssetStore`; HTTP harness của Thành viên 1 không đọc Room.
- Android Recognition adapter không đọc database; reference được truyền trong request.
- Android Memory adapter dùng `ObjectStore`, `SightingStore`, `SecureAssetStore`.
- Android Secure Vault adapter sẽ sở hữu DAO, transaction, migration và file lifecycle khi tích hợp vào app.

Các HTTP harness của bốn thành viên dùng contract backend riêng. Memory harness dùng
PostgreSQL server; Vault harness hiện chỉ seal/open trong memory và không thay thế
Room + Android Keystore của bản phát hành.
