import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import { CreateObjectDto, UpdateObjectDto } from './objects.dto.js';
import { ObjectsService } from './objects.service.js';

@ApiBearerAuth()
@ApiTags('objects')
@Controller('objects')
export class ObjectsController {
  constructor(private readonly objects: ObjectsService) {}

  @Get()
  list(@CurrentUserId() userId: string) {
    return this.objects.list(userId);
  }

  @Post()
  create(@CurrentUserId() userId: string, @Body() dto: CreateObjectDto) {
    return this.objects.create(userId, dto);
  }

  @Patch(':id')
  update(
    @CurrentUserId() userId: string,
    @Param('id') id: string,
    @Body() dto: UpdateObjectDto,
  ) {
    return this.objects.update(userId, id, dto);
  }

  @HttpCode(204)
  @Delete(':id')
  remove(@CurrentUserId() userId: string, @Param('id') id: string) {
    return this.objects.remove(userId, id);
  }
}
