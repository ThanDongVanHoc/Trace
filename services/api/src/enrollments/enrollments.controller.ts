import {
  BadRequestException,
  Body,
  Controller,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiBody,
  ApiConsumes,
  ApiCreatedResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import {
  CreateEnrollmentDto,
  EnrollmentResponseDto,
} from './enrollments.dto.js';
import {
  EnrollmentsService,
  type UploadedEnrollmentImage,
} from './enrollments.service.js';

const MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

@ApiBearerAuth()
@ApiTags('enrollments')
@Controller('enrollments')
export class EnrollmentsController {
  constructor(private readonly enrollments: EnrollmentsService) {}

  @Post()
  @ApiOperation({
    summary: 'Create a one-shot object reference from one tagged image',
  })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      required: ['image', 'tag', 'roiLeft', 'roiTop', 'roiRight', 'roiBottom'],
      properties: {
        image: { type: 'string', format: 'binary' },
        tag: { type: 'string', minLength: 1, maxLength: 80 },
        roiLeft: { type: 'number', minimum: 0, maximum: 1, example: 0.2 },
        roiTop: { type: 'number', minimum: 0, maximum: 1, example: 0.15 },
        roiRight: { type: 'number', minimum: 0, maximum: 1, example: 0.8 },
        roiBottom: { type: 'number', minimum: 0, maximum: 1, example: 0.9 },
        rotationDegrees: {
          type: 'integer',
          enum: [0, 90, 180, 270],
          default: 0,
        },
      },
    },
  })
  @ApiCreatedResponse({ type: EnrollmentResponseDto })
  @ApiBadRequestResponse({ description: 'Invalid image, tag or ROI' })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid access token' })
  @Throttle({ default: { limit: 10, ttl: 60_000 } })
  @UseInterceptors(
    FileInterceptor('image', {
      limits: { files: 1, fileSize: MAX_IMAGE_SIZE_BYTES },
    }),
  )
  create(
    @CurrentUserId() userId: string,
    @Body() dto: CreateEnrollmentDto,
    @UploadedFile() image?: UploadedEnrollmentImage,
  ): Promise<EnrollmentResponseDto> {
    if (!image) throw new BadRequestException('image is required');
    return this.enrollments.enroll(userId, dto, image);
  }
}
