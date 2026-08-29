import {
  IsInt,
  IsOptional,
  IsString,
  IsUUID,
  Length,
  Min,
} from 'class-validator';

export class CreateObjectDto {
  @IsOptional()
  @IsUUID()
  id?: string;

  @IsString()
  @Length(1, 80)
  tag: string;

  @IsInt()
  @Min(1)
  referenceRevision: number;
}

export class UpdateObjectDto {
  @IsOptional()
  @IsString()
  @Length(1, 80)
  tag?: string;

  @IsOptional()
  @IsInt()
  @Min(1)
  referenceRevision?: number;
}
