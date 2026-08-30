# TRACE Android

Mở trực tiếp thư mục này bằng Android Studio. Gradle dùng JDK 17, compile SDK 36, min SDK 24.

## Module boundary

- `:app`: UI và adapter Android; không đặt thuật toán nhận diện/mã hóa tại đây.
- `:core:contracts`: public API giữa các thành viên; chỉ thay qua PR contract riêng.
- `:core:database`: Room entity/DAO/schema; feature khác chỉ truy cập qua Store.
- `:core:network`: REST DTO/client, access token interceptor, refresh rotation, Keystore token store.
- `:feature:*`: bốn bài toán kỹ thuật độc lập.

## Lệnh chuẩn

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
.\gradlew.bat installDebug -PTRACE_API_BASE_URL=http://10.0.2.2:3000/v1/
```

Release build không cho HTTP cleartext. Firebase plugin chỉ được apply khi `app/google-services.json` tồn tại, vì vậy CI và developer không có Firebase config vẫn build được.
