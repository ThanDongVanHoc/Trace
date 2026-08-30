import {
  BadRequestException,
  InternalServerErrorException,
} from '@nestjs/common';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { ObjectsService } from '../objects/objects.service.js';
import type { EnrollmentEngine } from './enrollment-engine.js';
import type { CreateEnrollmentDto } from './enrollments.dto.js';
import { EnrollmentsService } from './enrollments.service.js';

describe('EnrollmentsService', () => {
  const process = vi.fn<EnrollmentEngine['process']>();
  const createObject = vi.fn();
  const engine = { process } satisfies EnrollmentEngine;
  const objects = {
    create: createObject,
  } as unknown as ObjectsService;
  const service = new EnrollmentsService(engine, objects);

  beforeEach(() => {
    vi.clearAllMocks();
    process.mockResolvedValue({
      qualityScore: 0.91,
      embeddings: [
        {
          values: [0.1, 0.2, 0.3],
          modelName: 'test-encoder',
          modelVersion: '1',
        },
      ],
      warnings: [],
    });
    createObject.mockResolvedValue({});
  });

  it('processes one JPEG and persists the object metadata', async () => {
    const result = await service.enroll('user-id', validDto(), validJpeg());

    expect(process).toHaveBeenCalledWith({
      image: validJpeg().buffer,
      mimeType: 'image/jpeg',
      rotationDegrees: 90,
      roi: { left: 0.2, top: 0.15, right: 0.8, bottom: 0.9 },
    });
    expect(createObject).toHaveBeenCalledWith('user-id', {
      id: result.objectId,
      tag: 'My backpack',
      referenceRevision: 1,
    });
    expect(result.referenceId).toBeTruthy();
    expect(result.qualityScore).toBe(0.91);
    expect(result.embeddingCount).toBe(1);
  });

  it('rejects a MIME-spoofed or non-JPEG upload before processing', async () => {
    await expect(
      service.enroll('user-id', validDto(), {
        buffer: Buffer.from('not-a-jpeg'),
        mimetype: 'image/jpeg',
        size: 10,
      }),
    ).rejects.toBeInstanceOf(BadRequestException);

    expect(process).not.toHaveBeenCalled();
    expect(createObject).not.toHaveBeenCalled();
  });

  it('rejects an unordered or too-small ROI before processing', async () => {
    await expect(
      service.enroll(
        'user-id',
        { ...validDto(), roiRight: 0.205 },
        validJpeg(),
      ),
    ).rejects.toBeInstanceOf(BadRequestException);

    expect(process).not.toHaveBeenCalled();
    expect(createObject).not.toHaveBeenCalled();
  });

  it('rejects incompatible embeddings and does not persist an object', async () => {
    process.mockResolvedValue({
      qualityScore: 0.8,
      embeddings: [
        {
          values: [0.1, 0.2],
          modelName: 'encoder',
          modelVersion: '1',
        },
        {
          values: [0.1, 0.2, 0.3],
          modelName: 'encoder',
          modelVersion: '1',
        },
      ],
      warnings: [],
    });

    await expect(
      service.enroll('user-id', validDto(), validJpeg()),
    ).rejects.toBeInstanceOf(InternalServerErrorException);
    expect(createObject).not.toHaveBeenCalled();
  });
});

function validDto(): CreateEnrollmentDto {
  return {
    tag: 'My backpack',
    roiLeft: 0.2,
    roiTop: 0.15,
    roiRight: 0.8,
    roiBottom: 0.9,
    rotationDegrees: 90,
  };
}

function validJpeg() {
  const buffer = Buffer.from([0xff, 0xd8, 0xff, 0xd9]);
  return { buffer, mimetype: 'image/jpeg', size: buffer.length };
}
