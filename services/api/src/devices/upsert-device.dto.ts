import {
  IsBoolean,
  IsEnum,
  IsOptional,
  IsString,
  Length,
} from 'class-validator';
import { DevicePlatform } from './device.entity.js';

export class UpsertDeviceDto {
  @IsEnum(DevicePlatform)
  platform: DevicePlatform;

  @IsOptional()
  @IsString()
  @Length(10, 4096)
  pushToken?: string | null;

  @IsString()
  @Length(2, 20)
  locale: string;

  @IsBoolean()
  notificationsEnabled: boolean;
}
