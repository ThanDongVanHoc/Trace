# Phân công kỹ thuật

Mọi thành viên dùng cùng quy trình:

```bat
dev.bat
```

Mở `http://localhost:8080/docs`, gọi API của mình, sửa Kotlin và gọi lại. Không cần
Android Studio, Docker, database server hoặc token. SQLite nằm tại
`playground/data/trace-dev.db` và được tạo tự động.

| Thành viên | Branch | Chỉ sửa module |
|---|---|---|
| 1 | `feature/kotlin-enrollment` | `playground/member1-enrollment` |
| 2 | `feature/kotlin-recognition` | `playground/member2-recognition` |
| 3 | `feature/kotlin-memory` | `playground/member3-memory` |
| 4 | `feature/kotlin-secure-vault` | `playground/member4-vault` |

Không đổi file trong `contracts`, `storage` hoặc `dev-server` nếu chưa thống nhất
với team. PR phải có test cho logic mới và chạy xanh `test.bat memberN`.
