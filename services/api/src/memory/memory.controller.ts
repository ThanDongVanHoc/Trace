import {
  Body,
  Controller,
  DefaultValuePipe,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import {
  FindMemoryDto,
  FindMemoryResponseDto,
  MemorySightingDto,
  RecordMemoryResponseDto,
  RecordMemorySightingDto,
} from './memory.dto.js';
import { MemoryService } from './memory.service.js';

@ApiBearerAuth()
@ApiTags('memory')
@Controller('memory')
export class MemoryController {
  constructor(private readonly memory: MemoryService) {}

  @Post('sightings')
  @ApiOperation({ summary: 'Record one confirmed object sighting' })
  @ApiCreatedResponse({ type: RecordMemoryResponseDto })
  record(
    @CurrentUserId() userId: string,
    @Body() dto: RecordMemorySightingDto,
  ): Promise<RecordMemoryResponseDto> {
    return this.memory.record(userId, dto);
  }

  @Post('find')
  @ApiOperation({ summary: 'Find the last-seen record by tag or object UUID' })
  @ApiCreatedResponse({ type: FindMemoryResponseDto })
  find(
    @CurrentUserId() userId: string,
    @Body() dto: FindMemoryDto,
  ): Promise<FindMemoryResponseDto> {
    return this.memory.find(userId, dto.query);
  }

  @Get('objects/:objectId/timeline')
  @ApiOperation({ summary: 'Read a newest-first object sighting timeline' })
  @ApiOkResponse({ type: MemorySightingDto, isArray: true })
  timeline(
    @CurrentUserId() userId: string,
    @Param('objectId') objectId: string,
    @Query('limit', new DefaultValuePipe(50), ParseIntPipe) limit: number,
  ): Promise<MemorySightingDto[]> {
    return this.memory.timeline(
      userId,
      objectId,
      Math.min(Math.max(limit, 1), 100),
    );
  }
}
