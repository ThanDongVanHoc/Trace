# TRACE

TRACE là ứng dụng Android local-first giúp người dùng ghi nhớ đồ vật đã gắn tag được nhìn thấy lần cuối ở đâu và khi nào. Nhánh `android` là prototype native có thể build thành APK, đồng thời chia bốn bài toán kỹ thuật thành bốn Gradle module độc lập để bốn thành viên phát triển song song.

## Công nghệ

- Android API 24+, Kotlin 2.3.21, Jetpack Compose, Material 3.
- CameraX, Fused Location Provider, Firebase Cloud Messaging.
- Hilt, Coroutines, Room + KSP, Retrofit + OkHttp.
- Android Keystore cho token đăng nhập.
- NestJS REST API, PostgreSQL, JWT access/refresh rotation, notification outbox.

Các phiên bản Android được khóa trong `apps/android/gradle/libs.versions.toml`. Compose/Hilt/Lifecycle được giữ ở dòng tương thích AGP 8.13 và compile SDK 36; không nâng riêng lẻ nếu chưa chạy lại toàn bộ CI.

## Cấu trúc

```text
apps/android/
  app/                    Compose UI, CameraX, location, FCM, composition root
  core/contracts/         DTO và interface bất biến giữa bốn module
  core/database/          Room schema v1
  core/network/           REST client, JWT refresh, Keystore token store
  feature/enrollment/     Thành viên 1
  feature/recognition/    Thành viên 2
  feature/memory/         Thành viên 3
  feature/securevault/    Thành viên 4
services/api/             NestJS + PostgreSQL backend
docs/team/                Requirement giao cho từng thành viên
```

`PrototypeVisualEngine` và các `InMemory*Store` chỉ là adapter giúp tích hợp UI ngay từ đầu. Chúng không phải model nhận diện hay kho mã hóa dùng cho bản phát hành; Thành viên 2 và 4 có trách nhiệm thay chúng mà không đổi `core/contracts`.

## Chạy backend

Yêu cầu: Node.js 24, Docker Desktop.

```powershell
Copy-Item .env.example .env
docker compose up -d
Set-Location services/api
npm ci
npm run start:dev
```

Swagger: `http://localhost:3000/docs`

Health: `http://localhost:3000/v1/health`

## Chạy Android

Yêu cầu: Android Studio, JDK 17, Android SDK Platform 36 và Build Tools 35.0.0.

```powershell
Set-Location apps/android
.\gradlew.bat installDebug -PTRACE_API_BASE_URL=http://10.0.2.2:3000/v1/
```

`10.0.2.2` là máy host nhìn từ Android Emulator. Khi dùng điện thoại thật, thay bằng IP LAN của máy chạy backend. Release build phải dùng HTTPS.

Để bật push notification, đặt `google-services.json` vào `apps/android/app/` và cấu hình `FIREBASE_SERVICE_ACCOUNT_JSON` ở backend. File secret này đã bị Git bỏ qua.

## Kiểm tra

```powershell
Set-Location apps/android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug

Set-Location ../../services/api
npm ci
npm run build
npm run lint
npm test
```

APK debug nằm tại `apps/android/app/build/outputs/apk/debug/app-debug.apk`.
