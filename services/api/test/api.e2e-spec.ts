import { ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { randomUUID } from 'node:crypto';
import request from 'supertest';
import type { App } from 'supertest/types.js';
import { AppModule } from '../src/app.module.js';

describe('TRACE API (e2e)', () => {
  let app: Awaited<ReturnType<typeof createApp>>;
  let server: App;

  beforeAll(async () => {
    app = await createApp();
    server = app.getHttpServer() as App;
  });

  afterAll(async () => app.close());

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
