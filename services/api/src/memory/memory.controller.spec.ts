import { ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import type { NextFunction, Response } from 'express';
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
import type { AuthenticatedRequest } from '../auth/guards/access-token.guard.js';
import { MemoryController } from './memory.controller.js';
import { MemoryService } from './memory.service.js';

describe('MemoryController', () => {
  const record = vi.fn();
  const find = vi.fn();
  const timeline = vi.fn();
  let app: Awaited<ReturnType<typeof createTestApp>>;

  beforeAll(async () => {
    app = await createTestApp({ record, find, timeline });
  });

  beforeEach(() => {
    vi.clearAllMocks();
    record.mockResolvedValue({
      sightingId: '33333333-3333-4333-8333-333333333333',
      created: true,
      deduplicatedWith: null,
      warnings: ['prototype'],
    });
    find.mockResolvedValue({
      objectId: '11111111-1111-4111-8111-111111111111',
      tag: 'My backpack',
      lastSeen: null,
      warnings: [],
    });
    timeline.mockResolvedValue([]);
  });

  afterAll(async () => app.close());

  it('records a confirmed sighting from JSON', async () => {
    const body = {
      objectId: '11111111-1111-4111-8111-111111111111',
      detectedAt: '2026-08-30T04:30:00.000Z',
      confidence: 0.91,
      location: {
        latitude: 10.7769,
        longitude: 106.7009,
        accuracyMeters: 8,
      },
    };
    await request(app.getHttpServer())
      .post('/v1/memory/sightings')
      .send(body)
      .expect(201);

    expect(record).toHaveBeenCalledWith('test-user-id', body);
  });

  it('finds by a human tag without requiring an image', async () => {
    await request(app.getHttpServer())
      .post('/v1/memory/find')
      .send({ query: 'My backpack' })
      .expect(201);

    expect(find).toHaveBeenCalledWith('test-user-id', 'My backpack');
  });
});

async function createTestApp(methods: {
  record: ReturnType<typeof vi.fn>;
  find: ReturnType<typeof vi.fn>;
  timeline: ReturnType<typeof vi.fn>;
}) {
  const moduleFixture = await Test.createTestingModule({
    controllers: [MemoryController],
    providers: [{ provide: MemoryService, useValue: methods }],
  }).compile();
  const app = moduleFixture.createNestApplication();
  app.setGlobalPrefix('v1');
  app.use(
    (
      incomingRequest: AuthenticatedRequest,
      _response: Response,
      next: NextFunction,
    ) => {
      incomingRequest.auth = {
        userId: 'test-user-id',
        sessionId: 'test-session-id',
      };
      next();
    },
  );
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
