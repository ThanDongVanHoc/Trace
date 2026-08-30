import { Module } from '@nestjs/common';
import { ObjectsModule } from '../objects/objects.module.js';
import { ENROLLMENT_ENGINE } from './enrollment-engine.js';
import { EnrollmentsController } from './enrollments.controller.js';
import { EnrollmentsService } from './enrollments.service.js';
import { PrototypeEnrollmentEngine } from './prototype-enrollment.engine.js';

@Module({
  imports: [ObjectsModule],
  controllers: [EnrollmentsController],
  providers: [
    EnrollmentsService,
    PrototypeEnrollmentEngine,
    {
      provide: ENROLLMENT_ENGINE,
      useExisting: PrototypeEnrollmentEngine,
    },
  ],
})
export class EnrollmentsModule {}
