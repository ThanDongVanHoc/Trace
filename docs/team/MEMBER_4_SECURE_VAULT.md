# Thành viên 4 — Secure Local Vault

## Mục tiêu

Thay toàn bộ `InMemory*Store` bằng persistence Room và mã hóa dữ liệu nhạy cảm đúng chuẩn Android.

## Phạm vi code

- Branch: `feature/android-secure-vault`
- Sở hữu: `apps/android/feature/securevault/**`, migration/schema trong `apps/android/core/database/**`
- Không sửa domain DTO, Recognition, Enrollment hoặc Memory.

## API phải implement

- `ObjectStore`
- `SightingStore`
- `SecureAssetStore`

Room schema v1 đã có:

- `local_objects`
- `local_object_references`
- `local_reference_embeddings`
- `local_sightings`
- `secure_assets`

Không thay tên bảng/cột nếu không có migration và schema JSON mới.

## Yêu cầu

- AES-256-GCM; nonce ngẫu nhiên 12 byte và không lặp với cùng key.
- Master key sinh trong Android Keystore, alias có version; không hard-code hoặc export key.
- Mã hóa tag, embedding và location trước khi ghi Room.
- Reference/evidence image ghi thành file ciphertext; DB chỉ giữ relative path, nonce và metadata.
- Dùng AAD gắn ciphertext với `assetId`/record ID và version để chống tráo bản ghi.
- Ghi file theo `temp → fsync → atomic rename`; crash không để file nửa chừng.
- Tạo object + reference + embeddings trong một Room transaction.
- Xóa object phải cascade reference/sighting và xóa mọi file asset liên quan.
- Decrypt/tamper/wrong-key phải trả `CRYPTO_FAILURE`; không trả plaintext hỏng.
- Không log key, nonce+ciphertext đầy đủ, tag, embedding, location hoặc token.
- Auth token thuộc `core/network/KeystoreTokenStore`, không thay trong task này.

## Nghiệm thu

- Copy DB/files ra ngoài không tìm thấy plaintext tag, location, embedding hoặc JPEG header.
- Sửa 1 byte ciphertext/tag hoặc đổi AAD thì decrypt thất bại.
- Test: round-trip, unique nonce 10.000 lần, tamper, wrong key, transaction rollback, restart app, delete cascade và orphan cleanup.
- Commit schema `core/database/schemas/.../2.json` nếu tăng version và có migration test `1 → 2`.
- Chạy xanh:

```powershell
.\gradlew.bat :feature:securevault:testDebugUnitTest :feature:securevault:connectedDebugAndroidTest
```
