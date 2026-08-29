import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ObjectsModule } from '../objects/objects.module.js';
import { SightingEntity } from './sighting.entity.js';
import { SightingsController } from './sightings.controller.js';
import { SightingsService } from './sightings.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([SightingEntity]), ObjectsModule],
  controllers: [SightingsController],
  providers: [SightingsService],
})
export class SightingsModule {}
