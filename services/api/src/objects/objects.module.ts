import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ObjectsController } from './objects.controller.js';
import { ObjectsService } from './objects.service.js';
import { TraceObjectEntity } from './trace-object.entity.js';

@Module({
  imports: [TypeOrmModule.forFeature([TraceObjectEntity])],
  controllers: [ObjectsController],
  providers: [ObjectsService],
  exports: [ObjectsService],
})
export class ObjectsModule {}
