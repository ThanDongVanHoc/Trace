# TRACE

TRACE là ứng dụng Android local-first giúp người dùng ghi nhớ đồ vật đã gắn tag được nhìn thấy lần cuối ở đâu và khi nào. Nhánh `android` có prototype native build được thành APK và backend HTTP để từng bài toán kỹ thuật có thể phát triển, gửi request và kiểm thử độc lập trước khi nối vào app.

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
  feature/enrollment/     Adapter Enrollment prototype phía Android
  feature/recognition/    Adapter Recognition prototype phía Android
  feature/memory/         Adapter Memory prototype phía Android
  feature/securevault/    Adapter Vault prototype phía Android
services/api/             NestJS + PostgreSQL; 4 technical dev APIs
docs/team/                Requirement giao cho từng thành viên
```

`PrototypeVisualEngine` và các `InMemory*Store` chỉ là adapter giúp UI hiện tại chạy được. Bốn thành viên phát triển các engine HTTP độc lập ở backend; việc thay adapter Android và nối bốn API vào app được thực hiện ở giai đoạn tích hợp sau.

## Chạy backend

Yêu cầu: Node.js 24, Docker Desktop.

```powershell
Set-Location services/api
npm ci
npm run start:dev
```

Chỉ cần Docker Desktop đang chạy. `npm run start:dev` tự tạo `.env` nếu thiếu,
bật PostgreSQL, chờ database healthy rồi chạy NestJS hot reload.
Database dev dùng cổng host `55432` để không đụng PostgreSQL cài sẵn trên máy.

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
