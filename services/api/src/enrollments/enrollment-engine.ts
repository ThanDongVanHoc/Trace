export const ENROLLMENT_ENGINE = Symbol('ENROLLMENT_ENGINE');

export interface NormalizedRoi {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export interface EnrollmentEngineInput {
  image: Buffer;
  mimeType: 'image/jpeg';
  rotationDegrees: 0 | 90 | 180 | 270;
  roi: NormalizedRoi;
}

export interface EnrollmentEmbedding {
  values: readonly number[];
  modelName: string;
  modelVersion: string;
}

export interface EnrollmentEngineResult {
  qualityScore: number;
  embeddings: readonly EnrollmentEmbedding[];
  warnings: readonly string[];
}

export interface EnrollmentEngine {
  process(input: EnrollmentEngineInput): Promise<EnrollmentEngineResult>;
}
