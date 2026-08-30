import { Injectable } from '@nestjs/common';
import type {
  EnrollmentEngine,
  EnrollmentEngineInput,
  EnrollmentEngineResult,
} from './enrollment-engine.js';

@Injectable()
export class PrototypeEnrollmentEngine implements EnrollmentEngine {
  async process(
    _input: EnrollmentEngineInput,
  ): Promise<EnrollmentEngineResult> {
    return Promise.resolve({
      qualityScore: 0.5,
      embeddings: [],
      warnings: [
        'Prototype engine only: image quality, crop and embeddings are not implemented.',
      ],
    });
  }
}
