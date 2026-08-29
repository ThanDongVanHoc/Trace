# Thành viên 4 — Secure Local Vault

## Mục tiêu

Thay các InMemory Store bằng persistence mã hóa, quản lý khóa đúng trên Android và iOS.

## Code sở hữu

`apps/mobile/lib/features/secure_vault/**` và adapter trong `core/database/**`

## API phải implement

- `ObjectStore`
- `SightingStore`
- `SecureAssetStore`

Không thay domain DTO hoặc API của ba module khác.

## Bảng sở hữu

- `local_objects`
- `secure_assets`
- `local_object_references`
- `local_reference_embeddings`
- `local_sightings`

Schema hiện tại nằm trong `core/database/trace_database.dart`.

## Yêu cầu

- Ảnh và evidence: AES-256-GCM, nonce duy nhất cho mỗi lần mã hóa.
- Tag, embedding và location được mã hóa trước khi ghi SQLite.
- Master key được bảo vệ bằng Android Keystore/iOS Keychain; không hard-code.
- Ghi file theo kiểu temp → fsync → atomic rename.
- Xóa object phải xóa reference, sighting và asset liên quan.
- Token đăng nhập chỉ lưu bằng `flutter_secure_storage`.

## Nghiệm thu

- File/database copy ra ngoài không đọc được plaintext nhạy cảm.
- Sửa một byte ciphertext hoặc authentication tag thì decrypt thất bại.
- Không có key, token, embedding hoặc location trong log.
- Có test encrypt/decrypt, unique nonce, tamper, wrong key, delete cascade và restart app.
