import { Injectable } from '@nestjs/common';
import type {
  RecognitionEngine,
  RecognitionEngineInput,
  RecognitionEngineResult,
} from './recognition-engine.js';

@Injectable()
export class PrototypeRecognitionEngine implements RecognitionEngine {
  async recognize(
    _input: RecognitionEngineInput,
  ): Promise<RecognitionEngineResult> {
    return Promise.resolve({
      detections: [],
      modelVersion: 'prototype-no-model',
      warnings: [
        'Prototype engine only: image encoding and similarity matching are not implemented.',
      ],
    });
  }
}
