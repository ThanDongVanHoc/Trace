# Quy tắc tích hợp

1. Mỗi người chỉ sửa module được giao trên branch riêng.
2. Không sửa `core/contracts`; đề xuất thay đổi contract bằng PR riêng trước.
3. Feature module không import feature module khác và không đọc database trực tiếp.
4. PR phải kèm test, số đo nghiệm thu và chạy được task Gradle ghi trong requirement.
5. Không commit ảnh cá nhân, key, token, model chưa rõ license hoặc `google-services.json`.

| Thành viên | Branch | Module |
|---|---|---|
| 1 | `feature/android-enrollment` | `:feature:enrollment` |
| 2 | `feature/android-recognition` | `:feature:recognition` |
| 3 | `feature/android-memory` | `:feature:memory` |
| 4 | `feature/android-secure-vault` | `:feature:securevault`, `:core:database` |
