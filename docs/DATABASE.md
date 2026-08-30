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

- Thành viên 1 dùng `ObjectStore` và `SecureAssetStore`.
- Thành viên 2 không đọc database; reference được truyền trong request.
- Thành viên 3 dùng `ObjectStore`, `SightingStore`, `SecureAssetStore`.
- Thành viên 4 sở hữu DAO, transaction, encryption, migration và file lifecycle.
