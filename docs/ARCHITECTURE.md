# TRACE architecture

## Runtime boundary

```text
Flutter mobile
  ├─ Camera + Location
  ├─ On-device visual model
  ├─ Encrypted local vault
  └─ Sync client
           │ HTTPS /v1
NestJS API
  ├─ Authentication + sessions
  ├─ Object/sighting sync
  ├─ Device registry
  └─ Notification outbox → FCM/APNs
           │
       PostgreSQL
```

Recognition images and embeddings remain local by default. The server syncs account data, object metadata and sightings. Evidence-image cloud upload is intentionally excluded until an explicit privacy and retention policy exists.

## Mobile dependency rule

Feature modules depend on `core/contracts`; they do not import another feature's implementation. Riverpod in `app/providers.dart` is the composition root that selects the active implementations.

## Security boundary

- Access token: 15 minutes by default.
- Refresh token: rotated on every use and stored hashed server-side.
- Mobile auth tokens: Keystore/Keychain through secure storage.
- Local private data: AES-256-GCM implementation owned by Secure Vault.
- Transport: HTTPS outside local development.
- Push credentials and signing keys: deployment secrets only.
