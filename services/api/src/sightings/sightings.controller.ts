import {
  Body,
  Controller,
  DefaultValuePipe,
  Get,
  ParseIntPipe,
  Post,
  Query,
  Param,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import { BatchSightingsDto } from './sightings.dto.js';
import { SightingsService } from './sightings.service.js';

@ApiBearerAuth()
@ApiTags('sightings')
@Controller()
export class SightingsController {
  constructor(private readonly sightings: SightingsService) {}

  @Post('sightings/batch')
  saveBatch(@CurrentUserId() userId: string, @Body() dto: BatchSightingsDto) {
    return this.sightings.saveBatch(userId, dto);
  }

  @Get('objects/:objectId/last-seen')
  getLatest(
    @CurrentUserId() userId: string,
    @Param('objectId') objectId: string,
  ) {
    return this.sightings.getLatest(userId, objectId);
  }

  @Get('objects/:objectId/sightings')
  getTimeline(
    @CurrentUserId() userId: string,
    @Param('objectId') objectId: string,
    @Query('limit', new DefaultValuePipe(50), ParseIntPipe) limit: number,
  ) {
    return this.sightings.getTimeline(userId, objectId, limit);
  }
}
