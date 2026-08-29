import { Type } from 'class-transformer';
import {
  ArrayMaxSize,
  ArrayMinSize,
  IsArray,
  IsISO8601,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsOptional,
  IsUUID,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';

export class SightingItemDto {
  @IsUUID()
  id: string;

  @IsUUID()
  objectId: string;

  @IsISO8601()
  detectedAt: string;

  @IsOptional()
  @IsLatitude()
  latitude?: number | null;

  @IsOptional()
  @IsLongitude()
  longitude?: number | null;

  @IsOptional()
  @IsNumber()
  @Min(0)
  accuracyMeters?: number | null;

  @IsNumber()
  @Min(0)
  @Max(1)
  confidence: number;

  @IsOptional()
  @IsUUID()
  evidenceAssetId?: string | null;
}

export class BatchSightingsDto {
  @IsArray()
  @ArrayMinSize(1)
  @ArrayMaxSize(100)
  @ValidateNested({ each: true })
  @Type(() => SightingItemDto)
  items: SightingItemDto[];
}
