import { ConflictException, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ObjectsService } from '../objects/objects.service.js';
import { SightingEntity } from './sighting.entity.js';
import { BatchSightingsDto } from './sightings.dto.js';

@Injectable()
export class SightingsService {
  constructor(
    @InjectRepository(SightingEntity)
    private readonly sightings: Repository<SightingEntity>,
    private readonly objects: ObjectsService,
  ) {}

  async saveBatch(userId: string, dto: BatchSightingsDto) {
    const objectIds = [...new Set(dto.items.map((item) => item.objectId))];
    await Promise.all(objectIds.map((id) => this.objects.getOwned(userId, id)));

    let accepted = 0;
    for (const item of dto.items) {
      const existing = await this.sightings.findOneBy({ id: item.id });
      if (existing && existing.userId !== userId) {
        throw new ConflictException('Sighting id belongs to another account');
      }
      await this.sightings.save(
        this.sightings.create({
          ...existing,
          id: item.id,
          userId,
          objectId: item.objectId,
          detectedAt: new Date(item.detectedAt),
          latitude: item.latitude ?? null,
          longitude: item.longitude ?? null,
          accuracyMeters: item.accuracyMeters ?? null,
          confidence: item.confidence,
          evidenceAssetId: item.evidenceAssetId ?? null,
        }),
      );
      accepted += 1;
    }
    return { accepted };
  }

  async getLatest(userId: string, objectId: string) {
    await this.objects.getOwned(userId, objectId);
    return this.sightings.findOne({
      where: { userId, objectId },
      order: { detectedAt: 'DESC' },
    });
  }

  async getTimeline(userId: string, objectId: string, limit: number) {
    await this.objects.getOwned(userId, objectId);
    return this.sightings.find({
      where: { userId, objectId },
      order: { detectedAt: 'DESC' },
      take: Math.min(Math.max(limit, 1), 100),
    });
  }
}
