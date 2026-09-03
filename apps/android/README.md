# TRACE Android

Ứng dụng Kotlin/Jetpack Compose độc lập, `minSdk 24`, `targetSdk 36`. Không có
HTTP client, API URL, JWT, Firebase hay backend runtime.

## Công cụ cần có

- Git.
- JDK 21 LTS. Bytecode Android vẫn target Java 17.
- Android SDK Platform 36 và Build Tools 35 trở lên.
- `adb` chỉ cần khi muốn cài APK từ command line.

`build-android.ps1` ở root tự tìm JDK 21, kể cả khi Java mặc định của Windows là
Java 26. Gradle tìm Android SDK qua `ANDROID_HOME`, `ANDROID_SDK_ROOT`, hoặc file
không commit `apps/android/local.properties`:

```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
```

## Lệnh chuẩn (chạy từ root repository)

```powershell
.\build-android.bat build       # APK debug cài được
.\build-android.bat check       # unit test + lint + APK debug
.\build-android.bat install     # cài lên thiết bị đang nối adb
.\build-android.bat submission  # apk/app-release.apk, ARM64, signed debug
.\build-android.bat bundle      # release AAB cho Google Play
```

`submission` tạo APK ARM64 nhỏ hơn giới hạn một file của GitHub. Đây là
certificate debug theo đúng lựa chọn “release hoặc signed debug” của đề; không
dùng certificate này để phát hành Google Play.

## Ký release thật

Không commit keystore hay mật khẩu. Đặt bốn biến môi trường rồi chạy `bundle`:

```powershell
$env:TRACE_RELEASE_STORE_FILE='D:\secure\trace-release.jks'
$env:TRACE_RELEASE_STORE_PASSWORD='...'
$env:TRACE_RELEASE_KEY_ALIAS='trace'
$env:TRACE_RELEASE_KEY_PASSWORD='...'
.\build-android.bat bundle
```

Nếu thiếu biến, Gradle vẫn tạo được AAB/APK release chưa ký để CI kiểm tra R8.

## Module boundary

- `:app`: Compose UI, CameraX, runtime permissions, vị trí và notification.
- `:core:auth`: tài khoản/session cục bộ; password verifier PBKDF2 có salt.
- `:core:contracts`: request/result/interface giữa các feature.
- `:core:database`: Room entity, DAO và migration; không chứa business logic.
- `:feature:enrollment`: xác thực input, ROI, embedding, tạo reference.
- `:feature:recognition`: ONNX Runtime, SSD detection, MobileNetV3 embedding và
  cosine matching.
- `:feature:memory`: sighting, deduplication, timeline và last-seen.
- `:feature:securevault`: AES-256-GCM, Android Keystore, encrypted file/field và
  implementation các Store.

Dependency chỉ đi qua `core:contracts`; UI không truy cập DAO trực tiếp. Model
được copy từ assets sang `noBackupFilesDir` lần đầu để ONNX Runtime mở bằng path.

## Smoke test trên điện thoại

1. Cài APK, tạo tài khoản với mật khẩu ít nhất 8 ký tự.
2. Cấp Camera; ở lần nhận diện đầu tiên, chọn cho phép Location. Notification là
   tùy chọn.
3. Gắn tag một đồ vật bằng ROI rõ, đủ sáng.
4. Sang chế độ Nhận diện, chụp đồ vật trong một khung cảnh khác.
5. Mở tab Tìm và kiểm tra thời gian/vị trí lần cuối.
6. Tắt mạng và lặp lại: toàn bộ luồng vẫn phải hoạt động.

Nếu cần xóa sạch dữ liệu test: Android Settings → Apps → TRACE → Storage → Clear
storage. Android Keystore key cũ và ciphertext không được backup/restore.
