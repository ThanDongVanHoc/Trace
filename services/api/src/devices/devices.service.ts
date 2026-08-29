import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { DeviceEntity } from './device.entity.js';
import { UpsertDeviceDto } from './upsert-device.dto.js';

@Injectable()
export class DevicesService {
  constructor(
    @InjectRepository(DeviceEntity)
    private readonly devices: Repository<DeviceEntity>,
  ) {}

  async upsert(userId: string, installationId: string, dto: UpsertDeviceDto) {
    let device = await this.devices.findOneBy({ userId, installationId });
    device ??= this.devices.create({ userId, installationId });
    Object.assign(device, dto, {
      pushToken: dto.pushToken ?? null,
      lastSeenAt: new Date(),
    });
    return this.devices.save(device);
  }

  async remove(userId: string, installationId: string): Promise<void> {
    await this.devices.delete({ userId, installationId });
  }
}
