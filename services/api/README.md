# TRACE API

NestJS API cho authentication, object sync, one-shot enrollment, sightings và
notifications của TRACE.

## Development

Yêu cầu: Node.js 24 và Docker Desktop đang chạy.

Lần đầu:

```powershell
Set-Location C:\Mobile_Final_App\services\api
npm ci
npm run start:dev
```

`npm run start:dev` tự thực hiện ba việc:

1. Tạo `.env` từ `.env.example` nếu máy dev chưa có.
2. Khởi động và chờ PostgreSQL healthy bằng Docker Compose.
3. Chạy NestJS watch mode tại `http://localhost:3000`.

PostgreSQL của TRACE dùng cổng host `55432` để tránh xung đột với PostgreSQL cài
trực tiếp trên máy; bên trong Docker vẫn dùng cổng chuẩn `5432`.

Nếu developer dùng PostgreSQL ngoài Docker:

```powershell
$env:TRACE_SKIP_DEV_DB = "1"
npm run start:dev
```

Swagger: `http://localhost:3000/docs`

Health: `http://localhost:3000/v1/health`

## Test API kỹ thuật bằng Swagger

1. Gọi `POST /v1/auth/register` và copy `accessToken`.
2. Bấm **Authorize** trong Swagger, nhập token.
3. Gọi endpoint được giao và bấm **Try it out**.

| Thành viên | Endpoint chính | Input |
|---|---|---|
| 1 — Enrollment | `POST /v1/enrollments` | JPEG + tag + ROI |
| 2 — Recognition | `POST /v1/recognitions` | JPEG + candidate embeddings |
| 3 — Memory | `POST /v1/memory/sightings`, `/find` | JSON, Find không có ảnh |
| 4 — Vault | `POST /v1/vault/seal`, `/open` | Base64 payload + AAD |

Mỗi endpoint có `Prototype*Engine` và warning rõ ràng. Mỗi thành viên chỉ thay engine
trong module được giao; upload, validation, auth, Swagger và response contract đã
có sẵn. Android chưa gọi bốn endpoint này trong giai đoạn phát triển độc lập.

## Verification

```powershell
npm run lint
npm test
npm run test:e2e
npm run build
```
