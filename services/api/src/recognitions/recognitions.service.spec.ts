import { BadRequestException } from '@nestjs/common';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { RecognitionEngine } from './recognition-engine.js';
import type { CreateRecognitionDto } from './recognitions.dto.js';
import { RecognitionsService } from './recognitions.service.js';

describe('RecognitionsService', () => {
  const recognize = vi.fn<RecognitionEngine['recognize']>();
  const service = new RecognitionsService({ recognize });

  beforeEach(() => {
    vi.clearAllMocks();
    recognize.mockResolvedValue({
      detections: [],
      modelVersion: 'test-model-1',
      warnings: [],
    });
  });

  it('passes normalized request data to the engine', async () => {
    const dto = validDto();
    await service.recognize(dto, validJpeg());

    expect(recognize).toHaveBeenCalledWith({
      image: validJpeg().buffer,
      mimeType: 'image/jpeg',
      rotationDegrees: 0,
      candidates: dto.candidates,
      minimumSimilarity: 0.75,
      maximumResults: 5,
    });
  });

  it('rejects a non-JPEG before calling the engine', async () => {
    await expect(
      service.recognize(validDto(), {
        buffer: Buffer.from('bad'),
        mimetype: 'image/jpeg',
        size: 3,
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
    expect(recognize).not.toHaveBeenCalled();
  });
});

function validDto(): CreateRecognitionDto {
  return {
    candidates: [
      {
        objectId: '11111111-1111-4111-8111-111111111111',
        referenceId: '22222222-2222-4222-8222-222222222222',
        modelName: 'clip',
        modelVersion: '1',
        embedding: [0.1, 0.2],
      },
    ],
    minimumSimilarity: 0.75,
    maximumResults: 5,
    rotationDegrees: 0,
  };
}

function validJpeg() {
  const buffer = Buffer.from([0xff, 0xd8, 0xff, 0xd9]);
  return { buffer, mimetype: 'image/jpeg', size: buffer.length };
}
