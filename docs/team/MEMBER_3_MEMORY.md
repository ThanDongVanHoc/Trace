# Thành viên 3 — Last-seen Memory

## Bài toán

Ghi nhận một lần xuất hiện đã được Recognition xác nhận. Cho phép người dùng tìm
bằng tag và nhận lại lần thấy cuối cùng cùng timeline; đầu vào tìm kiếm không có ảnh.

## Phạm vi

- Branch: `feature/kotlin-memory`
- Chỉ sửa: `playground/member3-memory/**`
- Implementation: `MemoryAlgorithm.kt`
- API thử: `/dev/memory/**`
- Test: `test.bat member3`

## Nghiệm thu

- Tìm tag không phân biệt hoa/thường; kết quả mới nhất trước.
- Object chưa có sighting trả `lastSeen: null`.
- Dedup cùng object trong 2 phút; nếu có GPS thì khoảng cách dưới 30 m.
- Timeline áp dụng limit và luôn sắp theo thời gian giảm dần.
- Unit test latest, empty, GPS null, dedup và timeline limit.
- SQLite/repository đã được cấp qua contract; không tự cấu hình database khác.
