import { ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { randomUUID } from 'node:crypto';
import request from 'supertest';
import type { App } from 'supertest/types.js';
import { AppModule } from '../src/app.module.js';

describe('TRACE API (e2e)', () => {
  let app: Awaited<ReturnType<typeof createApp>> | undefined;
  let server: App;

  beforeAll(async () => {
    app = await createApp();
    server = app.getHttpServer() as App;
  }, 30_000);

  afterAll(async () => app?.close());

  it('registers, refreshes, syncs an object and reads last-seen', async () => {
    const email = `e2e-${randomUUID()}@example.com`;
    const registered = await request(server)
      .post('/v1/auth/register')
      .send({
        email,
        password: 'correct horse battery staple',
        displayName: 'E2E User',
      })
      .expect(201);

    const accessToken = registered.body.accessToken as string;
    const refreshToken = registered.body.refreshToken as string;
    expect(accessToken).toBeTruthy();

    const refreshed = await request(server)
      .post('/v1/auth/refresh')
      .send({ refreshToken })
      .expect(200);
    expect(refreshed.body.refreshToken).not.toBe(refreshToken);

    const enrollment = await request(server)
      .post('/v1/enrollments')
      .set('Authorization', `Bearer ${accessToken}`)
      .field('tag', 'E2E tagged backpack')
      .field('roiLeft', '0.2')
      .field('roiTop', '0.15')
      .field('roiRight', '0.8')
      .field('roiBottom', '0.9')
      .field('rotationDegrees', '0')
      .attach('image', Buffer.from([0xff, 0xd8, 0xff, 0xd9]), {
        filename: 'backpack.jpg',
        contentType: 'image/jpeg',
      })
      .expect(201);
    expect(enrollment.body.objectId).toBeTruthy();
    expect(enrollment.body.referenceId).toBeTruthy();
    expect(enrollment.body.warnings).toContain(
      'Prototype engine only: image quality, crop and embeddings are not implemented.',
    );

    const recognition = await request(server)
      .post('/v1/recognitions')
      .set('Authorization', `Bearer ${accessToken}`)
      .field(
        'candidates',
        JSON.stringify([
          {
            objectId: enrollment.body.objectId,
            referenceId: enrollment.body.referenceId,
            modelName: 'e2e-model',
            modelVersion: '1',
            embedding: [0.1, 0.2, 0.3],
          },
        ]),
      )
      .attach('image', Buffer.from([0xff, 0xd8, 0xff, 0xd9]), {
        filename: 'query.jpg',
        contentType: 'image/jpeg',
      })
      .expect(201);
    expect(recognition.body.modelVersion).toBe('prototype-no-model');

    const memorySighting = await request(server)
      .post('/v1/memory/sightings')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({
        objectId: enrollment.body.objectId,
        detectedAt: '2026-08-30T04:30:00.000Z',
        confidence: 0.91,
        location: {
          latitude: 10.7769,
          longitude: 106.7009,
          accuracyMeters: 8,
        },
      })
      .expect(201);
    const found = await request(server)
      .post('/v1/memory/find')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({ query: 'E2E tagged backpack' })
      .expect(201);
    expect(found.body.lastSeen.id).toBe(memorySighting.body.sightingId);

    const plaintextBase64 = Buffer.from('e2e secret').toString('base64');
    const sealed = await request(server)
      .post('/v1/vault/seal')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({ plaintextBase64, associatedData: 'e2e:asset' })
      .expect(201);
    const opened = await request(server)
      .post('/v1/vault/open')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({
        nonceBase64: sealed.body.nonceBase64,
        ciphertextBase64: sealed.body.ciphertextBase64,
        authenticationTagBase64: sealed.body.authenticationTagBase64,
        associatedData: 'e2e:asset',
        keyVersion: sealed.body.keyVersion,
      })
      .expect(201);
    expect(opened.body.plaintextBase64).toBe(plaintextBase64);

    const objectId = randomUUID();
    await request(server)
      .post('/v1/objects')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({ id: objectId, tag: 'E2E backpack', referenceRevision: 1 })
      .expect(201);

    const sightingId = randomUUID();
    await request(server)
      .post('/v1/sightings/batch')
      .set('Authorization', `Bearer ${accessToken}`)
      .send({
        items: [
          {
            id: sightingId,
            objectId,
            detectedAt: '2026-08-29T12:30:00.000Z',
            latitude: 10.7769,
            longitude: 106.7009,
            accuracyMeters: 8,
            confidence: 0.91,
          },
        ],
      })
      .expect(201);

    const lastSeen = await request(server)
      .get(`/v1/objects/${objectId}/last-seen`)
      .set('Authorization', `Bearer ${accessToken}`)
      .expect(200);
    expect(lastSeen.body.id).toBe(sightingId);
  });
});

async function createApp() {
  const moduleFixture = await Test.createTestingModule({
    imports: [AppModule],
  }).compile();
  const app = moduleFixture.createNestApplication();
  app.setGlobalPrefix('v1');
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );
  await app.init();
  return app;
}
