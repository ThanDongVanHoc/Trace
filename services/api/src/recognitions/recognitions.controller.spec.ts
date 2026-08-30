import { ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import {
  afterAll,
  beforeAll,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';
import { RecognitionsController } from './recognitions.controller.js';
import { RecognitionsService } from './recognitions.service.js';

describe('RecognitionsController', () => {
  const recognize = vi.fn();
  let app: Awaited<ReturnType<typeof createTestApp>>;

  beforeAll(async () => {
    app = await createTestApp(recognize);
  });

  beforeEach(() => {
    vi.clearAllMocks();
    recognize.mockResolvedValue({
      detections: [],
      processingTimeMillis: 1,
      modelVersion: 'prototype-no-model',
      warnings: ['prototype'],
    });
  });

  afterAll(async () => app.close());

  it('accepts a JPEG and candidate embeddings as multipart form data', async () => {
    const candidates = [
      {
        objectId: '11111111-1111-4111-8111-111111111111',
        referenceId: '22222222-2222-4222-8222-222222222222',
        modelName: 'clip',
        modelVersion: '1',
        embedding: [0.1, 0.2, 0.3],
      },
    ];
    const response = await request(app.getHttpServer())
      .post('/v1/recognitions')
      .field('candidates', JSON.stringify(candidates))
      .field('minimumSimilarity', '0.8')
      .attach('image', Buffer.from([0xff, 0xd8, 0xff, 0xd9]), {
        filename: 'query.jpg',
        contentType: 'image/jpeg',
      });

    expect(response.status, JSON.stringify(response.body)).toBe(201);

    expect(recognize).toHaveBeenCalledOnce();
    expect(recognize.mock.calls[0]?.[0]).toEqual({
      candidates,
      minimumSimilarity: 0.8,
      maximumResults: 5,
      rotationDegrees: 0,
    });
  });

  it('rejects malformed candidate JSON', async () => {
    await request(app.getHttpServer())
      .post('/v1/recognitions')
      .field('candidates', 'not-json')
      .attach('image', Buffer.from([0xff, 0xd8, 0xff, 0xd9]), {
        filename: 'query.jpg',
        contentType: 'image/jpeg',
      })
      .expect(400);

    expect(recognize).not.toHaveBeenCalled();
  });
});

async function createTestApp(recognize: ReturnType<typeof vi.fn>) {
  const moduleFixture = await Test.createTestingModule({
    controllers: [RecognitionsController],
    providers: [{ provide: RecognitionsService, useValue: { recognize } }],
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
