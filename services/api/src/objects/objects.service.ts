import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { randomUUID } from 'node:crypto';
import { IsNull, Repository } from 'typeorm';
import { CreateObjectDto, UpdateObjectDto } from './objects.dto.js';
import { TraceObjectEntity } from './trace-object.entity.js';

@Injectable()
export class ObjectsService {
  constructor(
    @InjectRepository(TraceObjectEntity)
    private readonly objects: Repository<TraceObjectEntity>,
  ) {}

  list(userId: string) {
    return this.objects.find({
      where: { userId, deletedAt: IsNull() },
      order: { updatedAt: 'DESC' },
    });
  }

  async create(userId: string, dto: CreateObjectDto) {
    const id = dto.id ?? randomUUID();
    const existing = await this.objects.findOne({
      where: { id },
      withDeleted: true,
    });
    if (existing) throw new ConflictException('Object id already exists');
    return this.objects.save(
      this.objects.create({
        id,
        userId,
        tag: dto.tag.trim(),
        referenceRevision: dto.referenceRevision,
      }),
    );
  }

  async update(userId: string, id: string, dto: UpdateObjectDto) {
    const object = await this.getOwned(userId, id);
    if (dto.tag !== undefined) object.tag = dto.tag.trim();
    if (dto.referenceRevision !== undefined) {
      object.referenceRevision = dto.referenceRevision;
    }
    object.version += 1;
    return this.objects.save(object);
  }

  async remove(userId: string, id: string): Promise<void> {
    const object = await this.getOwned(userId, id);
    await this.objects.softRemove(object);
  }

  async getOwned(userId: string, id: string): Promise<TraceObjectEntity> {
    const object = await this.objects.findOneBy({ id, userId });
    if (!object) throw new NotFoundException('Object not found');
    return object;
  }
}
