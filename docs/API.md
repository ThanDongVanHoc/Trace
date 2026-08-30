# TRACE API v1

Base URL: `/v1`. Protected endpoints require `Authorization: Bearer <accessToken>`.

## Authentication

```text
POST /auth/register  { email, password, displayName }
POST /auth/login     { email, password }
POST /auth/refresh   { refreshToken }
POST /auth/logout
GET  /auth/me
```

Register, login and refresh return:

```json
{
  "user": { "id": "uuid", "email": "user@example.com", "displayName": "Minh" },
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "expiresInSeconds": 900
}
```

## Device and push

```text
PUT    /devices/{installationId}
DELETE /devices/{installationId}
POST   /notifications/test
```

```json
{
  "platform": "android",
  "pushToken": "fcm-token",
  "locale": "vi_VN",
  "notificationsEnabled": true
}
```

## Objects

```text
GET    /objects
POST   /objects
PATCH  /objects/{id}
DELETE /objects/{id}
```

```json
{
  "id": "optional-client-uuid",
  "tag": "Balô của tôi",
  "referenceRevision": 1
}
```

## One-shot enrollment

```text
POST /enrollments
Content-Type: multipart/form-data
```

| Field | Type | Rule |
|---|---|---|
| `image` | JPEG file | required, maximum 10 MiB |
| `tag` | string | `1..80` characters |
| `roiLeft`, `roiTop`, `roiRight`, `roiBottom` | number | normalized `0..1`, ordered, area at least `1%` |
| `rotationDegrees` | integer | optional: `0`, `90`, `180`, `270` |

Response:

```json
{
  "objectId": "uuid",
  "referenceId": "uuid",
  "qualityScore": 0.5,
  "embeddingCount": 0,
  "warnings": [
    "Prototype engine only: image quality, crop and embeddings are not implemented."
  ]
}
```

Upload, validation, auth and object metadata persistence are wired. The current
`PrototypeEnrollmentEngine` deliberately does not crop, score or encode the image;
Thành viên 1 thay engine này nhưng giữ nguyên HTTP contract.

## One-shot recognition

```text
POST /recognitions
Content-Type: multipart/form-data
```

Fields: `image` JPEG, `candidates` JSON array, optional `minimumSimilarity`,
`maximumResults` and `rotationDegrees`. Each candidate contains `objectId`,
`referenceId`, `modelName`, `modelVersion` and numeric `embedding`.

```json
{
  "detections": [],
  "processingTimeMillis": 1,
  "modelVersion": "prototype-no-model",
  "warnings": [
    "Prototype engine only: image encoding and similarity matching are not implemented."
  ]
}
```

Thành viên 2 thay `PrototypeRecognitionEngine`; contract upload và candidate giữ nguyên.

## Last-seen memory

```text
POST /memory/sightings
POST /memory/find
GET  /memory/objects/{objectId}/timeline?limit=50
```

Record request example:

```json
{
  "objectId": "object-uuid",
  "detectedAt": "2026-08-30T04:30:00.000Z",
  "confidence": 0.91,
  "location": {
    "latitude": 10.7769,
    "longitude": 106.7009,
    "accuracyMeters": 8
  }
}
```

Find request does not accept an image:

```json
{ "query": "Balô của tôi" }
```

Thành viên 3 thay `PrototypeMemoryEngine` để implement dedup theo hai phút/khoảng
cách nhưng giữ nguyên các route.

## Secure vault

```text
POST /vault/seal
POST /vault/open
```

Seal accepts Base64 plaintext plus mandatory AAD:

```json
{
  "plaintextBase64": "QmFsb8O0",
  "associatedData": "object:object-uuid"
}
```

The response contains `AES-256-GCM`, `keyVersion`, Base64 nonce, ciphertext and
authentication tag. Copy that bundle to `/vault/open`; changing ciphertext, AAD,
tag or key version returns `CRYPTO_FAILURE`. Key prototype chỉ tồn tại trong memory;
Thành viên 4 thay `PrototypeVaultEngine` bằng key provider có version và persistence.

## Sightings

```text
POST /sightings/batch
GET  /objects/{id}/last-seen
GET  /objects/{id}/sightings?limit=50
```

```json
{
  "items": [
    {
      "id": "client-uuid",
      "objectId": "object-uuid",
      "detectedAt": "2026-08-29T12:30:00.000Z",
      "latitude": 10.7769,
      "longitude": 106.7009,
      "accuracyMeters": 8.5,
      "confidence": 0.91
    }
  ]
}
```

The server accepts at most 100 sightings per batch. Reusing the same sighting UUID is idempotent for the same account.
