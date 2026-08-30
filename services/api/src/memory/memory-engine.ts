import type {
  FindMemoryResponseDto,
  MemorySightingDto,
  RecordMemoryResponseDto,
  RecordMemorySightingDto,
} from './memory.dto.js';

export const MEMORY_ENGINE = Symbol('MEMORY_ENGINE');

export interface MemoryEngine {
  record(
    userId: string,
    input: RecordMemorySightingDto,
  ): Promise<RecordMemoryResponseDto>;
  find(userId: string, query: string): Promise<FindMemoryResponseDto>;
  timeline(
    userId: string,
    objectId: string,
    limit: number,
  ): Promise<readonly MemorySightingDto[]>;
}
