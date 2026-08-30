# Thành viên 4 — Secure Vault API

## Mục tiêu

Xây engine mã hóa AES-256-GCM có version key và AAD, phát hiện mọi sửa đổi dữ liệu,
không làm lộ plaintext hoặc key. API dev cho phép seal/open bundle bằng Base64 để
test trực tiếp bằng Swagger/Postman; tích hợp storage/app làm sau.

## Phạm vi code

- Branch: `feature/api-secure-vault`
- Sở hữu: `services/api/src/vault/**`
- Không sửa auth, Enrollment, Recognition, Memory hoặc HTTP contract.

## Chạy khi phát triển

```powershell
Set-Location services/api
npm ci
npm run start:dev
```

Mở `http://localhost:3000/docs`, đăng nhập/Authorize, gọi `seal`, copy bundle trả
về sang `open`, rồi thử sửa ciphertext, AAD, tag hoặc key version.

## HTTP contract

```text
POST /v1/vault/seal
POST /v1/vault/open
```

`seal` nhận `plaintextBase64`, `associatedData`; trả algorithm, key version, nonce,
ciphertext, authentication tag và warnings. `open` nhận bundle trên và chỉ trả
plaintext khi authentication thành công.

Controller, validation, giới hạn payload và AES-GCM engine mẫu đã có. Thay
`PrototypeVaultEngine` bằng engine production qua `VaultEngine`; không đổi DTO.

## Yêu cầu kỹ thuật

- AES-256-GCM, nonce ngẫu nhiên 12 byte, tag 16 byte; không lặp nonce cùng key.
- Key 32 byte lấy từ secret provider, không hard-code/log/export; có `keyVersion`.
- AAD bắt buộc và gắn với record/asset ID; đổi AAD phải decrypt thất bại.
- Tamper, wrong key/version/tag trả cùng mã `CRYPTO_FAILURE`, không trả plaintext hỏng.
- Hỗ trợ đọc key version cũ trong thời gian rotation; key mới chỉ dùng để encrypt.
- Không log key, plaintext, ciphertext/tag đầy đủ hoặc token.

## Nghiệm thu

- Round-trip đúng; plaintext không xuất hiện trong ciphertext.
- Test unique nonce ít nhất 10.000 lần, tamper từng thành phần, wrong key và AAD.
- Restart với secret/key version cố định vẫn mở được bundle cũ.
- Swagger/Postman seal → open thành công; sửa một byte nhận `CRYPTO_FAILURE`.
- Chạy xanh:

```powershell
npm test -- vault
npm run lint
npm run build
```
