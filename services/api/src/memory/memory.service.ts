import { BadRequestException, Inject, Injectable } from '@nestjs/common';
import { MEMORY_ENGINE, type MemoryEngine } from './memory-engine.js';
import type {
  FindMemoryResponseDto,
  MemorySightingDto,
  RecordMemoryResponseDto,
  RecordMemorySightingDto,
} from './memory.dto.js';

@Injectable()
export class MemoryService {
  constructor(
    @Inject(MEMORY_ENGINE)
    private readonly engine: MemoryEngine,
  ) {}

  record(
    userId: string,
    input: RecordMemorySightingDto,
  ): Promise<RecordMemoryResponseDto> {
    const box = input.boundingBox;
    if (box && (box.left >= box.right || box.top >= box.bottom)) {
      throw new BadRequestException('boundingBox must be ordered');
    }
    return this.engine.record(userId, input);
  }

  find(userId: string, query: string): Promise<FindMemoryResponseDto> {
    return this.engine.find(userId, query.trim());
  }

  async timeline(
    userId: string,
    objectId: string,
    limit: number,
  ): Promise<MemorySightingDto[]> {
    return [...(await this.engine.timeline(userId, objectId, limit))];
  }
}
