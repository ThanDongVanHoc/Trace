# Thành viên 4 — Secure Vault

## Bài toán

Mã hóa dữ liệu bằng AES-256-GCM, gắn AAD với record và phát hiện mọi sửa đổi. Không
được log hoặc trả plaintext khi xác thực thất bại.

## Phạm vi

- Branch: `feature/kotlin-secure-vault`
- Chỉ sửa: `playground/member4-vault/**`
- Implementation: `VaultAlgorithm.kt`
- API thử: `POST /dev/vault/seal`, `POST /dev/vault/open`
- Test: `test.bat member4`

## Nghiệm thu

- Nonce ngẫu nhiên 12 byte, authentication tag 16 byte; không lặp nonce cùng key.
- AAD thay đổi hoặc ciphertext bị sửa phải decrypt thất bại như nhau.
- Có `keyId`/version và hỗ trợ rotation; key không hard-code hoặc ghi log.
- Round-trip vẫn hoạt động sau restart khi dùng cùng key provider.
- Test ít nhất 10.000 nonce cùng tamper ciphertext, nonce, AAD và wrong key.
- Key hiện tại của playground là tạm thời; key management là phần thành viên phải làm.
