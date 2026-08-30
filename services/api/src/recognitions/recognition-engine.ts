import type {
  RecognitionDetectionDto,
  RecognitionCandidateDto,
} from './recognitions.dto.js';

export const RECOGNITION_ENGINE = Symbol('RECOGNITION_ENGINE');

export interface RecognitionEngineInput {
  image: Buffer;
  mimeType: 'image/jpeg';
  rotationDegrees: 0 | 90 | 180 | 270;
  candidates: readonly RecognitionCandidateDto[];
  minimumSimilarity: number;
  maximumResults: number;
}

export interface RecognitionEngineResult {
  detections: readonly RecognitionDetectionDto[];
  modelVersion: string;
  warnings: readonly string[];
}

export interface RecognitionEngine {
  recognize(input: RecognitionEngineInput): Promise<RecognitionEngineResult>;
}
