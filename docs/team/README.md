# Quy tắc tích hợp

1. Mỗi người chỉ sửa phạm vi được giao trên branch riêng.
2. Không tự đổi public HTTP/Kotlin contract; đề xuất thay đổi bằng PR riêng trước.
3. Module kỹ thuật chỉ phụ thuộc contract/port đã chốt, không đọc dữ liệu của module khác trực tiếp.
4. PR phải kèm test, số đo nghiệm thu và chạy được lệnh kiểm tra ghi trong requirement.
5. Không commit ảnh cá nhân, key, token, model chưa rõ license hoặc `google-services.json`.

| Thành viên | Branch | Module |
|---|---|---|
| 1 | `feature/api-enrollment` | `services/api/src/enrollments` |
| 2 | `feature/api-recognition` | `services/api/src/recognitions` |
| 3 | `feature/api-memory` | `services/api/src/memory` |
| 4 | `feature/api-secure-vault` | `services/api/src/vault` |
