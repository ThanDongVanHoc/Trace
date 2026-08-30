import { Module } from '@nestjs/common';
import { ObjectsModule } from '../objects/objects.module.js';
import { SightingsModule } from '../sightings/sightings.module.js';
import { MEMORY_ENGINE } from './memory-engine.js';
import { MemoryController } from './memory.controller.js';
import { MemoryService } from './memory.service.js';
import { PrototypeMemoryEngine } from './prototype-memory.engine.js';

@Module({
  imports: [ObjectsModule, SightingsModule],
  controllers: [MemoryController],
  providers: [
    MemoryService,
    PrototypeMemoryEngine,
    { provide: MEMORY_ENGINE, useExisting: PrototypeMemoryEngine },
  ],
})
export class MemoryModule {}
