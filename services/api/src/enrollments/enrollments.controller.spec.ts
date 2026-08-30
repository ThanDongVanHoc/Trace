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
import { EnrollmentsController } from './enrollments.controller.js';
import { EnrollmentsService } from './enrollments.service.js';

describe('EnrollmentsController', () => {
  const enroll = vi.fn();
  let app: Awaited<ReturnType<typeof createTestApp>>;

  beforeAll(async () => {
    app = await createTestApp(enroll);
  });

  beforeEach(() => {
    vi.clearAllMocks();
    enroll.mockResolvedValue({
      objectId: '4c819090-0d94-45d2-802e-b3e97492ab40',
      referenceId: '660d71e7-611c-45bc-bbb1-4d06b04f4d63',
      qualityScore: 0.5,
      embeddingCount: 0,
      warnings: ['prototype'],
    });
  });

  afterAll(async () => app.close());

  it('accepts a multipart JPEG, tag and normalized ROI', async () => {
    const response = await request(app.getHttpServer())
      .post('/v1/enrollments')
      .field('tag', 'My backpack')
      .field('roiLeft', '0.2')
      .field('roiTop', '0.15')
      .field('roiRight', '0.8')
      .field('roiBottom', '0.9')
      .attach('image', Buffer.from([0xff, 0xd8, 0xff, 0xd9]), {
        filename: 'backpack.jpg',
        contentType: 'image/jpeg',
      })
      .expect(201);

    expect(response.body.embeddingCount).toBe(0);
    expect(enroll).toHaveBeenCalledOnce();
    expect(enroll.mock.calls[0]?.[0]).toBe('test-user-id');
    expect(enroll.mock.calls[0]?.[1]).toEqual({
      tag: 'My backpack',
      roiLeft: 0.2,
      roiTop: 0.15,
      roiRight: 0.8,
      roiBottom: 0.9,
      rotationDegrees: 0,
    });
    expect(enroll.mock.calls[0]?.[2]).toMatchObject({
      mimetype: 'image/jpeg',
      size: 4,
    });
  });

  it('rejects a request without an image', async () => {
    await request(app.getHttpServer())
      .post('/v1/enrollments')
      .field('tag', 'My backpack')
      .field('roiLeft', '0.2')
      .field('roiTop', '0.15')
      .field('roiRight', '0.8')
      .field('roiBottom', '0.9')
      .expect(400);

    expect(enroll).not.toHaveBeenCalled();
  });

  it('rejects an image larger than 10 MiB before calling the service', async () => {
    await request(app.getHttpServer())
      .post('/v1/enrollments')
      .field('tag', 'My backpack')
      .field('roiLeft', '0.2')
      .field('roiTop', '0.15')
      .field('roiRight', '0.8')
      .field('roiBottom', '0.9')
      .attach('image', Buffer.alloc(10 * 1024 * 1024 + 1, 1), {
        filename: 'oversized.jpg',
        contentType: 'image/jpeg',
      })
      .expect(413);

    expect(enroll).not.toHaveBeenCalled();
  });
});

async function createTestApp(enroll: ReturnType<typeof vi.fn>) {
  const moduleFixture = await Test.createTestingModule({
    controllers: [EnrollmentsController],
    providers: [{ provide: EnrollmentsService, useValue: { enroll } }],
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
