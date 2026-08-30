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
import {
  CreateRecognitionDto,
  RecognitionResponseDto,
} from './recognitions.dto.js';
import {
  RecognitionsService,
  type UploadedRecognitionImage,
} from './recognitions.service.js';

const MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

@ApiBearerAuth()
@ApiTags('recognitions')
@Controller('recognitions')
export class RecognitionsController {
  constructor(private readonly recognitions: RecognitionsService) {}

  @Post()
  @ApiOperation({
    summary: 'Match a new JPEG against supplied one-shot references',
  })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      required: ['image', 'candidates'],
      properties: {
        image: { type: 'string', format: 'binary' },
        candidates: {
          type: 'string',
          description: 'JSON array of candidate reference embeddings',
          example:
            '[{"objectId":"11111111-1111-4111-8111-111111111111","referenceId":"22222222-2222-4222-8222-222222222222","modelName":"clip","modelVersion":"1","embedding":[0.1,0.2,0.3]}]',
        },
        minimumSimilarity: { type: 'number', default: 0.75 },
        maximumResults: { type: 'integer', default: 5 },
        rotationDegrees: {
          type: 'integer',
          enum: [0, 90, 180, 270],
          default: 0,
        },
      },
    },
  })
  @ApiCreatedResponse({ type: RecognitionResponseDto })
  @ApiBadRequestResponse({
    description: 'Invalid image or candidate references',
  })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid access token' })
  @Throttle({ default: { limit: 10, ttl: 60_000 } })
  @UseInterceptors(
    FileInterceptor('image', {
      limits: {
        files: 1,
        fileSize: MAX_IMAGE_SIZE_BYTES,
        fieldSize: 2 * 1024 * 1024,
      },
    }),
  )
  create(
    @Body() dto: CreateRecognitionDto,
    @UploadedFile() image?: UploadedRecognitionImage,
  ): Promise<RecognitionResponseDto> {
    if (!image) throw new BadRequestException('image is required');
    return this.recognitions.recognize(dto, image);
  }
}
