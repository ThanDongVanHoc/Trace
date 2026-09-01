# TRACE

TRACE giúp người dùng ghi nhớ đồ vật đã được gắn tag và tìm lại lần cuối chúng
xuất hiện ở đâu, khi nào.

Ưu tiên hiện tại của team là giải bốn bài toán kỹ thuật bằng Kotlin. Thành viên
không cần Android Studio, Android SDK, Docker, Node.js, JWT hay PostgreSQL.

## Bắt đầu trong hai bước

Yêu cầu duy nhất: Git và JDK 21 LTS.

```bat
git clone https://github.com/ThanDongVanHoc/Trace.git
cd Trace
dev.bat
```

Lần đầu Gradle tự tải dependency, SQLite tự tạo database. Sau đó mở:

- Swagger: `http://localhost:8080/docs`
- Database: `playground/data/trace-dev.db`
- Ảnh thử: `playground/data/blobs/`

Không có đăng nhập trong playground. Khi sửa Kotlin, watcher tự biên dịch và Ktor
nạp lại code. Nếu code không compile, xem `playground/data/compile-watch-error.log`.

## Bốn module

| Thành viên | File chính | Test |
|---|---|---|
| 1 — Enrollment | `playground/member1-enrollment/**/EnrollmentAlgorithm.kt` | `test.bat member1` |
| 2 — Recognition | `playground/member2-recognition/**/RecognitionAlgorithm.kt` | `test.bat member2` |
| 3 — Memory | `playground/member3-memory/**/MemoryAlgorithm.kt` | `test.bat member3` |
| 4 — Secure Vault | `playground/member4-vault/**/VaultAlgorithm.kt` | `test.bat member4` |

Chạy toàn bộ kiểm tra:

```bat
test.bat
```

## Kiến trúc playground

```text
Swagger / Postman
        |
        v
Ktor dev-server             Chỉ là test harness
        |
        +--> member1-enrollment
        +--> member2-recognition
        +--> member3-memory ----> SQLite file
        +--> member4-vault
        |
        +--> contracts           Request/response/interface cố định
```

HTTP và SQLite đã được nối sẵn. Thành viên chỉ sửa thuật toán và test trong module
của mình. Prototype hiện tại chứng minh pipeline chạy được, không phải lời giải cuối.

`apps/android/` và `services/api/` vẫn được giữ cho giai đoạn sản phẩm. Playground
không phải production backend; sau khi bốn bài toán đạt tiêu chí, AI/team integration
sẽ chuyển hoặc viết lại implementation cho Android.

Requirement ngắn của từng người nằm trong `docs/team/`.
