import {
  BadRequestException,
  Inject,
  Injectable,
  InternalServerErrorException,
} from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { ObjectsService } from '../objects/objects.service.js';
import {
  ENROLLMENT_ENGINE,
  type EnrollmentEmbedding,
  type EnrollmentEngine,
  type EnrollmentEngineResult,
  type NormalizedRoi,
} from './enrollment-engine.js';
import {
  CreateEnrollmentDto,
  EnrollmentResponseDto,
} from './enrollments.dto.js';

export interface UploadedEnrollmentImage {
  buffer: Buffer;
  mimetype: string;
  size: number;
}

@Injectable()
export class EnrollmentsService {
  constructor(
    @Inject(ENROLLMENT_ENGINE)
    private readonly engine: EnrollmentEngine,
    private readonly objects: ObjectsService,
  ) {}

  async enroll(
    userId: string,
    dto: CreateEnrollmentDto,
    image: UploadedEnrollmentImage,
  ): Promise<EnrollmentResponseDto> {
    this.assertJpeg(image);
    const roi = this.toValidRoi(dto);
    const engineResult = await this.engine.process({
      image: image.buffer,
      mimeType: 'image/jpeg',
      rotationDegrees: dto.rotationDegrees as 0 | 90 | 180 | 270,
      roi,
    });
    this.assertEngineResult(engineResult);

    const objectId = randomUUID();
    const referenceId = randomUUID();
    await this.objects.create(userId, {
      id: objectId,
      tag: dto.tag,
      referenceRevision: 1,
    });

    return {
      objectId,
      referenceId,
      qualityScore: engineResult.qualityScore,
      embeddingCount: engineResult.embeddings.length,
      warnings: [...engineResult.warnings],
    };
  }

  private assertJpeg(image: UploadedEnrollmentImage): void {
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

  private toValidRoi(dto: CreateEnrollmentDto): NormalizedRoi {
    const roi = {
      left: dto.roiLeft,
      top: dto.roiTop,
      right: dto.roiRight,
      bottom: dto.roiBottom,
    };
    const area = (roi.right - roi.left) * (roi.bottom - roi.top);
    if (
      roi.left >= roi.right ||
      roi.top >= roi.bottom ||
      area < MINIMUM_ROI_AREA
    ) {
      throw new BadRequestException(
        'ROI must be ordered and cover at least 1% of the image',
      );
    }
    return roi;
  }

  private assertEngineResult(result: EnrollmentEngineResult): void {
    if (
      !Number.isFinite(result.qualityScore) ||
      result.qualityScore < 0 ||
      result.qualityScore > 1 ||
      !Array.isArray(result.embeddings) ||
      !Array.isArray(result.warnings)
    ) {
      throw new InternalServerErrorException(
        'Enrollment engine returned an invalid result',
      );
    }
    if (result.embeddings.length === 0) return;

    const first = result.embeddings[0];
    if (!first || !this.isValidEmbedding(first)) {
      throw new InternalServerErrorException(
        'Enrollment engine returned an invalid embedding',
      );
    }
    const dimensions = first.values.length;
    const compatible = result.embeddings.every(
      (embedding) =>
        this.isValidEmbedding(embedding) &&
        embedding.modelName === first.modelName &&
        embedding.modelVersion === first.modelVersion &&
        embedding.values.length === dimensions,
    );
    if (!compatible) {
      throw new InternalServerErrorException(
        'Enrollment embeddings must use the same model, version and dimensions',
      );
    }
  }

  private isValidEmbedding(embedding: EnrollmentEmbedding): boolean {
    return (
      embedding.modelName.trim().length > 0 &&
      embedding.modelVersion.trim().length > 0 &&
      embedding.values.length > 0 &&
      embedding.values.every(Number.isFinite)
    );
  }
}

const MINIMUM_ROI_AREA = 0.01;
