import {
  plainToInstance,
  Transform,
  Type,
  type TransformFnParams,
} from 'class-transformer';
import {
  ArrayMaxSize,
  ArrayMinSize,
  IsArray,
  IsIn,
  IsInt,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Length,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';

const toNumber = ({ value }: TransformFnParams): unknown =>
  typeof value === 'number' ? value : Number(value);

const parseCandidates = ({ value }: TransformFnParams): unknown => {
  if (typeof value !== 'string') return value;
  try {
    const parsed = JSON.parse(value) as unknown;
    return Array.isArray(parsed)
      ? parsed.map((candidate) =>
          plainToInstance(RecognitionCandidateDto, candidate),
        )
      : parsed;
  } catch {
    return value;
  }
};

export class RecognitionCandidateDto {
  @IsUUID()
  objectId: string;

  @IsUUID()
  referenceId: string;

  @IsString()
  @Length(1, 100)
  modelName: string;

  @IsString()
  @Length(1, 100)
  modelVersion: string;

  @IsArray()
  @ArrayMinSize(1)
  @ArrayMaxSize(4096)
  @IsNumber({ allowInfinity: false, allowNaN: false }, { each: true })
  embedding: number[];
}

export class CreateRecognitionDto {
  @Transform(parseCandidates)
  @IsArray()
  @ArrayMinSize(1)
  @ArrayMaxSize(100)
  @ValidateNested({ each: true })
  @Type(() => RecognitionCandidateDto)
  candidates: RecognitionCandidateDto[];

  @IsOptional()
  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @Min(0)
  @Max(1)
  minimumSimilarity = 0.75;

  @IsOptional()
  @Transform(toNumber)
  @IsInt()
  @Min(1)
  @Max(20)
  maximumResults = 5;

  @IsOptional()
  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @IsIn([0, 90, 180, 270])
  rotationDegrees = 0;
}

export class RecognitionBoundingBoxDto {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export class RecognitionDetectionDto {
  objectId: string | null;
  referenceId: string | null;
  boundingBox: RecognitionBoundingBoxDto | null;
  similarity: number;
  status: 'MATCHED' | 'UNKNOWN';
}

export class RecognitionResponseDto {
  detections: RecognitionDetectionDto[];
  processingTimeMillis: number;
  modelVersion: string;
  warnings: string[];
}
