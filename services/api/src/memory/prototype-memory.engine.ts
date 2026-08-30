import { Injectable, NotFoundException } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { ObjectsService } from '../objects/objects.service.js';
import type { SightingEntity } from '../sightings/sighting.entity.js';
import { SightingsService } from '../sightings/sightings.service.js';
import type { MemoryEngine } from './memory-engine.js';
import type {
  FindMemoryResponseDto,
  MemorySightingDto,
  RecordMemoryResponseDto,
  RecordMemorySightingDto,
} from './memory.dto.js';

@Injectable()
export class PrototypeMemoryEngine implements MemoryEngine {
  constructor(
    private readonly objects: ObjectsService,
    private readonly sightings: SightingsService,
  ) {}

  async record(
    userId: string,
    input: RecordMemorySightingDto,
  ): Promise<RecordMemoryResponseDto> {
    const sightingId = randomUUID();
    await this.sightings.saveBatch(userId, {
      items: [
        {
          id: sightingId,
          objectId: input.objectId,
          detectedAt: input.detectedAt,
          latitude: input.location?.latitude ?? null,
          longitude: input.location?.longitude ?? null,
          accuracyMeters: input.location?.accuracyMeters ?? null,
          confidence: input.confidence,
          evidenceAssetId: input.evidenceAssetId ?? null,
        },
      ],
    });
    return {
      sightingId,
      created: true,
      deduplicatedWith: null,
      warnings: [
        'Prototype engine only: the two-minute and distance deduplication rules are not implemented.',
      ],
    };
  }

  async find(userId: string, query: string): Promise<FindMemoryResponseDto> {
    const normalized = query.trim().toLocaleLowerCase();
    const objects = await this.objects.list(userId);
    const object =
      objects.find(
        (candidate) =>
          candidate.id === query ||
          candidate.tag.toLocaleLowerCase() === normalized,
      ) ??
      objects.find((candidate) =>
        candidate.tag.toLocaleLowerCase().includes(normalized),
      );
    if (!object) throw new NotFoundException('Object not found');
    const latest = await this.sightings.getLatest(userId, object.id);
    return {
      objectId: object.id,
      tag: object.tag,
      lastSeen: latest ? this.toDto(latest) : null,
      warnings: [],
    };
  }

  async timeline(
    userId: string,
    objectId: string,
    limit: number,
  ): Promise<readonly MemorySightingDto[]> {
    const sightings = await this.sightings.getTimeline(userId, objectId, limit);
    return sightings.map((sighting) => this.toDto(sighting));
  }

  private toDto(sighting: SightingEntity): MemorySightingDto {
    return {
      id: sighting.id,
      objectId: sighting.objectId,
      detectedAt: sighting.detectedAt.toISOString(),
      latitude: sighting.latitude,
      longitude: sighting.longitude,
      accuracyMeters: sighting.accuracyMeters,
      confidence: sighting.confidence,
      evidenceAssetId: sighting.evidenceAssetId,
    };
  }
}
