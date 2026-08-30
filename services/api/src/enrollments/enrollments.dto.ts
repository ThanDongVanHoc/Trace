import { Transform, type TransformFnParams } from 'class-transformer';
import {
  IsIn,
  IsNumber,
  IsOptional,
  IsString,
  Length,
  Max,
  Min,
} from 'class-validator';

const toNumber = ({ value }: TransformFnParams): unknown =>
  typeof value === 'number' ? value : Number(value);

const trim = ({ value }: TransformFnParams): unknown =>
  typeof value === 'string' ? value.trim() : value;

export class CreateEnrollmentDto {
  @Transform(trim)
  @IsString()
  @Length(1, 80)
  tag: string;

  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @Min(0)
  @Max(1)
  roiLeft: number;

  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @Min(0)
  @Max(1)
  roiTop: number;

  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @Min(0)
  @Max(1)
  roiRight: number;

  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @Min(0)
  @Max(1)
  roiBottom: number;

  @IsOptional()
  @Transform(toNumber)
  @IsNumber({ allowInfinity: false, allowNaN: false })
  @IsIn([0, 90, 180, 270])
  rotationDegrees = 0;
}

export class EnrollmentResponseDto {
  objectId: string;
  referenceId: string;
  qualityScore: number;
  embeddingCount: number;
  warnings: string[];
}
