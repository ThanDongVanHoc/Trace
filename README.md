# TRACE

TRACE is a local-first mobile application that remembers where tagged objects were last seen. This repository is a production-oriented prototype for Android API 24+ and iOS 15+.

## Repository

```text
apps/mobile/       Flutter application
services/api/      NestJS REST API
docs/team/         Four member assignments
compose.yaml       Local PostgreSQL
```

## Implemented foundation

- Email/password registration and login.
- Short-lived JWT access tokens and rotated, hashed refresh tokens.
- Device registration for FCM/APNs.
- Cloud object and sighting APIs.
- PostgreSQL notification outbox and Firebase delivery worker.
- Flutter camera, location, Tag, Recognize and Last-seen flows.
- Versioned module contracts and Drift local schema.
- Android Keystore/iOS Keychain token storage.

`PrototypeVisualEngine` and the in-memory stores are integration baselines, not production ML or encryption. The four assignments in `docs/team/` replace them without changing the UI contract.

## Start the backend

Requirements: Node.js, Docker and Docker Compose.

```powershell
Copy-Item .env.example .env
docker compose up -d
Set-Location services/api
npm ci
npm run start:dev
```

Swagger UI: `http://localhost:3000/docs`  
Health: `http://localhost:3000/v1/health`

For a deployed environment, set `DB_SYNCHRONIZE=false` and apply `services/api/migrations/001_initial.sql` through the release pipeline.

## Start the mobile app

```powershell
Set-Location apps/mobile
flutter pub get
dart run build_runner build
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:3000/v1
```

Use the LAN IP of the development machine instead of `10.0.2.2` on a physical Android/iOS device. iOS builds require macOS and Xcode.

Remote push requires the standard Firebase files:

- Android: `google-services.json`
- iOS: `GoogleService-Info.plist`
- API: `FIREBASE_SERVICE_ACCOUNT_JSON`

These files are ignored by Git and must never be committed.

## Verification

```powershell
Set-Location services/api
npm run build
npm run lint
npm test

Set-Location ../../apps/mobile
flutter analyze
flutter test
```
