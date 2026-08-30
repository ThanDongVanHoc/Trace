import { Module } from '@nestjs/common';
import { RECOGNITION_ENGINE } from './recognition-engine.js';
import { PrototypeRecognitionEngine } from './prototype-recognition.engine.js';
import { RecognitionsController } from './recognitions.controller.js';
import { RecognitionsService } from './recognitions.service.js';

@Module({
  controllers: [RecognitionsController],
  providers: [
    RecognitionsService,
    PrototypeRecognitionEngine,
    {
      provide: RECOGNITION_ENGINE,
      useExisting: PrototypeRecognitionEngine,
    },
  ],
})
export class RecognitionsModule {}
