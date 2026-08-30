import {
  BadRequestException,
  Inject,
  Injectable,
  InternalServerErrorException,
} from '@nestjs/common';
import {
  RECOGNITION_ENGINE,
  type RecognitionEngine,
} from './recognition-engine.js';
import {
  CreateRecognitionDto,
  RecognitionResponseDto,
} from './recognitions.dto.js';

export interface UploadedRecognitionImage {
  buffer: Buffer;
  mimetype: string;
  size: number;
}

@Injectable()
export class RecognitionsService {
  constructor(
    @Inject(RECOGNITION_ENGINE)
    private readonly engine: RecognitionEngine,
  ) {}

  async recognize(
    dto: CreateRecognitionDto,
    image: UploadedRecognitionImage,
  ): Promise<RecognitionResponseDto> {
    this.assertJpeg(image);
    const startedAt = performance.now();
    const result = await this.engine.recognize({
      image: image.buffer,
      mimeType: 'image/jpeg',
      rotationDegrees: dto.rotationDegrees as 0 | 90 | 180 | 270,
      candidates: dto.candidates,
      minimumSimilarity: dto.minimumSimilarity,
      maximumResults: dto.maximumResults,
    });
    this.assertResult(result.detections, result.modelVersion);
    return {
      detections: [...result.detections].slice(0, dto.maximumResults),
      processingTimeMillis: Math.max(
        0,
        Math.round(performance.now() - startedAt),
      ),
      modelVersion: result.modelVersion,
      warnings: [...result.warnings],
    };
  }

  private assertJpeg(image: UploadedRecognitionImage): void {
    const bytes = image.buffer;
    const hasJpegSignature =
      bytes.length >= 3 &&
      bytes[0] === 0xff &&
      bytes[1] === 0xd8 &&
      bytes[2] === 0xff;
    if (image.mimetype !== 'image/jpeg' || !hasJpegSignature) {
      throw new BadRequestException('image must be a valid JPEG file');
    }
  }

  private assertResult(
    detections: readonly RecognitionResponseDto['detections'][number][],
    modelVersion: string,
  ): void {
    const valid =
      modelVersion.trim().length > 0 &&
      detections.every((detection) => {
        const box = detection.boundingBox;
        const validBox =
          box === null ||
          (box.left >= 0 &&
            box.top >= 0 &&
            box.right <= 1 &&
            box.bottom <= 1 &&
            box.left < box.right &&
            box.top < box.bottom);
        return (
          Number.isFinite(detection.similarity) &&
          detection.similarity >= 0 &&
          detection.similarity <= 1 &&
          validBox &&
          (detection.status === 'MATCHED' || detection.status === 'UNKNOWN') &&
          (detection.status !== 'MATCHED' ||
            (detection.objectId !== null && detection.referenceId !== null))
        );
      });
    if (!valid) {
      throw new InternalServerErrorException(
        'Recognition engine returned an invalid result',
      );
    }
  }
}
