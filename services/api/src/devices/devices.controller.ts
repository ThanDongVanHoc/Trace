import { Body, Controller, Delete, HttpCode, Param, Put } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import { DevicesService } from './devices.service.js';
import { UpsertDeviceDto } from './upsert-device.dto.js';

@ApiBearerAuth()
@ApiTags('devices')
@Controller('devices')
export class DevicesController {
  constructor(private readonly devices: DevicesService) {}

  @Put(':installationId')
  upsert(
    @CurrentUserId() userId: string,
    @Param('installationId') installationId: string,
    @Body() dto: UpsertDeviceDto,
  ) {
    return this.devices.upsert(userId, installationId, dto);
  }

  @HttpCode(204)
  @Delete(':installationId')
  remove(
    @CurrentUserId() userId: string,
    @Param('installationId') installationId: string,
  ) {
    return this.devices.remove(userId, installationId);
  }
}
