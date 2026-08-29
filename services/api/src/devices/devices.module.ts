import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DeviceEntity } from './device.entity.js';
import { DevicesController } from './devices.controller.js';
import { DevicesService } from './devices.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([DeviceEntity])],
  controllers: [DevicesController],
  providers: [DevicesService],
  exports: [TypeOrmModule],
})
export class DevicesModule {}
