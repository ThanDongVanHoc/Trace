# TRACE API v1

Base URL: `/v1`. JSON only. Protected endpoints require `Authorization: Bearer <accessToken>`.

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
