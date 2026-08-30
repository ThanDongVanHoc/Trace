import { Type } from 'class-transformer';
import {
  IsISO8601,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Length,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';

export class MemoryBoundingBoxDto {
  @IsNumber()
  @Min(0)
  @Max(1)
  left: number;

  @IsNumber()
  @Min(0)
  @Max(1)
  top: number;

  @IsNumber()
  @Min(0)
  @Max(1)
  right: number;

  @IsNumber()
  @Min(0)
  @Max(1)
  bottom: number;
}

export class MemoryLocationDto {
  @IsLatitude()
  latitude: number;

  @IsLongitude()
  longitude: number;

  @IsNumber()
  @Min(0)
  accuracyMeters: number;
}

export class RecordMemorySightingDto {
  @IsUUID()
  objectId: string;

  @IsISO8601()
  detectedAt: string;

  @IsNumber()
  @Min(0)
  @Max(1)
  confidence: number;

  @IsOptional()
  @ValidateNested()
  @Type(() => MemoryBoundingBoxDto)
  boundingBox?: MemoryBoundingBoxDto | null;

  @IsOptional()
  @ValidateNested()
  @Type(() => MemoryLocationDto)
  location?: MemoryLocationDto | null;

  @IsOptional()
  @IsUUID()
  evidenceAssetId?: string | null;
}

export class FindMemoryDto {
  @IsString()
  @Length(1, 80)
  query: string;
}

export class MemorySightingDto {
  id: string;
  objectId: string;
  detectedAt: string;
  latitude: number | null;
  longitude: number | null;
  accuracyMeters: number | null;
  confidence: number;
  evidenceAssetId: string | null;
}

export class RecordMemoryResponseDto {
  sightingId: string;
  created: boolean;
  deduplicatedWith: string | null;
  warnings: string[];
}

export class FindMemoryResponseDto {
  objectId: string;
  tag: string;
  lastSeen: MemorySightingDto | null;
  warnings: string[];
}
